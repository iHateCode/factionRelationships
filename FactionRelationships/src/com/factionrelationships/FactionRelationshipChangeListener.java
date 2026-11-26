package com.factionrelationships;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import lunalib.lunaSettings.LunaSettings;

/**
 * Listens for faction relationship changes and records them for the overlay.
 * Registered as a transient listener in onGameLoad so it is not saved with the game.
 */
public class FactionRelationshipChangeListener extends BaseCampaignEventListener {

    private static final String AUTO_SHOW_SETTING = "autoShowOverlayOnRelationshipChange";
    private static final String DISPLAY_SECONDS_SETTING = "relationshipChangeDisplaySeconds";
    private static final int DEFAULT_DISPLAY_SECONDS = 30;
    private static final int MIN_DISPLAY_SECONDS = 5;
    private static final int MAX_DISPLAY_SECONDS = 120;

    public FactionRelationshipChangeListener() {
        super(false);
    }

    private static int getDisplayDurationSeconds() {
        if (!FactionRelationshipsPlugin.isLunaLibEnabled()) {
            return DEFAULT_DISPLAY_SECONDS;
        }
        Integer v = LunaSettings.getInt(FactionRelationshipsPlugin.MOD_ID, DISPLAY_SECONDS_SETTING);
        if (v == null) {
            return DEFAULT_DISPLAY_SECONDS;
        }
        int sec = v.intValue();
        if (sec < MIN_DISPLAY_SECONDS) sec = MIN_DISPLAY_SECONDS;
        if (sec > MAX_DISPLAY_SECONDS) sec = MAX_DISPLAY_SECONDS;
        return sec;
    }

    public static void recordRelationshipChange(String factionId, float delta) {
        long durationMs = getDisplayDurationSeconds() * 1000L;
        RelationshipChangeStore.record(factionId, delta, durationMs);
        if (FactionRelationshipsPlugin.isLunaLibEnabled()) {
            Boolean autoShow = LunaSettings.getBoolean(FactionRelationshipsPlugin.MOD_ID, AUTO_SHOW_SETTING);
            if (Boolean.TRUE.equals(autoShow)) {
                // An overlay the player opened, with no hide time pending, is theirs:
                // leave it open rather than starting a countdown that closes it.
                long pendingExpiry = RelationshipChangeStore.getAutoShowExpiry();
                if (!FactionRelationshipsPlugin.isOverlayVisible() || pendingExpiry > 0) {
                    FactionRelationshipsPlugin.setOverlayVisible(true);
                    int autoHideSec = FactionRelationshipsPlugin.getAutoHideOverlayAfterSeconds();
                    long hideAfterMs = (autoHideSec > 0) ? (autoHideSec * 1000L) : durationMs;
                    long expiry = System.currentTimeMillis() + hideAfterMs;
                    RelationshipChangeStore.setAutoShowExpiry(Math.max(pendingExpiry, expiry));
                }
            }
        }
    }

    // reportPlayerReputationChange(PersonAPI, float) is deliberately not overridden.
    // The game sends it when only that person's standing changed, so treating it as
    // a change for their faction would show a faction change that never happened.

    @Override
    public void reportPlayerReputationChange(String factionId, float delta) {
        recordRelationshipChange(factionId, delta);
        // The game reports after applying the change, so the timer's baseline can move
        // to the new value now; otherwise its next poll records this same change again.
        FactionRelationshipsTimerScript.noteCurrentRelationship(factionId);
    }
}
