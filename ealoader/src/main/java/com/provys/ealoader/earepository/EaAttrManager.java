package com.provys.ealoader.earepository;

import com.provys.catalogue.api.Entity;
import org.sparx.Element;

/**
 * Class enables synchronisation of attributes in Enterprise Architect data model with PROVYS metadata catalogue. It
 * only does synchronisation on entity-by-entity basis as it doesn't make sense to keep attributes loaded in memory
 */
public interface EaAttrManager {

    /**
     * Synchronize all attributes in given entity. Entity should be mapped to supplied element.
     */
    void syncForEntity(Entity entity, Element entityElement);

}
