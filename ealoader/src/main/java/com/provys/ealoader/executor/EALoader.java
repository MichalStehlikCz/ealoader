package com.provys.ealoader.executor;

import com.provys.ealoader.earepository.EaRepository;
import org.sparx.Repository;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Objects;

@ApplicationScoped
public class EALoader {

    @Nonnull
    private final EaRepository eaRepository;

    @SuppressWarnings("CdiUnproxyableBeanTypesInspection")
    @Inject
    EALoader(EaRepository eaRepository) {
        this.eaRepository = Objects.requireNonNull(eaRepository);
    }

    public void run(Repository eaRepository) {
        this.eaRepository.setEaRepository(eaRepository);
        this.eaRepository.getEaEntityGrpManager().syncAllPackages();
        this.eaRepository.getEaEntityManager().syncAllElements();
    }
}
