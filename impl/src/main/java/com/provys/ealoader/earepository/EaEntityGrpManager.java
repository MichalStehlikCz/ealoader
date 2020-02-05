package com.provys.ealoader.earepository;

import com.provys.catalogue.api.EntityGrp;
import com.provys.common.datatype.DtUid;
import org.sparx.Package;

import javax.annotation.Nonnull;

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
    Package getPackage(DtUid entityGrpId);

    /**
     * Synchronise package in Enterprise Architect with entity group data
     *
     * @param entityGrp is entity group we want to synchronize
     */
    void syncPackage(EntityGrp entityGrp);

    /**
     * Synchronise package in Enterprise Architect with entity group data, based on internal name of entity group
     *
     * @param entityGrpNm is internal name of entity group we want to synchronize
     */
    void syncPackage(String entityGrpNm);

    /**
     * Synchronize all packages with their corresponding entity groups
     */
    void syncAllPackages();
}
