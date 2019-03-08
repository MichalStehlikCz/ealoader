package com.provys.ealoader.earepository;

import com.provys.catalogue.api.Entity;
import com.provys.catalogue.api.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparx.Element;
import org.sparx.Package;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class EaEntityManagerImpl implements EaEntityManager {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(EaEntityGrpManagerImpl.class.getName());

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
            if (child.GetAlias().equals(entity.getNameNm())) {
                LOG.info("Found element corresponding to entity {}", entity::getNameNm);
                if (elementById.put(entity.getId(), child) != null) {
                    LOG.warn("Adding existing entity {} to index", entity::getNameNm);
                }
                return child;
            }
        }
        LOG.info("Create element for entity {}", entity::getNameNm);
        var newElement = childElements.AddNew(entity.getName(), "Entity");
        newElement.Update();
        newElement.SetStereotype("ArchiMate_DataObject");
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

    @Override
    public void syncElement(Entity entity) {
         Element element = getElement(entity);
         element.SetName(entity.getName());
         element.SetNotes(entity.getNote().orElse(null));
         element.Update();
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
        // we want to check if there are no unrecognized packages...
        //for (var entityGrp : entityGrps) {
        //    indicateUnusedEntityGrpChildren(entityGrp);
        //}
    }
}
