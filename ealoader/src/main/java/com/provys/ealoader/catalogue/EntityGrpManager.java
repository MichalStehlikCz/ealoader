package com.provys.ealoader.catalogue;

import org.jooq.DSLContext;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

public interface EntityGrpManager {

    /**
     * Retrieve entity group from repository using supplied UID.
     *
     * @param id is id of entity group to be retrieved
     * @return entity group if present in repository, empty optional otherwise
     */
    @Nonnull
    Optional<? extends EntityGrp> getById(BigInteger id);

    /**
     * Retrieve entity group from repository using supplied internal name.
     *
     * @param nameNm is internal name of entity group to be retrieved
     * @return entity group if present in repository, empty optional otherwise
     */
    @Nonnull
    Optional<EntityGrp> getByNameNm(String nameNm);

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