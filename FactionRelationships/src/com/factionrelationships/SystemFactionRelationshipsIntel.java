package com.factionrelationships;

import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;

/**
 * Stub for save compatibility only. Old saves may reference this class in the
 * IntelManager; this allows them to load. The intel ends immediately so it
 * does not appear in the intel screen.
 */
public class SystemFactionRelationshipsIntel extends BaseIntelPlugin {

    public SystemFactionRelationshipsIntel() {
        super();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);
        endImmediately();
    }

    @Override
    public String getSmallDescriptionTitle() {
        return "Faction Relationships (legacy)";
    }
}
