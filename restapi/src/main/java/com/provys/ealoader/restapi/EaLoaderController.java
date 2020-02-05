package com.provys.ealoader.restapi;

import com.provys.ealoader.earepository.EaRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import java.util.Objects;

@RestController
@RequestMapping(value = "/ealoader", produces = MediaType.TEXT_PLAIN_VALUE)
public class EaLoaderController {

    private static final Logger LOG = LogManager.getLogger(EaLoaderController.class);

    @Nonnull
    private final EaRepository eaRepository;

    @Autowired
    public EaLoaderController(EaRepository eaRepository) {
        this.eaRepository = Objects.requireNonNull(eaRepository);
    }

    @ApiOperation(value = "Full Synchronization", notes = "Synchronize all entity groups and entities in EA with db")
    @GetMapping("/syncall")
    public String syncAll() {
        LOG.info("EaLoader SyncAll");
        eaRepository.getEaEntityGrpManager().syncAllPackages();
        eaRepository.getEaEntityManager().syncAllElements();
        LOG.info("EaLoader SyncAll finished");
        return "Synchronisation successful";
    }

    @ApiOperation(value = "Synchronize Entity Group", notes = "Synchronize single entity group in EA with DB")
    @GetMapping("/syncentitygrp/{entityGrpNm:[a-zA-Z][a-zA-Z_0-9]*}")
    public String syncEntityGrp(@PathVariable("entityGrpNm") @ApiParam("Internal name of entity group")
                                            String entityGrpNm) {
        LOG.info("EaLoader SyncEntityGrp {}", entityGrpNm);
        eaRepository.getEaEntityGrpManager().syncPackage(entityGrpNm);
        LOG.info("EaLoader SyncEntityGrp finished");
        return "Synchronisation successful";
    }

    @ApiOperation(value = "Synchronize Entity", notes = "Synchronize single entity in EA with DB")
    @GetMapping("/syncentity/{entityNm:[a-zA-Z][a-zA-Z_0-9]*}")
    public String syncEntity(@PathVariable("entityNm") @ApiParam("Internal name of entity")
                                        String entityNm) {
        LOG.info("EaLoader SyncEntity {}", entityNm);
        eaRepository.getEaEntityManager().syncElement(entityNm);
        LOG.info("EaLoader SyncEntity finished");
        return "Synchronisation successful";
    }
}
