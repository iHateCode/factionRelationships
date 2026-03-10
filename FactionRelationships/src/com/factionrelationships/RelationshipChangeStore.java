package com.factionrelationships;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds recent faction relationship changes for UI display.
 * Listener writes; UI renderer reads and prunes expired entries.
 * All access is on the game thread.
 */
public final class RelationshipChangeStore {

    public static final class RecentChange {
        public final float delta;
        public final long expiryTimeMillis;

        public RecentChange(float delta, long expiryTimeMillis) {
            this.delta = delta;
            this.expiryTimeMillis = expiryTimeMillis;
        }
    }

    private static final Map<String, RecentChange> RECENT_CHANGES = new HashMap<String, RecentChange>();

    /** Expiry time for auto-shown overlay (0 = no auto-show expiry). */
    private static long autoShowOverlayUntilMillis = 0L;

    /** Set when the auto-shown overlay should be hidden. Call when auto-showing. */
    public static void setAutoShowExpiry(long untilMillis) {
        autoShowOverlayUntilMillis = untilMillis;
    }

    /** Current auto-show overlay expiry (0 if not set). */
    public static long getAutoShowExpiry() {
        return autoShowOverlayUntilMillis;
    }

    /** Clear auto-show expiry so we do not auto-hide. Call when user uses keybind or when we auto-hide. */
    public static void clearAutoShowExpiry() {
        autoShowOverlayUntilMillis = 0L;
    }

    /** Record a relationship change for a faction (overwrites any existing for that faction). Duration in milliseconds. */
    public static void record(String factionId, float delta, long durationMs) {
        RECENT_CHANGES.put(factionId, new RecentChange(delta, System.currentTimeMillis() + durationMs));
    }

    /** Get the current map of recent changes (read-only view; caller may prune via removeExpired). */
    public static Map<String, RecentChange> getRecentChanges() {
        return RECENT_CHANGES;
    }

    /** Remove entries that have passed their expiry time. Call from renderer when drawing. */
    public static void removeExpired() {
        long now = System.currentTimeMillis();
        for (java.util.Iterator<Map.Entry<String, RecentChange>> it = RECENT_CHANGES.entrySet().iterator(); it.hasNext(); ) {
            if (it.next().getValue().expiryTimeMillis < now) {
                it.remove();
            }
        }
    }
}
