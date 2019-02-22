package com.provys.object.impl;

import com.provys.common.exception.InternalException;
import com.provys.object.ProvysObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Objects;

abstract public class ProvysObjectProxy<R extends ProvysRepositoryImpl, V extends ProvysObjectValue>
        implements ProvysObject {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(ProvysObjectProxy.class);

    @Nonnull
    private final R repository;
    @Nonnull
    private final BigInteger id;
    @Nullable
    private V value;

    ProvysObjectProxy(R repository, BigInteger id) {
        this.repository = Objects.requireNonNull(repository);
        this.id = Objects.requireNonNull(id);
    }

    @Nonnull
    protected R getRepository() {
        return repository;
    }

    @Nonnull
    abstract protected ProvysObjectManagerImpl<R, V, ? extends ProvysObject,
            ? extends ProvysObjectLoader<R, ? extends ProvysObjectProxy<R, V>>> getManager();

    public void setValue(V value) {
        var oldValue = this.value;
        this.value = value;
        getManager().registerChange(this, oldValue, value);
    }

    abstract protected void loadValue();

    @Nonnull
    private V validateValue() {
        if (value == null) {
            loadValue();
            if (value == null) {
                throw new InternalException(LOG, "Load entity grp failed - value empty");
            }
        }
        return value;
    }

    @Nonnull
    public BigInteger getId() {
        return id;
    }

    /**
     * If two instances have same Id, they represent same object and thus are considered to be the same
     *
     * @param other is other object this is being compared to
     * @return true if this and other are proxies of the same type with same Id, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        ProvysObjectProxy otherProxy = (ProvysObjectProxy) other;
        return getId().equals(otherProxy.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Nonnull
    @Override
    public String toString() {
        return getClass().getName() + "{" +
                "id=" + id +
                ", value=" + (value == null ? "null" : value.toString()) +
                '}';
    }
}
