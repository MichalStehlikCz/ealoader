package com.provys.ealoader.catalogue;

import org.jooq.DSLContext;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@ApplicationScoped
class EntityGrpRepositoryImpl implements EntityGrpRepository {

    private final Map<BigInteger, EntityGrpImpl> entityGrpById = new ConcurrentHashMap<>(10);

    @Nonnull
    @Override
    public Optional<EntityGrpImpl> getById(BigInteger id) {
        return Optional.ofNullable(entityGrpById.get(Objects.requireNonNull(id)));
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
