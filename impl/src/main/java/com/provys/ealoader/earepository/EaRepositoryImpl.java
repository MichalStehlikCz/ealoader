package com.provys.ealoader.earepository;

import com.provys.catalogue.api.CatalogueRepository;
import com.provys.common.exception.RegularException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparx.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class EaRepositoryImpl implements EaRepository {

    private static final Logger LOG = LogManager.getLogger(EaRepositoryImpl.class);

    @Nonnull
    private final Repository eaRepository;
    @Nonnull
    private final EaEntityGrpManagerImpl eaEntityGrpManager;
    @Nonnull
    private final EaEntityManagerImpl eaEntityManager;
    @Nonnull
    private final EaAttrManagerImpl eaAttrManager;

    @Autowired
    EaRepositoryImpl(EaLoaderConfiguration configuration, CatalogueRepository catRepository) {
        eaRepository = new Repository();
        String eaAddress = configuration.getAddress();
        String eaUser = configuration.getUser();
        // Attempt to open the provided file
        LOG.debug("Open Enterprise Architect repository {}", eaAddress);
        if (eaUser.isEmpty()) {
            if (!eaRepository.OpenFile(eaAddress)) {
                // If the file couldn't be opened then notify the user
                throw new RegularException("EALOADER_CANNOTOPENREPOSITORY",
                        "Enterprise Architect was unable to open the file '" + eaAddress + '\'');
            }
        } else {
            if (!eaRepository.OpenFile2(eaAddress, eaUser, configuration.getPwd())) {
                // If the file couldn't be opened then notify the user
                throw new RegularException("EALOADER_CANNOTOPENREPOSITORY",
                        "Enterprise Architect was unable to open the file '" + eaAddress + "', user " + eaUser);
            }
        }
        LOG.info("Enterprise architect repository {} opened", eaAddress);
        eaEntityGrpManager = new EaEntityGrpManagerImpl(this, catRepository);
        eaEntityManager = new EaEntityManagerImpl(this, catRepository.getEntityManager());
        eaAttrManager = new EaAttrManagerImpl(catRepository.getAttrManager());
    }

    @Nonnull
    @Override
    public Repository getEaRepository() {
        return eaRepository;
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

    @Nonnull
    @Override
    public EaAttrManager getEaAttrManager() {
        return eaAttrManager;
    }
}
