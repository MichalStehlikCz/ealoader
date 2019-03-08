package com.provys.ealoader.earepository;

import com.provys.catalogue.api.EntityGrp;
import com.provys.catalogue.api.EntityGrpManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparx.Collection;
import org.sparx.Package;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class EaEntityGrpManagerImpl implements EaEntityGrpManager {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(EaEntityGrpManagerImpl.class.getName());

    @Nonnull
    private final EaRepository repository;
    @Nonnull
    private final EntityGrpManager entityGrpManager;
    @Nullable
    private Package dataModelPackage;
    @Nonnull
    private final Map<BigInteger, Package> packageById = new ConcurrentHashMap<>(10);

    EaEntityGrpManagerImpl(EaRepository repository, EntityGrpManager entityGrpManager) {
        this.repository = repository;
        this.entityGrpManager = entityGrpManager;
    }

    /**
     * Get child package with specified alias.
     *
     * @param parent is parent package
     * @param alias is required alias
     * @return package if one exists, empty optional if no such package is found
     */
    private Optional<Package> getChildByAlias(Package parent, String alias) {
        Collection<Package> children = parent.GetPackages();
        for (var child : children) {
            if (child.GetAlias().equals(alias)) {
                return Optional.of(child);
            }
        }
        return Optional.empty();
    }

    /**
     * @return package corresponding to root of data model (application PROVYS, path by aliases APPLAYER / DATAMODEL)
     */
    @Nonnull
    private Package getDataModelPackage() {
        if (dataModelPackage == null) {
            LOG.info("Find DATAMODEL package");
            Collection<Package> models = repository.getEaRepository().GetModels();
            Package model = models.GetByName("PROVYS");
            dataModelPackage = getChildByAlias(
                    getChildByAlias(model, "APPLAYER").
                            orElseThrow(() -> new RuntimeException("APPLAYER node not found")),
                    "DATAMODEL").
                    orElseThrow(() -> new RuntimeException("DATAMODEL node not found"));
        }
        return dataModelPackage;
    }

    private Package registerPackage(EntityGrp entityGrp) {
        Package parent = entityGrp.getParent().
                map(parentEG -> (packageById.get(parentEG.getId()) == null)
                        ? getPackage(parentEG) : packageById.get(parentEG.getId())).
                orElse(getDataModelPackage());
        Package result = getChildByAlias(parent, entityGrp.getNameNm()).
                map(pkg -> {
                    LOG.info("Found entity group {}", entityGrp::getNameNm);
                    return pkg;
                }).
                orElseGet(() -> {
                    LOG.info("Register new entity group {}", entityGrp::getNameNm);
                    Package newPackage = parent.GetPackages().AddNew(entityGrp.getName(), "Class");
                    newPackage.Update();
                    newPackage.SetAlias(entityGrp.getNameNm());
                    newPackage.Update();
                    return newPackage;
                });
        // set mapping for given entity group
        if (packageById.put(entityGrp.getId(), result) != null) {
            LOG.warn("Register invoked for entity group with already registered package {}", entityGrp::getNameNm);
        }
        return result;
    }

    @Nonnull
    public Package getPackage(EntityGrp entityGrp) {
        return getPackage(entityGrp.getId());
    }

    @Nonnull
    public Package getPackage(BigInteger entityGrpId) {
        Package eaPackage = packageById.get(Objects.requireNonNull(entityGrpId));
        if (eaPackage == null) {
            eaPackage = registerPackage(entityGrpManager.getById(entityGrpId));
        }
        return eaPackage;
    }

    public void syncPackage(EntityGrp entityGrp) {
        LOG.info("Synchronize package for entity group {}", entityGrp::getNameNm);
        Package result = getPackage(entityGrp);
        result.SetName(entityGrp.getName());
        result.SetAlias(entityGrp.getNameNm());
        result.SetNotes(entityGrp.getNote().orElse(null));
        result.SetTreePos(entityGrp.getOrd());
        result.Update();
    }

    private void indicateUnusedEntityGrpChildren(EntityGrp parent) {
        LOG.info("Find unused subpackages for entity group {}", parent::getNameNm);
        Package parentPackage = getPackage(parent);
        for (var childPackage : parentPackage.GetPackages()) {
            if ((childPackage.GetAlias() == null) || (childPackage.GetAlias().isEmpty())) {
                LOG.warn("Package {} with empty alias in entity group {}", childPackage::GetName,
                        parent::getNameNm);
            } else {
                EntityGrp entityGrp = entityGrpManager.getByNameNmIfExists(childPackage.GetAlias()).orElse(null);
                if (entityGrp == null) {
                    LOG.warn("Package {} does not correspond to any entity group", childPackage::GetAlias);
                } else if (entityGrp.getParent().orElse(null) != parent) {
                    LOG.warn("Entity group {} is not child of {}, package is", childPackage::GetAlias,
                            parent::getNameNm);
                }
            }
        }
    }

    @Override
    public void syncAllPackages() {
        // go through all entity groups and sync them; ordering is useful as this way we will process parents before
        // children
        SortedSet<EntityGrp> entityGrps = new TreeSet<>(entityGrpManager.getAll());
        for (var entityGrp : entityGrps) {
            syncPackage(entityGrp);
        }
        // we want to check if there are no unrecognized packages...
        for (var entityGrp : entityGrps) {
            indicateUnusedEntityGrpChildren(entityGrp);
        }
    }

}
