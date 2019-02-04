package com.provys.ealoader.executor;

import com.provys.ealoader.catalogue.EntityGrpRepository;
import org.jooq.DSLContext;
import org.sparx.Repository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class EALoader {

    private final EntityGrpRepository entityGrpRepository;

    @Inject
    EALoader(EntityGrpRepository entityGrpRepository) {
        this.entityGrpRepository = Objects.requireNonNull(entityGrpRepository);
    }

    public void run(DSLContext dslContext, Repository eaRepository) {
        entityGrpRepository.load(dslContext);
        var executor = new EALoadExecutor(eaRepository, entityGrpRepository);
        executor.run();
    }
}
