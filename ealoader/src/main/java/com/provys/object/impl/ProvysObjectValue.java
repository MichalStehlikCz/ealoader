package com.provys.object.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Value class holding values of properties of given object
 */
abstract public class ProvysObjectValue {

    @Nonnull
    private final BigInteger id;

    public ProvysObjectValue(BigInteger id) {
        this.id = Objects.requireNonNull(id);
    }

    @Nonnull
    public BigInteger getId() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof ProvysObjectValue)) return false;
        ProvysObjectValue that = (ProvysObjectValue) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
