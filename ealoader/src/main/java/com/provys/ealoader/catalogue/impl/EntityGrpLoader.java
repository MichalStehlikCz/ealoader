package com.provys.ealoader.catalogue.impl;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

/**
 * Loader is responsible for retrieving objects from remote repository, their registration in manager and filling in
 * values in proxy object.
 */
public interface EntityGrpLoader {

    @Nonnull
    EntityGrpProxy loadById(CatRepositoryImpl repository, BigInteger id);

    void loadValue(CatRepositoryImpl repository, EntityGrpProxy entityGrpProxy);

    @Nonnull
    Optional<EntityGrpProxy> loadByNameNm(CatRepositoryImpl repository, String nameNm);

    @Nonnull
    Set<EntityGrpProxy> loadByParentId(CatRepositoryImpl repository, BigInteger parentId);

    void loadAll(CatRepositoryImpl repository);
}
