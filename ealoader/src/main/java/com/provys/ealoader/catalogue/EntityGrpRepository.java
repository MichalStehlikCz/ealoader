package com.provys.ealoader.catalogue;

import org.jooq.DSLContext;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

public interface EntityGrpRepository {
    /**
     * Retrieve entity group from repository.
     *
     * @param id is id of entity group to be retrieved
     * @return entity group if present in repositoy, empty optional otherwise
     */
    @Nonnull
    Optional<? extends EntityGrp> getById(BigInteger id);

    @Nonnull
    Collection<? extends EntityGrp> getAll();

    /**
     * Loads entity groups from specified repository.
     *
     * @param dslContext is database context used to load data from PROVYS
     * @throws IllegalStateException if entity groups already exist (does not support reload)
     */
    void load(DSLContext dslContext);
}