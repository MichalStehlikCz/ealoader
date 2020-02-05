package com.provys.ealoader.earepository;

import org.sparx.Repository;

import javax.annotation.Nonnull;

public interface EaRepository {

    /**
     * @return Enterprise Architect repository containing PROVYS description
     * @throws IllegalStateException if repository was not set yet
     */
    @Nonnull
    Repository getEaRepository();

    /**
     * @return Enterprise Architect entity group manager
     */
    @Nonnull
    EaEntityGrpManager getEaEntityGrpManager();

    /**
     * @return Enterprise Architect entity manager
     */
    @Nonnull
    EaEntityManager getEaEntityManager();

    /**
     * @return Enterprise Architect attribute manager
     */
    @Nonnull
    EaAttrManager getEaAttrManager();
}
