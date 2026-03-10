package com.factionrelationships;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.characters.PersonAPI;
import lunalib.lunaSettings.LunaSettings;

/**
 * Listens for faction relationship changes and records them for the overlay.
 * Registered as a transient listener in onGameLoad so it is not saved with the game.
 */
public class FactionRelationshipChangeListener extends BaseCampaignEventListener {

    private static final String MOD_ID = "factionrelationships";
    private static final String AUTO_SHOW_SETTING = "autoShowOverlayOnRelationshipChange";
    private static final String DISPLAY_SECONDS_SETTING = "relationshipChangeDisplaySeconds";
    private static final int DEFAULT_DISPLAY_SECONDS = 30;
    private static final int MIN_DISPLAY_SECONDS = 5;
    private static final int MAX_DISPLAY_SECONDS = 120;

    public FactionRelationshipChangeListener() {
        super(false);
    }

    private static int getDisplayDurationSeconds() {
        if (!Global.getSettings().getModManager().isModEnabled("lunalib")) {
            return DEFAULT_DISPLAY_SECONDS;
        }
        Integer v = LunaSettings.getInt(MOD_ID, DISPLAY_SECONDS_SETTING);
        if (v == null) {
            return DEFAULT_DISPLAY_SECONDS;
        }
        int sec = v.intValue();
        if (sec < MIN_DISPLAY_SECONDS) sec = MIN_DISPLAY_SECONDS;
        if (sec > MAX_DISPLAY_SECONDS) sec = MAX_DISPLAY_SECONDS;
        return sec;
    }

    @Override
    public void reportPlayerReputationChange(String factionId, float delta) {
        long durationMs = getDisplayDurationSeconds() * 1000L;
        RelationshipChangeStore.record(factionId, delta, durationMs);
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            Boolean autoShow = LunaSettings.getBoolean(MOD_ID, AUTO_SHOW_SETTING);
            if (Boolean.TRUE.equals(autoShow)) {
                FactionRelationshipsPlugin.setOverlayVisible(true);
                RelationshipChangeStore.setAutoShowExpiry(System.currentTimeMillis() + durationMs);
            }
        }
    }

    @Override
    public void reportPlayerReputationChange(PersonAPI person, float delta) {
        if (person == null || person.getFaction() == null) {
            return;
        }
        reportPlayerReputationChange(person.getFaction().getId(), delta);
    }
}
