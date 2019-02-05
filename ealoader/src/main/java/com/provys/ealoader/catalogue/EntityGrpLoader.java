package com.provys.ealoader.catalogue;

import com.provys.ealoader.catalogue.db.tables.records.CatEntitygrpVwRecord;
import org.jooq.DSLContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static com.provys.ealoader.catalogue.db.Tables.CAT_ENTITYGRP_VW;

class EntityGrpLoader {

    @Nonnull
    private final EntityGrpManagerImpl repository;
    @Nullable
    private Map<BigInteger, CatEntitygrpVwRecord> entityGrpRecordById;

    EntityGrpLoader(EntityGrpManagerImpl repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    private void select(DSLContext dslContext) {
        entityGrpRecordById = dslContext.selectFrom(CAT_ENTITYGRP_VW).
                fetch().
                intoMap(CAT_ENTITYGRP_VW.ENTITYGRP_ID);
    }

    private void registerAll() {
        Objects.requireNonNull(entityGrpRecordById, "Load entity grp must be called before registerAll");
        for (var entityGrpRecord : entityGrpRecordById.values()) {
            repository.getByIdOrRegister(entityGrpRecord.getEntitygrpId(), () -> createEntityGrp(entityGrpRecord));
        }
    }

    @Nonnull
    private EntityGrpImpl createEntityGrp(CatEntitygrpVwRecord entityGrpRecord) {
        return new EntityGrpImpl(entityGrpRecord.getEntitygrpId(),
                (entityGrpRecord.getParentId() == null) ? null : get(entityGrpRecord.getParentId()),
                entityGrpRecord.getNameNm(), entityGrpRecord.getName(), entityGrpRecord.getNote(),
                entityGrpRecord.getOrd());
    }

    @Nonnull
    private EntityGrpImpl get(BigInteger id) {
        Objects.requireNonNull(entityGrpRecordById, "Load entity grp must be called before get");
        var entityGrpRecord = entityGrpRecordById.get(Objects.requireNonNull(id));
        return repository.getByIdOrRegister(id, () -> createEntityGrp(entityGrpRecord));
    }

    void run(DSLContext dslContext) {
        select(dslContext);
        registerAll();
    }
}
