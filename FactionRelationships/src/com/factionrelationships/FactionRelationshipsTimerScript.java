package com.factionrelationships;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.FactionAPI;
import lunalib.lunaSettings.LunaSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Polls player-faction relationships on a timer and routes detected deltas
 * through the same pipeline used by event-driven relationship changes.
 */
public class FactionRelationshipsTimerScript implements EveryFrameScript {

    private static final String TIMER_ENABLED_SETTING = "timerEnabled";
    private static final String TIMER_MODE_SETTING = "timerMode";
    private static final String TIMER_INTERVAL_SETTING = "realSecondsInterval";

    private static final String MODE_REAL_SECONDS = "REAL_SECONDS";
    private static final String MODE_IN_GAME_DAY = "IN_GAME_DAY";

    private static final int DEFAULT_REAL_SECONDS_INTERVAL = 5;
    private static final float DELTA_THRESHOLD = 0.001f;

    private final Map<String, Float> repSnapshot = new HashMap<String, Float>();
    private float accumulatedSeconds = 0f;
    private long accumulatedRealMillis = 0L;
    private long lastAdvanceWallMillis = -1L;
    private int lastProcessedDayIndex = -1;

    /** The script registered for the current game; transient, replaced on each game load. */
    private static FactionRelationshipsTimerScript current = null;

    public FactionRelationshipsTimerScript() {
        initSnapshot();
        current = this;
    }

    /**
     * Moves the baseline for one faction to its current standing. Called when the
     * game reports a change itself, so the next poll does not record it a second time.
     */
    public static void noteCurrentRelationship(String factionId) {
        FactionRelationshipsTimerScript script = current;
        if (script == null || factionId == null
                || Global.getSector() == null || Global.getSector().getPlayerFaction() == null) {
            return;
        }
        script.repSnapshot.put(factionId, Global.getSector().getPlayerFaction().getRelationship(factionId));
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (!isTimerEnabled()) {
            return;
        }
        if (Global.getSector() == null || Global.getSector().getPlayerFaction() == null) {
            return;
        }

        String mode = getTimerMode();
        if (MODE_IN_GAME_DAY.equals(mode)) {
            int currentDayIndex = computeDayIndex();
            if (currentDayIndex > lastProcessedDayIndex) {
                lastProcessedDayIndex = currentDayIndex;
                runTimerCheck();
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (lastAdvanceWallMillis < 0L) {
            lastAdvanceWallMillis = now;
        }
        long elapsedWallMillis = now - lastAdvanceWallMillis;
        if (elapsedWallMillis < 0L) {
            elapsedWallMillis = 0L;
        }
        lastAdvanceWallMillis = now;

        accumulatedSeconds += amount;
        accumulatedRealMillis += elapsedWallMillis;
        int intervalSeconds = getRealSecondsInterval();
        long intervalMillis = intervalSeconds * 1000L;
        if (accumulatedRealMillis >= intervalMillis) {
            accumulatedRealMillis = accumulatedRealMillis % intervalMillis;
            accumulatedSeconds = 0f;
            runTimerCheck();
        }
    }

    private void initSnapshot() {
        if (Global.getSector() == null || Global.getSector().getPlayerFaction() == null) {
            return;
        }
        List<FactionAPI> factions = Global.getSector().getAllFactions();
        if (factions == null) {
            return;
        }

        String playerFactionId = Global.getSector().getPlayerFaction().getId();
        for (FactionAPI faction : factions) {
            String id = faction.getId();
            if (id.equals(playerFactionId)) continue;
            repSnapshot.put(id, Global.getSector().getPlayerFaction().getRelationship(id));
        }
        lastProcessedDayIndex = computeDayIndex();
    }

    private void runTimerCheck() {
        List<FactionAPI> factions = Global.getSector().getAllFactions();
        if (factions == null) {
            return;
        }

        String playerFactionId = Global.getSector().getPlayerFaction().getId();
        for (FactionAPI faction : factions) {
            String id = faction.getId();
            if (id.equals(playerFactionId)) continue;

            float currentRep = Global.getSector().getPlayerFaction().getRelationship(id);
            Float snapshotRep = repSnapshot.get(id);
            float baseline = (snapshotRep != null) ? snapshotRep.floatValue() : currentRep;
            float delta = currentRep - baseline;
            if (Math.abs(delta) >= DELTA_THRESHOLD) {
                FactionRelationshipChangeListener.recordRelationshipChange(id, delta);
            }
            repSnapshot.put(id, currentRep);
        }
    }

    private boolean isTimerEnabled() {
        if (!FactionRelationshipsPlugin.isLunaLibEnabled()) {
            return true;
        }
        Boolean v = LunaSettings.getBoolean(FactionRelationshipsPlugin.MOD_ID, TIMER_ENABLED_SETTING);
        return v == null || v.booleanValue();
    }

    private String getTimerMode() {
        if (!FactionRelationshipsPlugin.isLunaLibEnabled()) {
            return MODE_REAL_SECONDS;
        }
        String v = LunaSettings.getString(FactionRelationshipsPlugin.MOD_ID, TIMER_MODE_SETTING);
        return MODE_IN_GAME_DAY.equalsIgnoreCase(v) ? MODE_IN_GAME_DAY : MODE_REAL_SECONDS;
    }

    private int getRealSecondsInterval() {
        if (!FactionRelationshipsPlugin.isLunaLibEnabled()) {
            return DEFAULT_REAL_SECONDS_INTERVAL;
        }
        Integer v = LunaSettings.getInt(FactionRelationshipsPlugin.MOD_ID, TIMER_INTERVAL_SETTING);
        return (v == null || v.intValue() < 1) ? DEFAULT_REAL_SECONDS_INTERVAL : v.intValue();
    }

    private int computeDayIndex() {
        if (Global.getSector() == null || Global.getSector().getClock() == null) {
            return -1;
        }
        int cycle = Global.getSector().getClock().getCycle();
        int month = Global.getSector().getClock().getMonth();
        int day = Global.getSector().getClock().getDay();
        return (cycle * 12 * 30) + (month * 30) + day;
    }
}
