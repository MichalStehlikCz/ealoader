package com.provys.ealoader.catalogue.impl;

import com.provys.ealoader.catalogue.CatRepository;
import com.provys.ealoader.catalogue.EntityManager;
import com.provys.object.impl.ProvysRepositoryImpl;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class CatRepositoryImpl extends ProvysRepositoryImpl implements CatRepository {

    @Nonnull
    private final EntityGrpManagerImpl entityGrpManager;
    @Nonnull
    private final EntityManagerImpl entityManager;

    @SuppressWarnings("CdiUnproxyableBeanTypesInspection")
    @Inject
    CatRepositoryImpl(EntityGrpLoader entityGrpLoader) {
        this.entityGrpManager = new EntityGrpManagerImpl(this, Objects.requireNonNull(entityGrpLoader));
        this.entityManager = new EntityManagerImpl(this);
    }

    @Nonnull
    @Override
    public EntityGrpManagerImpl getEntityGrpManager() {
        return entityGrpManager;
    }

    @Nonnull
    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
