package com.provys.ealoader.catalogue.impl;

import com.provys.object.impl.ProvysNmObjectLoader;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Set;

/**
 * Loader is responsible for retrieving objects from remote repository, their registration in manager and filling in
 * values in proxy object.
 */
public interface EntityGrpLoader extends ProvysNmObjectLoader<CatRepositoryImpl, EntityGrpProxy> {

    @Nonnull
    Set<EntityGrpProxy> loadByParentId(CatRepositoryImpl repository, BigInteger parentId);
}
