package com.provys.ealoader.catalogue;

import com.provys.object.ProvysNmObject;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface Entity extends ProvysNmObject {

    /**
     * @return entity group (entity group with Id ENTITYGRP_ID)
     */
    @Nonnull
    Optional<EntityGrp> getEntityGrp();

}
