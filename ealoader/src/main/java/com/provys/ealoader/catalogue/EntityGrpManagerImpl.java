package com.provys.ealoader.catalogue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ApplicationScoped
class EntityGrpManagerImpl implements EntityGrpManager {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(EntityGrpManagerImpl.class.getName());

    private final Map<BigInteger, EntityGrpImpl> entityGrpById = new ConcurrentHashMap<>(10);
    private final Map<String, EntityGrpImpl> entityGrpByNameNm = new ConcurrentHashMap<>(10);

    @Nonnull
    @Override
    public Optional<EntityGrpImpl> getById(BigInteger id) {
        return Optional.ofNullable(entityGrpById.get(Objects.requireNonNull(id)));
    }

    @Nonnull
    @Override
    public Optional<EntityGrp> getByNameNm(String nameNm) {
        return Optional.ofNullable(entityGrpByNameNm.get(Objects.requireNonNull(nameNm)));
    }

    @Nonnull
    @Override
    public Collection<EntityGrp> getAll() {
        return Collections.unmodifiableCollection(entityGrpById.values());
    }

    @Nonnull
    EntityGrpImpl getByIdOrRegister(BigInteger id, Supplier<EntityGrpImpl> entityGrpSupplier) {
        var result = entityGrpById.get(Objects.requireNonNull(id));
        if (result == null) {
            var newEntityGrp = entityGrpSupplier.get();
            if (newEntityGrp == null) {
                throw new RuntimeException("Unexpected null result when getting new entity group");
            }
            result = entityGrpById.putIfAbsent(id, newEntityGrp);
            if (result == null) {
                var conflict = entityGrpByNameNm.put(newEntityGrp.getNameNm(), newEntityGrp);
                if (conflict != null) {
                    LOG.warn("Conflict in entity group internal name {}, entity groups new {} and old {}",
                            newEntityGrp.getNameNm(), newEntityGrp, conflict);
                    entityGrpById.remove(conflict.getId());
                }
                result = newEntityGrp;
            }
        }
        return result;
    }

    @Override
    public void load(DSLContext dslContext) {
        if (!entityGrpById.isEmpty()) {
            throw new IllegalStateException("Entity groups are already loaded");
        }
        var loader = new EntityGrpLoader(this);
        loader.run(dslContext);
    }
}
