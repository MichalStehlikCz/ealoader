package com.provys.ealoader.earepository;

import com.provys.catalogue.api.EntityGrp;
import org.sparx.Package;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public interface EaEntityGrpManager {

    /**
     * @param entityGrp is entity group which we want to find
     * @return package corresponding to given entity group; register package if one is not cached yet
     */
    @Nonnull
    Package getPackage(EntityGrp entityGrp);

    /**
     * @param entityGrpId is UID of entity group which we want to find
     * @return package corresponding to given entity group; register package if one is not cached yet
     */
    @Nonnull
    Package getPackage(BigInteger entityGrpId);

    /**
     * Synchronise package in Enterprise Architect with entity group data
     *
     * @param entityGrp is entity group we want to synchronize
     */
    void syncPackage(EntityGrp entityGrp);

    /**
     * Synchronize all packages with their corresponding entity groups
     */
    void syncAllPackages();
}
