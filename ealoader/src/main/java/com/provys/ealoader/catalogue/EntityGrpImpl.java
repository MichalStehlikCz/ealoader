package com.provys.ealoader.catalogue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;

class EntityGrpImpl implements EntityGrp {

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
    @Nonnull
    private final SortedSet<EntityGrp> children = new TreeSet<>();

    EntityGrpImpl(BigInteger id, @Nullable EntityGrpImpl parent, String nameNm, String name, @Nullable String note,
                  int ord) {
        this.id = Objects.requireNonNull(id);
        this.parent = parent;
        this.nameNm = nameNm;
        this.name = name;
        this.note = note;
        this.ord = ord;
        if (this.parent != null) {
            parent.registerChild(this);
        }
    }

    @Nonnull
    @Override
    public BigInteger getId() {
        return id;
    }

    @Nonnull
    @Override
    public Optional<EntityGrp> getParent() {
        return Optional.ofNullable(parent);
    }

    @Nonnull
    @Override
    public String getNameNm() {
        return nameNm;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public Optional<String> getNote() {
        return Optional.ofNullable(note);
    }

    @Override
    public int getOrd() {
        return ord;
    }

    @Nonnull
    @Override
    public SortedSet<EntityGrp> getChildren() {
        return Collections.unmodifiableSortedSet(children);
    }

    private void registerChild(EntityGrp child) {
        children.add(child);
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

    @Nonnull
    @Override
    public List<Integer> getFullOrd() {
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
    @Override
    public int compareTo(EntityGrp other) {
        if (other == this) {
            return 0;
        }
        if (other.getParent() == getParent()) {
            // most common comparison - comparing two children of the same parent
            return Integer.compare(getOrd(), other.getOrd());
        }
        // calculate full path from root and compare paths...
        List<Integer> myFullOrd = getFullOrd();
        List<Integer> otherFullOrd = other.getFullOrd();
        for (int i = 0; i < myFullOrd.size(); i++) {
            if (i >= otherFullOrd.size()) {
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
}
