package com.provys.ealoader.catalogue.impl;

import com.provys.ealoader.catalogue.Entity;
import com.provys.ealoader.catalogue.EntityManager;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class EntityManagerImpl implements EntityManager {

    @Nonnull
    private final CatRepositoryImpl repository;

    EntityManagerImpl(CatRepositoryImpl repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Nonnull
    @Override
    public Entity getById(BigInteger id) {
        return null;
    }

    @Nonnull
    @Override
    public Entity getByNameNm(String nameNm) {
        return null;
    }

    @Nonnull
    @Override
    public Optional<Entity> getByNameNmIfExists(String nameNm) {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Collection<Entity> getByEntityGrpId(BigInteger entityGrpId) {
        return null;
    }

    @Nonnull
    @Override
    public Collection<Entity> getAll() {
        return null;
    }
}
