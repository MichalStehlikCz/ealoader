package com.provys.ealoader.catalogue;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

public interface EntityManager {

    /**
     * Retrieve entity from repository using supplied UID. Try to load entity from database if not present in cache
     *
     * @param id is id of entity to be retrieved
     * @return entity with specified id
     * @throws RuntimeException if entity with given id is not found
     */
    @Nonnull
    Entity getById(BigInteger id);

    /**
     * Retrieve entity from repository using supplied internal name. Try to load entity from database if not present in
     * cache
     *
     * @param nameNm is internal name of entity to be retrieved
     * @return entity with specified internal name
     * @throws RuntimeException if entity with given internal name is not found
     */
    @Nonnull
    Entity getByNameNm(String nameNm);

    /**
     * Retrieve entity from repository using supplied internal name. Try to load entity from database if not present in
     * cache
     *
     * @param nameNm is internal name of entity to be retrieved
     * @return entity with specified internal name, empty optional if entity with such internal name doesn't exist
     */
    @Nonnull
    Optional<Entity> getByNameNmIfExists(String nameNm);

    /**
     * Retrieve entities belonging to entity group with given id from repository. Try to load entities from database if
     * not present in cache
     *
     * @param entityGrpId is UID of entity group
     * @return entities belonging to given entity group
     */
    @Nonnull
    Collection<Entity> getByEntityGrpId(BigInteger entityGrpId);

    /**
     * Retrieve all entities. Load all entities from database to cache
     *
     * @return collection of all entities in database
     */
    @Nonnull
    Collection<Entity> getAll();

}
