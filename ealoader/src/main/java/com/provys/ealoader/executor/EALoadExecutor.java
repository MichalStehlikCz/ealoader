package com.provys.ealoader.executor;

import com.provys.ealoader.catalogue.EntityGrp;
import com.provys.ealoader.catalogue.EntityGrpRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparx.Collection;
import org.sparx.Package;
import org.sparx.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class EALoadExecutor {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(EALoadExecutor.class.getName());

    @Nonnull
    private final Repository eaRepository;
    @Nonnull
    private final EntityGrpRepository entityGrpRepository;
    @Nonnull
    private final Map<BigInteger, Package> entityGrpEaMapping = new HashMap<>(10);
    @Nullable
    private Package eaDataModelPackage = null;

    EALoadExecutor(Repository eaRepository, EntityGrpRepository entityGrpRepository) {
        this.eaRepository = Objects.requireNonNull(eaRepository);
        this.entityGrpRepository = Objects.requireNonNull(entityGrpRepository);
    }

    private void processEntityGrps() {
        for (var entityGrp : entityGrpRepository.getAll()) {
            if (!entityGrpEaMapping.containsKey(entityGrp.getId())) {
                entityGrpEaMapping.put(entityGrp.getId(), processEntityGrp(entityGrp));
            }
        }
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

    @Nonnull
    private Package getDataModelPackage() {
        if (eaDataModelPackage == null) {
            LOG.info("Find DATAMODEL package");
            Collection<Package> models = eaRepository.GetModels();
            Package model = models.GetByName("PROVYS");
            eaDataModelPackage = getChildByAlias(
                    getChildByAlias(model, "APPLAYER").
                            orElseThrow(() -> new RuntimeException("APPLAYER node not found")),
                    "DATAMODEL").
                    orElseThrow(() -> new RuntimeException("DATAMODEL node not found"));
        }
        return eaDataModelPackage;
    }

    @Nonnull
    private Package processEntityGrp(EntityGrp entityGrp) {
        LOG.info("Process entity group {}", entityGrp::getNameNm);
        Package parent = entityGrp.getParent().
                map(parentEG -> entityGrpEaMapping.computeIfAbsent(parentEG.getId(), this::processEntityGrp)).
                orElse(getDataModelPackage());
        Package result;
        result = getChildByAlias(parent, entityGrp.getNameNm()).
                orElse(parent.GetPackages().AddNew(entityGrp.getName(), "Class"));
        result.SetName(entityGrp.getName());
        result.SetNotes(entityGrp.getNote().orElse(null));
        result.Update();
        return result;
    }

    @Nonnull
    private Package processEntityGrp(BigInteger entityGrpId) {
        EntityGrp entityGrp = entityGrpRepository.getById(entityGrpId).
                orElseThrow(() -> new IllegalArgumentException("Entity group not found by Id " + entityGrpId));
        return processEntityGrp(entityGrp);
    }

    void run() {
        processEntityGrps();
    }
}
