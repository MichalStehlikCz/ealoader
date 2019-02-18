package com.provys.ealoader.catalogue;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

public interface EntityGrpManager {

    /**
     * Retrieve entity group from repository using supplied UID. Try to load entity group from database if not present
     * in cache
     *
     * @param id is id of entity group to be retrieved
     * @return entity group with specified id
     * @throws RuntimeException if entity group with given id is not found
     */
    @Nonnull
    EntityGrp getById(BigInteger id);

    /**
     * Retrieve entity group from repository using supplied internal name. Try to load entity group from database if not
     * present in cache
     *
     * @param nameNm is internal name of entity group to be retrieved
     * @return entity group with specified internal name
     * @throws RuntimeException if entity group with given internal name is not found
     */
    @Nonnull
    EntityGrp getByNameNm(String nameNm);

    /**
     * Retrieve entity group from repository using supplied internal name. Try to load entity group from database if not
     * present in cache
     *
     * @param nameNm is internal name of entity group to be retrieved
     * @return entity group with specified internal name, empty optional if entity group with such internal name doesn't
     * exist
     */
    @Nonnull
    Optional<EntityGrp> getByNameNmIfExists(String nameNm);

    /**
     * Retrieve all entity groups. Load all entity groups from database to cache
     *
     * @return collection of all entity groups in database
     */
    @Nonnull
    Collection<EntityGrp> getAll();

}