package com.provys.ealoader.catalogue.dbloader;

import com.provys.common.exception.InternalException;
import com.provys.ealoader.catalogue.impl.*;
import com.provys.ealoader.catalogue.db.tables.records.CatEntitygrpVwRecord;
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
    public EntityGrpProxy loadById(CatRepositoryImpl repository, BigInteger id) {
        List<EntityGrpProxy> result = new LoadRunner(repository.getEntityGrpManager(),
                CAT_ENTITYGRP_VW.ENTITYGRP_ID.eq(id)).
                run();
        if (result.size() != 1) {
            throw new InternalException(LOG, "Incorrect number of EntityGrp items loaded by id: " + result.size());
        }
        return result.get(0);
    }

    @Override
    public void loadValue(CatRepositoryImpl repository, EntityGrpProxy entityGrpProxy) {
        var result = loadById(repository, entityGrpProxy.getId());
        if (result != entityGrpProxy) {
            throw new InternalException(LOG, "Duplicate entityGrpProxy for id=" + entityGrpProxy.getId());
        }
    }

    @Nonnull
    public Optional<EntityGrpProxy> loadByNameNm(CatRepositoryImpl repository, String nameNm) {
        List<EntityGrpProxy> result = new LoadRunner(repository.getEntityGrpManager(),
                CAT_ENTITYGRP_VW.NAME_NM.eq(nameNm)).
                run();
        if (result.size() > 1) {
            throw new InternalException(LOG, "Multiple EntityGrps items loaded by name_nm: " + result.size());
        }
        return (result.size() == 1) ? Optional.of(result.get(0)) : Optional.empty();
    }

    @Nonnull
    @Override
    public Set<EntityGrpProxy> loadByParentId(CatRepositoryImpl repository, BigInteger parentId) {
        List<EntityGrpProxy> result = new LoadRunner(repository.getEntityGrpManager(),
                CAT_ENTITYGRP_VW.PARENT_ID.eq(parentId)).
                run();
        return new HashSet<> (result);
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

        private List<EntityGrpProxy> registerAll() {
            Objects.requireNonNull(entityGrpRecordById, "Load entity grp must be called before registerAll");
            List<EntityGrpProxy> result = new ArrayList<>(entityGrpRecordById.size());
            for (var entityGrpRecord : entityGrpRecordById.values()) {
                var entityGrpProxy = entityGrpManager.getOrAddById(entityGrpRecord.getEntitygrpId());
                entityGrpProxy.setValue(createEntityGrp(entityGrpRecord));
                if (!result.add(entityGrpProxy)) {
                    throw new InternalException(LOG, "Entity group found in set - unexpected duplicity");
                }
            }
            return result;
        }

        @Nonnull
        private EntityGrpValue createEntityGrp(CatEntitygrpVwRecord entityGrpRecord) {
            return new EntityGrpValue(entityGrpRecord.getEntitygrpId(),
                    (entityGrpRecord.getParentId() == null) ? null :
                            entityGrpManager.getOrAddById(entityGrpRecord.getParentId()),
                    entityGrpRecord.getNameNm(), entityGrpRecord.getName(), entityGrpRecord.getNote(),
                    entityGrpRecord.getOrd());
        }

        List<EntityGrpProxy> run() {
            select();
            return registerAll();
        }
    }
}
