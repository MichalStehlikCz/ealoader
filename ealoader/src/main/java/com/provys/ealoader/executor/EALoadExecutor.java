package com.provys.ealoader.executor;

import com.provys.ealoader.catalogue.db.tables.records.CatEntitygrpVwRecord;
import org.jooq.DSLContext;
import org.sparx.Repository;

import java.math.BigInteger;
import java.util.Map;

import static com.provys.ealoader.catalogue.db.Tables.CAT_ENTITYGRP_VW;

class EALoadExecutor {

    private final DSLContext dslContext;
    private final Repository eaRepository;
    private Map<BigInteger, CatEntitygrpVwRecord> entityGrps;

    EALoadExecutor(DSLContext dslContext, Repository eaRepository) {
        this.dslContext = dslContext;
        this.eaRepository = eaRepository;
    }

    private void loadEntityGrps() {
        entityGrps = dslContext.selectFrom(CAT_ENTITYGRP_VW).
                fetch().
                intoMap(CAT_ENTITYGRP_VW.ENTITYGRP_ID);
    }

    private void processEntityGrps() {
        for (var entityGrp : entityGrps.values()) {
            processEntityGrp(entityGrp);
        }
    }

    private void processEntityGrp(CatEntitygrpVwRecord entityGrp) {
        System.out.println(entityGrp.getNameNm());
    }

    void run() {
        loadEntityGrps();
        processEntityGrps();
    }
}
