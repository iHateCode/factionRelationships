package com.factionrelationships;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;
import org.apache.log4j.Logger;

public class FactionRelationshipsPlugin extends BaseModPlugin {

    private static Logger log;

    /** Overlay visibility toggle; read by renderer, written by keybind script (Toggle mode). Default true. */
    private static volatile boolean overlayVisible = true;

    /** Key currently held; used in Hold mode so overlay shows only while key is down. */
    private static volatile boolean overlayKeyHeld = false;

    /** Cached overlay keybind mode (Toggle vs Hold); invalidated when LunaSettings change. */
    private static volatile String cachedOverlayKeybindMode = null;

    public static boolean isOverlayVisible() {
        return overlayVisible;
    }

    public static void setOverlayVisible(boolean visible) {
        overlayVisible = visible;
    }

    public static boolean isOverlayKeyHeld() {
        return overlayKeyHeld;
    }

    public static void setOverlayKeyHeld(boolean held) {
        overlayKeyHeld = held;
    }

    /** Returns "Toggle" or "Hold"; default "Toggle" if unset or LunaLib disabled. */
    public static String getOverlayKeybindMode() {
        if (cachedOverlayKeybindMode != null) {
            return cachedOverlayKeybindMode;
        }
        String mode = "Toggle";
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            String s = LunaSettings.getString("factionrelationships", "overlayKeybindMode");
            if (s != null && ("Hold".equalsIgnoreCase(s) || "Toggle".equalsIgnoreCase(s))) {
                mode = "Hold".equalsIgnoreCase(s) ? "Hold" : "Toggle";
            }
        }
        cachedOverlayKeybindMode = mode;
        return mode;
    }

    /** Called when LunaSettings change so mode and other caches are re-read. */
    public static void invalidateSettingsCache() {
        cachedOverlayKeybindMode = null;
    }

    @Override
    public void onApplicationLoad() throws Exception {
        log = Global.getLogger(FactionRelationshipsPlugin.class);
        log.info("Faction Relationships mod loaded.");
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            LunaSettings.addSettingsListener(new LunaSettingsListener() {
                @Override
                public void settingsChanged(String modID) {
                    if ("factionrelationships".equals(modID)) {
                        FactionRelationshipsPlugin.invalidateSettingsCache();
                        FactionRelationshipsUIRenderer.invalidateSettingsCache();
                    }
                }
            });
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        Global.getSector().getListenerManager().addListener(new FactionRelationshipsUIRenderer(), true);
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            Global.getSector().addScript(new FactionRelationshipsKeybindScript());
        }
        if (log != null) {
            log.info("Faction Relationships UI renderer and keybind script registered.");
        }
    }
}
