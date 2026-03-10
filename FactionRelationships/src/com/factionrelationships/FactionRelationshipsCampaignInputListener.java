package com.factionrelationships;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener;
import com.fs.starfarer.api.input.InputEventAPI;
import lunalib.lunaSettings.LunaSettings;

import java.util.List;

/**
 * Handles the overlay toggle keybind on the campaign map using the game's input
 * event stream. This runs when the campaign is active (and while paused when
 * registered with addListener(..., true)), so the key is detected reliably
 * instead of polling {@code Keyboard.isKeyDown} in an EveryFrameScript.
 * <p>
 * Pattern used by mods like Console Commands and Advanced Weapon Control:
 * implement {@link CampaignInputListener} and handle keys in
 * {@link #processCampaignInputPreCore(List)}.
 */
public class FactionRelationshipsCampaignInputListener implements CampaignInputListener {

    @Override
    public int getListenerInputPriority() {
        return 0;
    }

    @Override
    public void processCampaignInputPreCore(List<InputEventAPI> events) {
        if (!Global.getSettings().getModManager().isModEnabled("lunalib")) {
            return;
        }
        Integer keycodeObj = LunaSettings.getInt("factionrelationships", "toggleOverlayKeybind");
        if (keycodeObj == null || keycodeObj.intValue() == 0) {
            FactionRelationshipsPlugin.setOverlayKeyHeld(false);
            return;
        }
        final int keycode = keycodeObj.intValue();
        final String mode = FactionRelationshipsPlugin.getOverlayKeybindMode();

        for (InputEventAPI event : events) {
            if (event.isConsumed()) {
                continue;
            }
            if (event.isKeyDownEvent() && event.getEventValue() == keycode) {
                RelationshipChangeStore.clearAutoShowExpiry();
                if ("Hold".equals(mode)) {
                    FactionRelationshipsPlugin.setOverlayKeyHeld(true);
                } else {
                    FactionRelationshipsPlugin.setOverlayVisible(!FactionRelationshipsPlugin.isOverlayVisible());
                }
                event.consume();
                break;
            }
            if ("Hold".equals(mode) && event.isKeyUpEvent() && event.getEventValue() == keycode) {
                RelationshipChangeStore.clearAutoShowExpiry();
                FactionRelationshipsPlugin.setOverlayKeyHeld(false);
                event.consume();
                break;
            }
        }
    }

    @Override
    public void processCampaignInputPreFleetControl(List<InputEventAPI> events) {
    }

    @Override
    public void processCampaignInputPostCore(List<InputEventAPI> events) {
    }
}
