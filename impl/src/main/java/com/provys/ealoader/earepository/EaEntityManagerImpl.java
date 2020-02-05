package com.provys.ealoader.earepository;

import com.provys.catalogue.api.Entity;
import com.provys.catalogue.api.EntityManager;
import com.provys.common.datatype.DtUid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparx.Connector;
import org.sparx.Element;
import org.sparx.Package;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class EaEntityManagerImpl implements EaEntityManager {

    private static final Logger LOG = LogManager.getLogger(EaEntityManagerImpl.class);

    private static final String TYPE = "Entity";
    private static final String STEREOTYPE = "ArchiMate_DataObject";
    private static final String SUPERTYPE_CONNECTOR_TYPE = "Generalization";
    private static final String SUPERTYPE_CONNECTOR_STEREOTYPE = "ArchiMate_Specialization";

    @Nonnull
    private final EaRepository repository;
    @Nonnull
    private final EntityManager entityManager;
    @Nonnull
    private final Map<DtUid, Element> elementById = new ConcurrentHashMap<>(10);

    EaEntityManagerImpl(EaRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    private Element registerElement(Entity entity) {
        Package entityGrpPackage = repository.getEaEntityGrpManager().getPackage(entity.getEntityGrp().
                orElseThrow());
        var childElements = entityGrpPackage.GetElements();
        for (var child : childElements) {
            if ((child.GetAlias().equals(entity.getNameNm())) && child.GetType().equals(TYPE) &&
                    (child.GetStereotype().equals(STEREOTYPE))) {
                LOG.info("Found element corresponding to entity {}", entity::getNameNm);
                if (elementById.put(entity.getId(), child) != null) {
                    LOG.warn("Adding existing entity {} to index", entity::getNameNm);
                }
                childElements.destroy();
                return child;
            }
        }
        LOG.info("Create element for entity {}", entity::getNameNm);
        var newElement = childElements.AddNew(entity.getName(), TYPE);
        newElement.Update();
        newElement.SetStereotype(STEREOTYPE);
        newElement.SetAlias(entity.getNameNm());
        newElement.Update();
        childElements.destroy();
        if (elementById.put(entity.getId(), newElement) != null) {
            LOG.warn("Adding existing entity {} to index", entity::getNameNm);
        }
        return newElement;
    }

    @Nonnull
    @Override
    public Element getElement(Entity entity) {
        return getElement(entity.getId());
    }

    @Nonnull
    @Override
    public Element getElement(DtUid entityId) {
        Element eaElement = elementById.get(Objects.requireNonNull(entityId));
        if (eaElement == null) {
            eaElement = registerElement(entityManager.getById(entityId));
        }
        return eaElement;
    }

    private void syncEntityElement(Entity entity, Element element) {
        boolean update = false;
        int packageId = entity.getEntityGrp()
                .map(entityGrp -> repository.getEaEntityGrpManager().getPackage(entityGrp))
                .map(Package::GetPackageID)
                .orElse(-1);
        if ((packageId != -1) && (element.GetPackageID() != packageId)) {
            element.SetPackageID(packageId);
            update = true;
        }
        if (!element.GetName().equals(entity.getName())) {
            element.SetName(entity.getName());
            update = true;
        }
        if (!element.GetNotes().equals(entity.getNote().orElse(""))) {
            element.SetNotes(entity.getNote().orElse(null));
            update = true;
        }
        if (update) {
            element.Update();
        }
        repository.getEaAttrManager().syncForEntity(entity, element);
    }

    private void syncAncestorConnection(Entity entity, Element element) {
        if (entity.getAncestor().isPresent()) {
            var ancestor = entity.getAncestor().get();
            var connectors = element.GetConnectors();
            Connector subClass = null;
            for (var connector : connectors) {
                if (connector.GetType().equals(SUPERTYPE_CONNECTOR_TYPE) &&
                        (connector.GetClientID() == element.GetElementID())) {
                    subClass = connector;
                    break;
                }
            }
            if (subClass == null) {
                LOG.info("Add superclass connector {} -> {}", entity::getNameNm, ancestor::getNameNm);
                subClass = connectors.AddNew("", SUPERTYPE_CONNECTOR_TYPE);
                subClass.SetClientID(element.GetElementID());
            } else {
                LOG.info("Found superclass connector {} -> {}", entity::getNameNm, ancestor::getNameNm);
            }
            if ((subClass.GetSupplierID() != getElement(ancestor).GetElementID())
                    || !subClass.GetStereotype().equals(SUPERTYPE_CONNECTOR_STEREOTYPE)) {
                subClass.SetSupplierID(getElement(ancestor).GetElementID());
                subClass.SetStereotype(SUPERTYPE_CONNECTOR_STEREOTYPE);
                subClass.Update();
            }
            connectors.destroy();
        }
    }

    @Override
    public void syncElement(Entity entity) {
         LOG.info("Synchronize entity element {}", entity::getNameNm);
         Element element = getElement(entity);
         syncEntityElement(entity, element);
         syncAncestorConnection(entity, element);
    }

    @Override
    public void syncElement(String entityNm) {
        syncElement(entityManager.getByNameNm(entityNm));
    }

    @Override
    public void syncAllElements() {
        Collection<Entity> entities = entityManager.getAll();
        for (var entity : entities) {
            if (entity.getEntityGrp().isPresent()) {
                // if there is no entity group, there is nowhere to place given entity
                syncElement(entity);
            } else {
                LOG.warn("Skip entity {} - no entity group specified", entity::getNameNm);
            }
        }
    }

    /**
     * Used to process elements of correct type in package and map them to entities / remove ones that cannot be mapped
     */
    private class ElementProcessor implements AutoCloseable {

        private final Package entityGrpPackage;
        private boolean update = false;
        private org.sparx.Collection<Element> elements;
        private short index;

        private ElementProcessor(Package entityGrpPackage) {
            this.entityGrpPackage = entityGrpPackage;
            this.elements = entityGrpPackage.GetElements();
        }

        private void register(Element entityElement, Entity entity) {
            if (elementById.get(entity.getId()) != null) {
                if (!elementById.get(entity.getId()).GetElementGUID().equals(entityElement.GetElementGUID())) {
                    /* in theory, we might prefer to keep one in correct package... but at the moment, we take it easy
                       as it should not happen anyway if somebody didn't make a mistake */
                    LOG.warn("Duplicate element found for entity {}; element in package {} removed",
                            entityElement::GetAlias, entityGrpPackage::GetAlias);
                    elements.Delete(index);
                    update = true;
                }
            } else {
                elementById.put(entity.getId(), entityElement);
            }
        }

        private void removeNonExistent(Element entityElement) {
            LOG.info("Entity corresponding to element {} in package {} not found, removing",
                    entityElement::GetAlias, entityGrpPackage::GetAlias);
            elements.Delete(index);
            update = true;
        }

        private void process() {
            for (index = 0; index < elements.GetCount(); index++) {
                var entityElement = elements.GetAt(index);
                if (entityElement.GetType().equals(TYPE) && (entityElement.GetStereotype().equals(STEREOTYPE)) &&
                        (!entityElement.GetAlias().isEmpty())) {
                    var entity = entityManager.getByNameNmIfExists(entityElement.GetAlias());
                    entity.ifPresentOrElse(entity1 -> register(entityElement, entity1),
                            () -> removeNonExistent(entityElement));
                }
            }
        }

        @Override
        public void close() {
            if (update) {
                entityGrpPackage.Update();
            }
            if (elements != null) {
                elements.destroy();
                elements = null;
            }
        }
    }

    @Override
    public void mapElements(Package entityGrpPackage) {
        try (var processor = new ElementProcessor(entityGrpPackage)) {
            processor.process();
        }
    }
}
