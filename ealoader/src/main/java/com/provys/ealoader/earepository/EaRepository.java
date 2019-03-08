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
     * Set Enterprise Architect repository containing PROVYS description
     *
     * @param eaRepository is repository we should work with
     */
    void setEaRepository(Repository eaRepository);

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
}
