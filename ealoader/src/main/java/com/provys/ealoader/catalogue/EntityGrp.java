package com.provys.ealoader.catalogue;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

/**
 * Represents entity group in PROVYS
 */
public interface EntityGrp extends Comparable<EntityGrp> {

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

    int getOrd();

    @Nonnull
    SortedSet<EntityGrp> getChildren();

    /**
     * @return ordering of given entity group (using ord column in full hierarchy of parents)
     */
    @Nonnull
    List<Integer> getFullOrd();
}
