package com.provys.ealoader.catalogue;

import com.provys.object.ProvysNmObjectManager;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Collection;

public interface EntityGrpManager extends ProvysNmObjectManager<EntityGrp> {

    /**
     * Retrieve entity groups that are children of given parent group.
     */
    @Nonnull
    Collection<EntityGrp> getByParentId(BigInteger parentId);

}