package com.provys.ealoader.executor;

import com.provys.ealoader.catalogue.EntityGrpManager;
import com.provys.ealoader.earepository.EaRepository;
import org.jooq.DSLContext;
import org.sparx.Repository;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class EALoader {

    @Nonnull
    private final EntityGrpManager entityGrpManager;
    @Nonnull
    private final EaRepository eaRepository;

    @Inject
    EALoader(EntityGrpManager entityGrpManager, EaRepository eaRepository) {
        this.entityGrpManager = Objects.requireNonNull(entityGrpManager);
        this.eaRepository = Objects.requireNonNull(eaRepository);
    }

    public void run(DSLContext dslContext, Repository eaRepository) {
        this.eaRepository.setEaRepository(eaRepository);
        entityGrpManager.load(dslContext);
        this.eaRepository.getEaEntityGrpManager().syncAllPackages();
    }
}
