package com.provys.object;

import com.provys.ealoader.catalogue.EntityGrp;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Collection;

public interface ProvysObjectManager<T extends ProvysObject> {
    /**
     * Retrieve entity group from repository using supplied UID. Try to load entity group from database if not present
     * in cache
     *
     * @param id is id of entity group to be retrieved
     * @return entity group with specified id
     * @throws RuntimeException if entity group with given id is not found
     */
    @Nonnull
    T getById(BigInteger id);

    /**
     * Retrieve all entity groups. Load all entity groups from database to cache
     *
     * @return collection of all entity groups in database
     */
    @Nonnull
    Collection<T> getAll();
}
