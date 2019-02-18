package com.provys.ealoader.catalogue;

import javax.annotation.Nonnull;

public interface CatRepository {

    /**
     * @return Entity Group manager
     */
    @Nonnull
    EntityGrpManager getEntityGrpManager();
}
