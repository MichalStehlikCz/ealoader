package com.provys.ealoader.earepository;

import com.provys.catalogue.api.CatalogueRepository;
import org.sparx.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EaRepositoryImpl implements EaRepository {

    @Nullable
    private Repository eaRepository;
    @Nonnull
    private final EaEntityGrpManagerImpl eaEntityGrpManager;
    @Nonnull
    private final EaEntityManagerImpl eaEntityManager;

    @SuppressWarnings({"CdiUnproxyableBeanTypesInspection", "CdiInjectionPointsInspection"})
    @Inject
    EaRepositoryImpl(CatalogueRepository catRepository) {
        eaEntityGrpManager = new EaEntityGrpManagerImpl(this, catRepository);
        eaEntityManager = new EaEntityManagerImpl(this, catRepository.getEntityManager());
    }

    @Nonnull
    @Override
    public Repository getEaRepository() {
        if (eaRepository == null) {
            throw new IllegalStateException("Enterprise Architect repository is not set yet");
        }
        return eaRepository;
    }

    @Override
    public void setEaRepository(Repository eaRepository) {
        this.eaRepository = eaRepository;
    }

    @Nonnull
    @Override
    public EaEntityGrpManager getEaEntityGrpManager() {
        return eaEntityGrpManager;
    }

    @Nonnull
    @Override
    public EaEntityManager getEaEntityManager() {
        return eaEntityManager;
    }
}
