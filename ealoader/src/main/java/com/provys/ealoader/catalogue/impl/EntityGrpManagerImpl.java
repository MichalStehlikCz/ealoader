package com.provys.ealoader.catalogue.impl;

import com.provys.common.exception.InternalException;
import com.provys.common.exception.RegularException;
import com.provys.ealoader.catalogue.EntityGrp;
import com.provys.ealoader.catalogue.EntityGrpManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityGrpManagerImpl implements EntityGrpManager {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(EntityGrpManagerImpl.class.getName());

    @Nonnull
    private final CatRepositoryImpl repository;
    @Nonnull
    private final EntityGrpLoader loader;

    private final Map<BigInteger, EntityGrpProxy> entityGrpById = new ConcurrentHashMap<>(100);
    private final Map<String, EntityGrpProxy> entityGrpByNameNm = new ConcurrentHashMap<>(100);
    private final Map<BigInteger, Set<EntityGrpProxy>> entityGrpByParentId = new ConcurrentHashMap<>(100);

    EntityGrpManagerImpl(CatRepositoryImpl repository, EntityGrpLoader loader) {
        this.repository = Objects.requireNonNull(repository);
        this.loader = Objects.requireNonNull(loader);
    }

    @Nonnull
    EntityGrpLoader getLoader() {
        return loader;
    }

    @Nonnull
    @Override
    public EntityGrpProxy getById(BigInteger id) {
        EntityGrpProxy entityGrp = entityGrpById.get(Objects.requireNonNull(id));
        if (entityGrp == null) {
            entityGrp = loader.loadById(repository, id);
        }
        return entityGrp;
    }

    @Nonnull
    @Override
    public EntityGrpProxy getByNameNm(String nameNm) {
        EntityGrpProxy entityGrp = entityGrpByNameNm.get(Objects.requireNonNull(nameNm));
        if (entityGrp == null) {
            entityGrp = loader.loadByNameNm(repository, nameNm).orElseThrow(() -> new RegularException(LOG,
                    "CAT_ENTITYGRPNOTFOUNDBYNM", "Entity group not found using supplied internal name " + nameNm));
        }
        return entityGrp;
    }

    @Nonnull
    @Override
    public Optional<EntityGrp> getByNameNmIfExists(String nameNm) {
        EntityGrpProxy entityGrp = entityGrpByNameNm.get(Objects.requireNonNull(nameNm));
        if (entityGrp == null) {
            entityGrp = loader.loadByNameNm(repository, nameNm).orElse(null);
        }
        return Optional.ofNullable(entityGrp);
    }

    @Nonnull
    @Override
    public Collection<EntityGrp> getByParentId(BigInteger parentId) {
        // check if Id is valid entity group Id
        getById(parentId);
        Set<EntityGrpProxy> children = entityGrpByParentId.get(parentId);
        if (children == null) {
            children = loader.loadByParentId(repository, parentId);
            var old = entityGrpByParentId.putIfAbsent(parentId, children);
            if (old != null) {
                children = old;
            }
        }
        return Collections.unmodifiableSet(children);
    }

    @Nonnull
    @Override
    public Collection<EntityGrp> getAll() {
        loader.loadAll(repository);
        return Collections.unmodifiableCollection(entityGrpById.values());
    }

    /**
     * Retrieve entity group if already loaded to cache, otherwise create new proxy for given id. Should only be called
     * internally as method does not verify existence of given object in database.
     *
     * @param id is Id of entity group being looked for
     * @return entity group present in cache or newly added proxy
     */
    public EntityGrpProxy getOrAddById(BigInteger id) {
        return entityGrpById.computeIfAbsent(Objects.requireNonNull(id), key -> new EntityGrpProxy(repository, key));
    }

    /**
     * Register given entity group in indices. Verifies that entity group is registered for its id, if not, throws
     * exception
     *
     * @param entityGrp is entity group to be registered
     * @param oldValue are old values associated with entity group
     * @param newValue are new values associated with entity group
     */
    void registerChange(EntityGrpProxy entityGrp, @Nullable EntityGrpValue oldValue, @Nullable EntityGrpValue newValue)
    {
        synchronized(entityGrpById) {
            if (entityGrpById.get(entityGrp.getId()) != entityGrp) {
                throw new InternalException(LOG, "Register change called on entity unregistered entity group");
            }
            // change of nameNm
            if ((oldValue != null) && ((newValue == null) || (!oldValue.getNameNm().equals(newValue.getNameNm())))) {
                // remove old value (if it was not redirected yet)
                var oldEntityGrp = entityGrpByNameNm.remove(oldValue.getNameNm());
                if ((oldEntityGrp != null) && (oldEntityGrp != entityGrp)) {
                    entityGrpByNameNm.putIfAbsent(oldEntityGrp.getNameNm(), oldEntityGrp);
                }
            }
            if ((newValue != null) && ((oldValue == null) || (!newValue.getNameNm().equals(oldValue.getNameNm())))) {
                // register new value
                var oldEntityGrp = entityGrpByNameNm.put(newValue.getNameNm(), entityGrp);
                if ((oldEntityGrp != null) && (oldEntityGrp != entityGrp)) {
                    LOG.warn("Replaced entity group in internal name index {}", entityGrp.getNameNm());
                }
            }
            // change of parent
            if ((oldValue != null) && (oldValue.getParent().isPresent()) &&
                    ((newValue == null) ||
                            (!oldValue.getParent().get().equals(newValue.getParent().orElse(null))))) {
                // remove from children of old parent
                var setByParent = entityGrpByParentId.get(oldValue.getParent().get().getId());
                if (setByParent != null) {
                    setByParent.remove(entityGrp);
                }
            }
            if ((newValue != null) && newValue.getParent().isPresent() &&
                    ((oldValue == null) ||
                            (!newValue.getParent().get().equals(oldValue.getParent().orElse(null))))) {
                var setByParent = entityGrpByParentId.get(newValue.getParent().get().getId());
                if (setByParent != null) {
                    setByParent.add(entityGrp);
                }
            }
        }
    }

    /**
     * Remove given entity group. Used as reaction to delete. Note that even if references are removed from indices,
     * there might still be objects that retain reference and thus might stumble across invalid entity group
     */
    void unregister(EntityGrpProxy entityGrp, @Nullable EntityGrpValue oldValue) {
        // remove from additional indices
        if (oldValue != null) {
            registerChange(entityGrp, oldValue, null);
        }
        // remove from primary index
        var oldEntityGrp = entityGrpById.remove(entityGrp.getId());
        if ((oldEntityGrp != null) && (oldEntityGrp != entityGrp)) {
            entityGrpById.putIfAbsent(oldEntityGrp.getId(), oldEntityGrp);
        }
    }

}
