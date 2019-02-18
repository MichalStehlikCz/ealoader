package com.provys.ealoader.catalogue.impl;

import com.provys.ealoader.catalogue.EntityGrp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

public class EntityGrpValue {

    @Nonnull
    private BigInteger id;
    @Nullable
    private EntityGrp parent;
    @Nonnull
    private String nameNm;
    @Nonnull
    private String name;
    @Nullable
    private String note;
    private int ord;

    EntityGrpValue(BigInteger id, @Nullable EntityGrp parent, String nameNm, String name, @Nullable String note,
                  int ord) {
        this.id = Objects.requireNonNull(id);
        this.parent = parent;
        this.nameNm = nameNm;
        this.name = name;
        this.note = note;
        this.ord = ord;
    }

    @Nonnull
    public BigInteger getId() {
        return id;
    }

    @Nonnull
    public Optional<EntityGrp> getParent() {
        return Optional.ofNullable(parent);
    }

    @Nonnull
    public String getNameNm() {
        return nameNm;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Optional<String> getNote() {
        return Optional.ofNullable(note);
    }

    public int getOrd() {
        return ord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityGrpImpl entityGrp = (EntityGrpImpl) o;
        return getOrd() == entityGrp.getOrd() &&
                Objects.equals(getId(), entityGrp.getId()) &&
                Objects.equals(getParent(), entityGrp.getParent()) &&
                Objects.equals(getNameNm(), entityGrp.getNameNm()) &&
                Objects.equals(getName(), entityGrp.getName()) &&
                Objects.equals(getNote(), entityGrp.getNote());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
