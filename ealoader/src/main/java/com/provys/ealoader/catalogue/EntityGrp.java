package com.provys.ealoader.catalogue;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Optional;

/**
 * Represents entity group in PROVYS
 */
public interface EntityGrp {

    @Nonnull
    BigInteger getId();

    @Nonnull
    Optional<EntityGrp> getParent();

    @Nonnull
    String getNameNm();

    @Nonnull
    String getName();

    @Nonnull
    Optional<String> getNote();

    @Nonnull
    Integer getOrd();
}
