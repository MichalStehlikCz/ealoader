package com.provys.object.impl;

import com.provys.object.ProvysObject;

import javax.annotation.Nonnull;
import java.math.BigInteger;

public interface ProvysObjectLoader<R extends ProvysRepositoryImpl, T extends ProvysObject> {
    @Nonnull
    T loadById(R repository, BigInteger id);

    void loadValue(R repository, T entityGrpProxy);

    void loadAll(R repository);
}
