package com.provys.object.impl;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface ProvysNmObjectLoader<R extends ProvysRepositoryImpl, T extends ProvysNmObjectProxy>
        extends ProvysObjectLoader<R, T> {

    @Nonnull
    Optional<T> loadByNameNm(T repository, String nameNm);
}
