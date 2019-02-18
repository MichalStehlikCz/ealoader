package com.provys.ealoader.catalogue.impl;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Optional;

public interface EntityGrpLoader {

    @Nonnull
    EntityGrpImpl loadById(CatRepositoryImpl repository, BigInteger id);

    @Nonnull
    Optional<EntityGrpImpl> loadByNameNm(CatRepositoryImpl repository, String nameNm);

    void loadAll(CatRepositoryImpl repository);
}
