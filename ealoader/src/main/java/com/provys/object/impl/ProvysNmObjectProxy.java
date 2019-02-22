package com.provys.object.impl;

import java.math.BigInteger;

abstract public class ProvysNmObjectProxy<R extends ProvysRepositoryImpl, V extends ProvysNmObjectValue>
        extends ProvysObjectProxy<R, V> {

    public ProvysNmObjectProxy(R repository, BigInteger id) {
        super(repository, id);
    }

}
