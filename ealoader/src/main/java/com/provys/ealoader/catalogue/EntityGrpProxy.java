package com.provys.ealoader.catalogue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Optional;

class EntityGrpProxy implements EntityGrp {

    @Nullable

    @Nonnull
    @Override
    public BigInteger getId() {
        return null;
    }

    @Nonnull
    @Override
    public Optional<EntityGrp> getParent() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public String getNameNm() {
        return null;
    }

    @Nonnull
    @Override
    public String getName() {
        return null;
    }

    @Nonnull
    @Override
    public Optional<String> getNote() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Integer getOrd() {
        return null;
    }
}
