package com.provys.ealoader.earepository;

import com.provys.ealoader.catalogue.EntityGrpManager;
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

    @Inject
    EaRepositoryImpl(EntityGrpManager entityGrpManager) {
        eaEntityGrpManager = new EaEntityGrpManagerImpl(this, entityGrpManager);
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
}
