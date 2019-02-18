package com.provys.ealoader.catalogue.dbloader;

import com.provys.common.exception.InternalException;
import com.provys.ealoader.catalogue.impl.EntityGrpImpl;
import com.provys.ealoader.catalogue.impl.EntityGrpManagerImpl;
import com.provys.ealoader.catalogue.db.tables.records.CatEntitygrpVwRecord;
import com.provys.ealoader.catalogue.impl.CatRepositoryImpl;
import com.provys.ealoader.catalogue.impl.EntityGrpLoader;
import com.provys.provysdb.ProvysDBContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Condition;
import org.jooq.impl.DSL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigInteger;
import java.util.*;

import static com.provys.ealoader.catalogue.db.Tables.CAT_ENTITYGRP_VW;

@ApplicationScoped
public class EntityGrpDbLoader implements EntityGrpLoader {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(EntityGrpDbLoader.class);

    @Nonnull
    private final ProvysDBContext dbContext;

    @Inject
    EntityGrpDbLoader(ProvysDBContext dbContext) {
        this.dbContext = Objects.requireNonNull(dbContext);
    }

    @Nonnull
    @Override
    public EntityGrpImpl loadById(CatRepositoryImpl repository, BigInteger id) {
        List<EntityGrpImpl> result = new LoadRunner(repository.getEntityGrpManager(),
                CAT_ENTITYGRP_VW.ENTITYGRP_ID.eq(id)).
                run();
        if (result.size() != 1) {
            throw new InternalException(LOG, "Incorrect number of EntityGrp items loaded by id: " + result.size());
        }
        return result.get(0);
    }

    @Nonnull
    public Optional<EntityGrpImpl> loadByNameNm(CatRepositoryImpl repository, String nameNm) {
        List<EntityGrpImpl> result = new LoadRunner(repository.getEntityGrpManager(),
                CAT_ENTITYGRP_VW.NAME_NM.eq(nameNm)).
                run();
        if (result.size() > 1) {
            throw new InternalException(LOG, "Multiple EntityGrps items loaded by name_nm: " + result.size());
        }
        return (result.size() == 1) ? Optional.of(result.get(0)) : Optional.empty();
    }

    public void loadAll(CatRepositoryImpl repository) {
        new LoadRunner(repository.getEntityGrpManager()).
                run();
    }

    private class LoadRunner{
        @Nonnull
        private final EntityGrpManagerImpl entityGrpManager;
        @Nullable
        private final Condition condition;
        @Nullable
        private Map<BigInteger, CatEntitygrpVwRecord> entityGrpRecordById;

        LoadRunner(EntityGrpManagerImpl entityGrpManager, @Nullable Condition condition) {
            this.entityGrpManager = entityGrpManager;
            this.condition = condition;
        }

        LoadRunner(EntityGrpManagerImpl entityGrpManager) {
            this(entityGrpManager, null);
        }

        private void select() {
            try (var dsl = dbContext.createDSL()) {
                entityGrpRecordById = dsl.selectFrom(CAT_ENTITYGRP_VW).
                        where(condition == null ? DSL.noCondition() : condition).
                        fetch().
                        intoMap(CAT_ENTITYGRP_VW.ENTITYGRP_ID);
            }
        }

        private List<EntityGrpImpl> registerAll() {
            Objects.requireNonNull(entityGrpRecordById, "Load entity grp must be called before registerAll");
            List<EntityGrpImpl> result = new ArrayList<>(entityGrpRecordById.size());
            for (var entityGrpRecord : entityGrpRecordById.values()) {
                result.add(entityGrpManager.getByIdIfPresent(entityGrpRecord.getEntitygrpId()).
                                orElse(entityGrpManager.register(createEntityGrp(entityGrpRecord))));
            }
            return result;
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
            return entityGrpManager.getByIdIfPresent(id).
                    orElse(entityGrpManager.register(createEntityGrp(entityGrpRecord)));
        }

        List<EntityGrpImpl> run() {
            select();
            return registerAll();
        }
    }
}
