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

    /**
     * @return Id (attribute ENTITYGRP_ID)
     */
    @Nonnull
    BigInteger getId();

    /**
     * @return parent entity group (entity group with Id PARENT_ID)
     */
    @Nonnull
    Optional<EntityGrp> getParent();

    /**
     * @return internal name (attribute NAME_NM)
     */
    @Nonnull
    String getNameNm();

    /**
     * @return name (attribute NAME)
     */
    @Nonnull
    String getName();

    /**
     * @return note (attribute NOTE)
     */
    @Nonnull
    Optional<String> getNote();

    /**
     * @return order (attribute ORD)
     */
    int getOrd();

    /**
     * @return child entity groups, sorted by Ord
     */
    @Nonnull
    SortedSet<EntityGrp> getChildren();

    /**
     * @return ordering of given entity group (using ord column in full hierarchy of parents)
     */
    @Nonnull
    List<Integer> getFullOrd();
}
