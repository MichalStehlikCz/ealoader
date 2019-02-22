package com.provys.ealoader.catalogue.impl;

import com.provys.ealoader.catalogue.EntityGrp;
import com.provys.object.impl.ProvysNmObjectValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Value object representing entity group.
 */
public class EntityGrpValue extends ProvysNmObjectValue {

    @Nullable
    private final EntityGrp parent;
    @Nonnull
    private final String name;
    @Nullable
    private final String note;
    private final int ord;

    public EntityGrpValue(BigInteger id, @Nullable EntityGrp parent, String nameNm, String name, @Nullable String note,
                  int ord) {
        super(id, nameNm);
        this.parent = parent;
        this.name = Objects.requireNonNull(name);
        this.note = note;
        this.ord = ord;
    }

    @Nonnull
    Optional<EntityGrp> getParent() {
        return Optional.ofNullable(parent);
    }

    @Nonnull
    String getName() {
        return name;
    }

    @Nonnull
    Optional<String> getNote() {
        return Optional.ofNullable(note);
    }

    int getOrd() {
        return ord;
    }

    @Nonnull
    List<Integer> getFullOrd() {
        var result = getParent().map(EntityGrp::getFullOrd).orElse(new ArrayList<>(5));
        result.add(getOrd());
        return result;
    }

    /**
     * Compares entity groups by their order within the same parent and by their parents starting from root. Parent is
     * considered before its children.
     * Note that based on server constraints, ord should be unique within parent (or within entity groups without
     * parent) and thus comparison equality should be equal to object equality, even though it is not enforced by
     * this class and is left on database and loader properly loading data from database.
     *
     * @param other is other entity group to be compared with
     * @return -1 if full ordering of this is before other, 0 if both objects are the same and 1 if this object is after
     * other
     */
    int compareTo(EntityGrp other) {
        if (other.getParent() == getParent()) {
            // most common comparison - comparing two children of the same parent
            return Integer.compare(getOrd(), other.getOrd());
        }
        // calculate full path from root and compare paths...
        List<Integer> myFullOrd = getFullOrd();
        List<Integer> otherFullOrd = other.getFullOrd();
        for (int i = 0; i < myFullOrd.size(); i++) {
            if (i > otherFullOrd.size()) {
                // same start but other is shorter -> other is before this (parent before child)
                return 1;
            }
            if (myFullOrd.get(i) < otherFullOrd.get(i)) {
                return -1;
            } else if (myFullOrd.get(i) > otherFullOrd.get(i)) {
                return 1;
            }
        }
        if (myFullOrd.size() < otherFullOrd.size()) {
            // start same but this is shorter -> this is before other (parent before child)
            return -1;
        }
        // both objects are the same
        return 0;
    }

    @Override
    @SuppressWarnings("squid:S1206") // using Id as hash code is sufficient, no need to add additional fields
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EntityGrpValue entityGrp = (EntityGrpValue) o;
        return getOrd() == entityGrp.getOrd() &&
                Objects.equals(getParent(), entityGrp.getParent()) &&
                Objects.equals(getName(), entityGrp.getName()) &&
                Objects.equals(getNote(), entityGrp.getNote());
    }

    @Override
    public String toString() {
        return "EntityGrpValue{" +
                "id=" + getId() +
                ", parent=" + (parent == null ? "null" : parent) +
                ", nameNm=\"" + getNameNm() + '"' +
                ", name=\"" + name + '"' +
                ", note=" + (note == null ? "null" : '"' + note + '"') +
                ", ord=" + ord +
                '}';
    }
}
