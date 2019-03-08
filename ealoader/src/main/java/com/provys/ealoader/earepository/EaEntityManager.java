package com.provys.ealoader.earepository;

import com.provys.catalogue.api.Entity;
import org.sparx.Element;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public interface EaEntityManager {

    /**
     * @param entity is entity which we want to find
     * @return element corresponding to given entity; registers element if one is not cached yet
     */
    @Nonnull
    Element getElement(Entity entity);

    /**
     * @param entityId is UID of entity which we want to find
     * @return package corresponding to given entity group; register package if one is not cached yet
     */
    @Nonnull
    Element getElement(BigInteger entityId);

    /**
     * Synchronise element in Enterprise Architect with entity data
     *
     * @param entity is entity we want to synchronize
     */
    void syncElement(Entity entity);

    /**
     * Synchronize all elements with their corresponding entities
     */
    void syncAllElements();
}
