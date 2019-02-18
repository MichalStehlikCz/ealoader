package com.provys.ealoader.catalogue.impl;

import com.provys.ealoader.catalogue.CatRepository;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class CatRepositoryImpl implements CatRepository {

    @Nonnull
    private final EntityGrpManagerImpl entityGrpManager;

    @SuppressWarnings("CdiUnproxyableBeanTypesInspection")
    @Inject
    CatRepositoryImpl(EntityGrpLoader entityGrpLoader) {
        this.entityGrpManager = new EntityGrpManagerImpl(this, Objects.requireNonNull(entityGrpLoader));
    }

    @Nonnull
    @Override
    public EntityGrpManagerImpl getEntityGrpManager() {
        return entityGrpManager;
    }
}
