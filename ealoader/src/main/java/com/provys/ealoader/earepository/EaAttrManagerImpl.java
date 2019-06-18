package com.provys.ealoader.earepository;

import com.provys.catalogue.api.Attr;
import com.provys.catalogue.api.AttrManager;
import com.provys.catalogue.api.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sparx.Attribute;
import org.sparx.Collection;
import org.sparx.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

class EaAttrManagerImpl implements EaAttrManager {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(EaAttrManagerImpl.class.getName());

    @Nonnull
    private final AttrManager attrManager;

    EaAttrManagerImpl(AttrManager attrManager) {
        this.attrManager = attrManager;
    }

    /**
     * Supports mapping of PROVYS attribute and EA attribute
     */
    private static class AttrUsage implements Comparable<AttrUsage> {
        @Nonnull
        private final Attr attr;

        @Nullable
        private Attribute attribute = null;

        AttrUsage(Attr attr) {
            this.attr = attr;
        }

        @Override
        public int compareTo(AttrUsage o) {
            return attr.compareTo(Objects.requireNonNull(o).attr);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttrUsage)) return false;

            AttrUsage attrUsage = (AttrUsage) o;

            if (!attr.equals(attrUsage.attr)) return false;
            return attribute != null ? attribute.equals(attrUsage.attribute) : attrUsage.attribute == null;
        }

        @Override
        public int hashCode() {
            int result = attr.hashCode();
            result = 31 * result + (attribute != null ? attribute.hashCode() : 0);
            return result;
        }
    }

    private void syncAttribute(Collection<Attribute> attributes, AttrUsage attrUsage, int pos) {
        boolean update = false;
        if (attrUsage.attribute == null) {
            LOG.info("Register new attribute {}.{}", () -> attrUsage.attr.getEntity().getNameNm(),
                    attrUsage.attr::getNameNm);
            attrUsage.attribute = Objects.requireNonNull(attributes.AddNew(attrUsage.attr.getName(), "type"));
            attrUsage.attribute.SetAlias(attrUsage.attr.getNameNm());
            attrUsage.attribute.SetVisibility("Public");
        }
        if (!attrUsage.attribute.GetName().equals(attrUsage.attr.getName())) {
            attrUsage.attribute.SetName(attrUsage.attr.getName());
            update = true;
        }
        var type = attrUsage.attr.getDomain().getNameNm() + attrUsage.attr.getSubdomainNm()
                .map(subdomainNm -> "(" + subdomainNm + ")")
                .orElse("");
        if (!attrUsage.attribute.GetType().equals(type)) {
            attrUsage.attribute.SetType(type);
            update = true;
        }
        if (attrUsage.attribute.GetPos() != pos) {
            attrUsage.attribute.SetPos(pos);
            update = true;
        }
        if (update) {
            attrUsage.attribute.Update();
        }
    }

    @Override
    public void syncForEntity(Entity entity, Element entityElement) {
        var attrMap = attrManager.getByEntityId(entity.getId())
                .stream()
                .map(AttrUsage::new)
                .collect(Collectors.toMap(attrUsage -> attrUsage.attr.getNameNm(), Function.identity()));
        var attributes = entityElement.GetAttributes();
        // first remove attributes that are not present in catalogue and map existing ones
        for (short index = 0; index < attributes.GetCount(); index++) {
            var attribute = attributes.GetAt(index);
            if (attrMap.get(attribute.GetAlias()) != null) {
                attrMap.get(attribute.GetAlias()).attribute = attribute;
            } else {
                LOG.info("Delete attribute {}", attribute::GetAlias);
                attributes.Delete(index);
            }
        }
        // now go through PROVYS attributes and register missing ones, synchronise all
        var attrs = attrMap.values().stream().sorted().collect(Collectors.toList());
        int ord = 0;
        for (var attrUsage : attrs) {
            syncAttribute(attributes, attrUsage, ord++);
        }
        for (var attribute : attributes) {
            attribute.destroy();
        }
        attributes.destroy();
    }
}
