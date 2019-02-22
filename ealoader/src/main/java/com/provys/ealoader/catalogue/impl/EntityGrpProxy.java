package com.provys.ealoader.catalogue.impl;

import com.provys.common.exception.InternalException;
import com.provys.ealoader.catalogue.Entity;
import com.provys.ealoader.catalogue.EntityGrp;
import com.provys.object.impl.ProvysNmObjectProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;

/**
 *  Implements {@code EntityGrp} interface. Represents PROVYS ENTITYGRP object with specified Id. Implements lazy
 *  loading and lazy indexing to indices in entity group manager, uses entity group manager's loader to load values.
 */
public class EntityGrpProxy extends ProvysNmObjectProxy<CatRepositoryImpl, EntityGrpValue> implements EntityGrp {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(EntityGrpProxy.class);

    @Nonnull
    private final CatRepositoryImpl repository;
    @Nullable
    private EntityGrpValue value;

    EntityGrpProxy(CatRepositoryImpl repository, BigInteger id) {
        super(repository, id);
        this.repository = Objects.requireNonNull(repository);
    }

    public synchronized void setValue(EntityGrpValue value) {
        var oldValue = this.value;
        this.value = value;
        repository.getEntityGrpManager().registerChange(this, oldValue, value);
    }

    protected void loadValue() {
        repository.getEntityGrpManager().getLoader().loadValue(repository, this);
    }

    @Nonnull
    private EntityGrpValue validateValue() {
        if (value == null) {
            loadValue();
            if (value == null) {
                throw new InternalException(LOG, "Load entity grp failed - value empty");
            }
        }
        return value;
    }

    @Nonnull
    @Override
    public Optional<EntityGrp> getParent() {
        return validateValue().getParent();
    }

    @Nonnull
    @Override
    public String getNameNm() {
        return validateValue().getNameNm();
    }

    @Nonnull
    @Override
    public String getName() {
        return validateValue().getName();
    }

    @Nonnull
    @Override
    public Optional<String> getNote() {
        return validateValue().getNote();
    }

    @Override
    public int getOrd() {
        return validateValue().getOrd();
    }

    @Nonnull
    @Override
    public Collection<EntityGrp> getChildren() {
        return repository.getEntityGrpManager().getByParentId(getId());
    }

    @Nonnull
    @Override
    public Collection<Entity> getEntities() {
        return repository.getEntityManager().getByEntityGrpId(getId());
    }

    @Nonnull
    @Override
    public List<Integer> getFullOrd() {
        return validateValue().getFullOrd();
    }

    @Override
    public int compareTo(EntityGrp other) {
        if (this == other) {
            return 0;
        }
        return validateValue().compareTo(other);
    }

    /**
     * If two instances have same Id, they represent same entity group and thus are considered to be the same
     *
     * @param other is other object this is being compared to
     * @return true if this and other are entity group proxies with same Id, false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        EntityGrpProxy entityGrp = (EntityGrpProxy) other;
        return getId().equals(entityGrp.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Nonnull
    @Override
    public String toString() {
        return "EntityGrpProxy{" +
                "id=" + id +
                ", value=" + (value == null ? "null" : "EntityGrpValue{nameNm = \"" + value.getNameNm() + "\"}") +
                '}';
    }
}
