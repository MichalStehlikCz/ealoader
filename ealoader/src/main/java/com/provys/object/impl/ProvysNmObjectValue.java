package com.provys.object.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Objects;

abstract public class ProvysNmObjectValue extends ProvysObjectValue {

    @Nonnull
    private final String nameNm;

    public ProvysNmObjectValue(BigInteger id, String nameNm) {
        super(id);
        this.nameNm = Objects.requireNonNull(nameNm);
    }

    @Nonnull
    public String getNameNm() {
        return nameNm;
    }

    @Override
    @SuppressWarnings("squid:S1206") // using Id as hash code is sufficient, no need to add additional fields
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof ProvysNmObjectValue)) return false;
        if (!super.equals(o)) return false;
        ProvysNmObjectValue that = (ProvysNmObjectValue) o;
        return Objects.equals(getNameNm(), that.getNameNm());
    }

}
