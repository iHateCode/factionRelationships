package com.factionrelationships;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;

public class FactionRelationshipsPlugin extends BaseModPlugin {

    private static Logger log;

    @Override
    public void onApplicationLoad() throws Exception {
        log = Global.getLogger(FactionRelationshipsPlugin.class);
        log.info("Faction Relationships mod loaded.");
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().getListenerManager().addListener(new FactionRelationshipsUIRenderer(), true);
        if (log != null) {
            log.info("Faction Relationships UI renderer registered.");
        }
    }
}
