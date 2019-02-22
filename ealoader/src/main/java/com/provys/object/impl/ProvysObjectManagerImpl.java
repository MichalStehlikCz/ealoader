package com.provys.object.impl;

import com.provys.common.exception.InternalException;
import com.provys.object.ProvysObject;
import com.provys.object.ProvysObjectManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

abstract public class ProvysObjectManagerImpl<R extends ProvysRepositoryImpl, V extends ProvysObjectValue,
        T extends ProvysObject, L extends ProvysObjectLoader<R, T>>
        implements ProvysObjectManager<T> {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(ProvysObjectManagerImpl.class);

    @Nonnull
    private final R repository;
    @Nonnull
    private final L loader;

    @Nonnull
    private final Map<BigInteger, T> provysObjectById = new ConcurrentHashMap<>(100);

    ProvysObjectManagerImpl(R repository, L loader) {
        this.repository = Objects.requireNonNull(repository);
        this.loader = Objects.requireNonNull(loader);
    }

    @Nonnull
    @Override
    public T getById(BigInteger id) {
        T provysObject = provysObjectById.get(Objects.requireNonNull(id));
        if (provysObject == null) {
            provysObject = loader.loadById(repository, id);
        }
        return provysObject;
    }

    @Nonnull
    @Override
    public Collection<T> getAll() {
        loader.loadAll(repository);
        return Collections.unmodifiableCollection(provysObjectById.values());
    }

    abstract protected T getNewProxy(BigInteger id);

    /**
     * Retrieve entity group if already loaded to cache, otherwise create new proxy for given id. Should only be called
     * internally as method does not verify existence of given object in database.
     *
     * @param id is Id of entity group being looked for
     * @return entity group present in cache or newly added proxy
     */
    public T getOrAddById(BigInteger id) {
        return provysObjectById.computeIfAbsent(Objects.requireNonNull(id), this::getNewProxy);
    }

    /**
     * Register given object in indices. Verifies that object proxy has been previously registered for its id, if not,
     * throws exception
     *
     * @param provysObject is proxy to object to be registered
     * @param oldValue are old values associated with object
     * @param newValue are new values associated with object
     */
    void registerChange(T provysObject, @Nullable V oldValue, @Nullable V newValue) {
        if (provysObjectById.get(provysObject.getId()) != provysObject) {
            throw new InternalException(LOG, "Register change called on unregistered object proxy");
        }
    }

    /**
     * Remove given object. Used as reaction to delete. Note that even if references are removed from indices,
     * there might still be objects that retain reference and thus might stumble across invalid object proxy
     */
    void unregister(T provysObject, @Nullable V oldValue) {
        // remove from additional indices
        if (oldValue != null) {
            registerChange(provysObject, oldValue, null);
        }
        // remove from primary index
        var oldProvysObject = provysObjectById.remove(provysObject.getId());
        if ((oldProvysObject != null) && (oldProvysObject != provysObject)) {
            // if different object has been registered here, return it back
            provysObjectById.putIfAbsent(oldProvysObject.getId(), oldProvysObject);
        }
    }
}
