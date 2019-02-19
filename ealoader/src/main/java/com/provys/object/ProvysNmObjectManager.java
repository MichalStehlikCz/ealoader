package com.provys.object;

import com.provys.ealoader.catalogue.EntityGrp;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface ProvysNmObjectManager<T extends ProvysNmObject> extends ProvysObjectManager<T> {
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
}
