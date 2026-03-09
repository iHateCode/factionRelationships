package com.factionrelationships;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

/**
 * Polls the LunaLib keybind. In Toggle mode: press to show/hide overlay (edge detection).
 * In Hold mode: overlay visible only while key is held.
 */
public class FactionRelationshipsKeybindScript implements EveryFrameScript {

    private boolean keyWasDown;

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
        if (!Global.getSettings().getModManager().isModEnabled("lunalib")) {
            return;
        }
        Integer keycodeObj = LunaSettings.getInt("factionrelationships", "toggleOverlayKeybind");
        if (keycodeObj == null || keycodeObj.intValue() == 0) {
            FactionRelationshipsPlugin.setOverlayKeyHeld(false);
            return;
        }
        int keycode = keycodeObj.intValue();
        boolean keyDown = Keyboard.isKeyDown(keycode);
        String mode = FactionRelationshipsPlugin.getOverlayKeybindMode();
        if ("Hold".equals(mode)) {
            FactionRelationshipsPlugin.setOverlayKeyHeld(keyDown);
        } else {
            FactionRelationshipsPlugin.setOverlayKeyHeld(false);
            if (!keyWasDown && keyDown) {
                FactionRelationshipsPlugin.setOverlayVisible(!FactionRelationshipsPlugin.isOverlayVisible());
            }
        }
        keyWasDown = keyDown;
    }
}
