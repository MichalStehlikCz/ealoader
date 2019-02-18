package com.provys.ealoader.catalogue.impl;

import com.provys.common.exception.RegularException;
import com.provys.ealoader.catalogue.EntityGrp;
import com.provys.ealoader.catalogue.EntityGrpManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
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

    private final Map<BigInteger, EntityGrpImpl> entityGrpById = new ConcurrentHashMap<>(10);
    private final Map<String, EntityGrpImpl> entityGrpByNameNm = new ConcurrentHashMap<>(10);

    EntityGrpManagerImpl(CatRepositoryImpl repository, EntityGrpLoader loader) {
        this.repository = Objects.requireNonNull(repository);
        this.loader = Objects.requireNonNull(loader);
    }

    @Nonnull
    @Override
    public EntityGrpImpl getById(BigInteger id) {
        EntityGrpImpl entityGrp = entityGrpById.get(Objects.requireNonNull(id));
        if (entityGrp == null) {
            entityGrp = loader.loadById(repository, id);
        }
        return entityGrp;
    }

    @Nonnull
    @Override
    public EntityGrpImpl getByNameNm(String nameNm) {
        EntityGrpImpl entityGrp = entityGrpByNameNm.get(Objects.requireNonNull(nameNm));
        if (entityGrp == null) {
            entityGrp = loader.loadByNameNm(repository, nameNm).orElseThrow(() -> new RegularException(LOG,
                    "CAT_ENTITYGRPNOTFOUNDBYNM", "Entity group not found using supplied internal name " + nameNm));
        }
        return entityGrp;
    }

    @Nonnull
    @Override
    public Optional<EntityGrp> getByNameNmIfExists(String nameNm) {
        EntityGrpImpl entityGrp = entityGrpByNameNm.get(Objects.requireNonNull(nameNm));
        if (entityGrp == null) {
            entityGrp = loader.loadByNameNm(repository, nameNm).orElse(null);
        }
        return Optional.ofNullable(entityGrp);
    }

    @Nonnull
    @Override
    public Collection<EntityGrp> getAll() {
        loader.loadAll(repository);
        return Collections.unmodifiableCollection(entityGrpById.values());
    }

    /**
     * Retrieve entity group if already loaded to cache.
     *
     * @param id is Id of entity group being looked for
     * @return entity group if present in cache, empty optional otherwise
     */
    public Optional<EntityGrpImpl> getByIdIfPresent(BigInteger id) {
        return Optional.ofNullable(entityGrpById.get(Objects.requireNonNull(id)));
    }

    /**
     * Register given entity group. If entity group with given Id is already registered, do nothing
     *
     * @param entityGrp is entity group to be registered
     * @return supplied entity group if it was successfully registered, entity group with given Id if entity group
     * with given Id was already registered
     */
    public EntityGrpImpl register(EntityGrpImpl entityGrp) {
        var result = entityGrpById.putIfAbsent(entityGrp.getId(), entityGrp);
        if (result == null) {
            result = entityGrp;
            var nmEntityGrp = entityGrpByNameNm.putIfAbsent(entityGrp.getNameNm(), entityGrp);
            if (nmEntityGrp != null) {
                LOG.warn("Replacing entity group by internal name {}", entityGrp.getNameNm());
                entityGrpById.remove(nmEntityGrp.getId());
            }
        }
        return result;
    }
}
