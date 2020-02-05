package com.provys.ealoader.earepository;

import com.provys.catalogue.api.Entity;
import com.provys.common.datatype.DtUid;
import org.sparx.Element;
import org.sparx.Package;

import javax.annotation.Nonnull;

public interface EaEntityManager {

     /**
     * @param entity is entity which we want to find
     * @return element corresponding to given entity; registers element if one is not created yet
     */
    @Nonnull
    Element getElement(Entity entity);

    /**
     * @param entityId is UID of entity which we want to find
     * @return element corresponding to given entity; register element if one is not created yet
     */
    @Nonnull
    Element getElement(DtUid entityId);

    /**
     * Synchronise element in Enterprise Architect with entity data
     *
     * @param entity is entity we want to synchronize
     */
    void syncElement(Entity entity);

    /**
     * Synchronise element in Enterprise Architect with entity data
     *
     * @param entityNm is internal name of entity we want to synchronize
     */
    void syncElement(String entityNm);

    /**
     * Synchronize all elements with their corresponding entities
     */
    void syncAllElements();

    /**
     * Method registers all elements of type DataObject in given package to corresponding entity. Goal is to prevent
     * creation of duplicate elements when entity is moved between entity groups
     */
    void mapElements(Package entityGrpPackage);
}
