package com.provys.ealoader.earepository;

import com.provys.catalogue.api.Entity;
import com.provys.catalogue.api.EntityGrp;
import com.provys.catalogue.api.EntityManager;
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
    private final Map<BigInteger, Element> elementById = new ConcurrentHashMap<>(10);

    EaEntityManagerImpl(EaRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    private Element registerElement(Entity entity) {
        Package entityGrpPackage = repository.getEaEntityGrpManager().getPackage(entity.getEntityGrp().
                orElseThrow());
        var childElements = entityGrpPackage.GetElements();
        for (var child : childElements) {
            if ((child.GetAlias().equals(entity.getNameNm())) && (child.GetStereotype().equals(STEREOTYPE))) {
                LOG.info("Found element corresponding to entity {}", entity::getNameNm);
                if (elementById.put(entity.getId(), child) != null) {
                    LOG.warn("Adding existing entity {} to index", entity::getNameNm);
                }
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
    public Element getElement(BigInteger entityId) {
        Element eaElement = elementById.get(Objects.requireNonNull(entityId));
        if (eaElement == null) {
            eaElement = registerElement(entityManager.getById(entityId));
        }
        return eaElement;
    }

    private void syncEntityElement(Entity entity, Element element) {
        if (!element.GetName().equals(entity.getName()) ||
                !element.GetNotes().equals(entity.getNote().orElse(""))) {
            element.SetName(entity.getName());
            element.SetNotes(entity.getNote().orElse(null));
            element.Update();
        }
    }

    private void syncAncestorConnection(Entity entity, Element element) {
        if (entity.getAncestor().isPresent()) {
            @SuppressWarnings("squid:S3655") // we checked presence opening this block... it doesn't change that often
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
         Element element = getElement(entity);
         syncEntityElement(entity, element);
         syncAncestorConnection(entity, element);
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

    public void mapElements(Package entityGrpPackage) {
        var update = false;
        var elements = entityGrpPackage.GetElements();
        for (short i = 0; i < elements.GetCount(); i++) {
            var entityElement = elements.GetAt(i);
            if (entityElement.GetType().equals(TYPE) && (entityElement.GetStereotype().equals(STEREOTYPE)) &&
                    (!entityElement.GetAlias().isEmpty())) {
                var entity = entityManager.getByNameNmIfExists(entityElement.GetAlias());
                if (entity
                        .flatMap(Entity::getEntityGrp)
                        .map(EntityGrp::getNameNm)
                        .filter(nameNm -> nameNm.equals(entityGrpPackage.GetAlias()))
                        .isPresent()) {
                    LOG.info("Entity {} found in group {}",
                            entityElement::GetAlias, entityGrpPackage::GetAlias);
                } else {
                    LOG.info("Entity corresponding to element {} in package {} not found, removing",
                            entityElement::GetAlias, entityGrpPackage::GetAlias
                    );
                    elements.Delete(i);
                    update = true;
                }
            }
        }
        if (update) {
            entityGrpPackage.Update();
            elements.destroy();
        }
    }
}
