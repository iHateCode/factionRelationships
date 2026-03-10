package com.factionrelationships;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.CampaignUIRenderingListener;
import com.fs.starfarer.api.characters.RelationshipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.ui.Fonts;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import lunalib.lunaSettings.LunaSettings;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FactionRelationshipsUIRenderer implements CampaignUIRenderingListener {

    private static final float X_PAD = 20f;
    private static final float Y_PAD = 80f;
    private static final int DEFAULT_MAX_FACTIONS = 15;
    private static final int MIN_MAX_FACTIONS = 1;
    private static final int MAX_MAX_FACTIONS = 50;
    private static final String MOD_ID = "factionrelationships";

    /** Reputation -50 in UI = -0.5f in API. */
    private static final float HOSTILE_THRESHOLD = -0.5f;

    private static Integer cachedMaxFactions = null;
    private static Boolean cachedShowOnlyHostile = null;
    private static Boolean cachedShowRelationshipChangeInOverlay = null;
    private static String cachedFont = null;
    private static Float cachedLineHeight = null;

    public static void invalidateSettingsCache() {
        cachedMaxFactions = null;
        cachedShowOnlyHostile = null;
        cachedShowRelationshipChangeInOverlay = null;
        cachedFont = null;
        cachedLineHeight = null;
    }

    private static boolean getShowRelationshipChangeInOverlay() {
        if (cachedShowRelationshipChangeInOverlay != null) {
            return cachedShowRelationshipChangeInOverlay.booleanValue();
        }
        boolean value = true;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            Boolean v = LunaSettings.getBoolean(MOD_ID, "showRelationshipChangeInOverlay");
            if (v != null) {
                value = v.booleanValue();
            }
        }
        cachedShowRelationshipChangeInOverlay = Boolean.valueOf(value);
        return value;
    }

    private static boolean getShowOnlyHostile() {
        if (cachedShowOnlyHostile != null) {
            return cachedShowOnlyHostile.booleanValue();
        }
        boolean value = false;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            Boolean v = LunaSettings.getBoolean(MOD_ID, "showOnlyHostile");
            if (v != null) {
                value = v.booleanValue();
            }
        }
        cachedShowOnlyHostile = Boolean.valueOf(value);
        return value;
    }

    private static int getMaxFactions() {
        if (cachedMaxFactions != null) {
            return cachedMaxFactions.intValue();
        }
        int value = DEFAULT_MAX_FACTIONS;
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            Integer v = LunaSettings.getInt(MOD_ID, "maxFactions");
            if (v != null) {
                value = v.intValue();
            }
        }
        cachedMaxFactions = Integer.valueOf(Math.max(MIN_MAX_FACTIONS, Math.min(MAX_MAX_FACTIONS, value)));
        return cachedMaxFactions.intValue();
    }

    private static class FontAndLineHeight {
        final String font;
        final float lineHeight;

        FontAndLineHeight(String font, float lineHeight) {
            this.font = font;
            this.lineHeight = lineHeight;
        }
    }

    private static FontAndLineHeight getFontAndLineHeight() {
        if (cachedFont != null && cachedLineHeight != null) {
            return new FontAndLineHeight(cachedFont, cachedLineHeight.floatValue());
        }
        String size = "Medium";
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            String s = LunaSettings.getString(MOD_ID, "textSize");
            if (s != null && !s.isEmpty()) {
                size = s;
            }
        }
        String font = Fonts.VICTOR_10;
        float lineHeight = 18f;
        if ("Small".equalsIgnoreCase(size)) {
            font = Fonts.VICTOR_10;
            lineHeight = 14f;
        } else if ("Large".equalsIgnoreCase(size)) {
            font = Fonts.ORBITRON_20AA;
            lineHeight = 24f;
        } else {
            font = Fonts.ORBITRON_12;
            lineHeight = 18f;
        }
        cachedFont = font;
        cachedLineHeight = Float.valueOf(lineHeight);
        return new FontAndLineHeight(font, lineHeight);
    }

    @Override
    public void renderInUICoordsBelowUI(ViewportAPI viewport) {
        // no-op; draw above UI
    }

    @Override
    public void renderInUICoordsAboveUIBelowTooltips(ViewportAPI viewport) {
        renderPanel(viewport);
    }

    @Override
    public void renderInUICoordsAboveUIAndTooltips(ViewportAPI viewport) {
        // no-op; we use AboveUIBelowTooltips so tooltips can show on top
    }

    private void renderPanel(ViewportAPI viewport) {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) {
            return;
        }
        long autoShowExpiry = RelationshipChangeStore.getAutoShowExpiry();
        if (autoShowExpiry > 0
                && System.currentTimeMillis() >= autoShowExpiry
                && FactionRelationshipsPlugin.isOverlayVisible()) {
            FactionRelationshipsPlugin.setOverlayVisible(false);
            RelationshipChangeStore.clearAutoShowExpiry();
        }
        String mode = FactionRelationshipsPlugin.getOverlayKeybindMode();
        boolean showOverlay = "Hold".equals(mode)
            ? FactionRelationshipsPlugin.isOverlayKeyHeld()
            : FactionRelationshipsPlugin.isOverlayVisible();
        if (!showOverlay) {
            return;
        }

        Set<String> factionIdsWithMarkets = new HashSet<String>();
        EconomyAPI economy = Global.getSector().getEconomy();
        if (economy != null) {
            for (MarketAPI market : economy.getMarketsCopy()) {
                if (market.getFaction() != null) {
                    factionIdsWithMarkets.add(market.getFactionId());
                }
            }
        }

        List<FactionAPI> factions = new ArrayList<FactionAPI>();
        for (FactionAPI faction : Global.getSector().getAllFactions()) {
            if (faction.isPlayerFaction()) {
                continue;
            }
            if (!factionIdsWithMarkets.contains(faction.getId())) {
                continue;
            }
            factions.add(faction);
        }

        if (getShowOnlyHostile()) {
            List<FactionAPI> hostileOnly = new ArrayList<FactionAPI>();
            for (FactionAPI faction : factions) {
                if (faction.getRelToPlayer().getRel() <= HOSTILE_THRESHOLD) {
                    hostileOnly.add(faction);
                }
            }
            factions = hostileOnly;
        }

        Collections.sort(factions, new Comparator<FactionAPI>() {
            @Override
            public int compare(FactionAPI a, FactionAPI b) {
                float relA = a.getRelToPlayer().getRel();
                float relB = b.getRelToPlayer().getRel();
                return Float.compare(relA, relB);
            }
        });

        int maxFactions = getMaxFactions();
        int count = Math.min(factions.size(), maxFactions);
        if (count == 0) {
            return;
        }

        RelationshipChangeStore.removeExpired();
        Map<String, RelationshipChangeStore.RecentChange> recentChanges = RelationshipChangeStore.getRecentChanges();
        boolean showChangeInOverlay = getShowRelationshipChangeInOverlay();

        float screenW = Global.getSettings().getScreenWidth();
        float screenH = Global.getSettings().getScreenHeight();
        FontAndLineHeight fontAndLine = getFontAndLineHeight();
        float lineHeight = fontAndLine.lineHeight;

        List<LabelAPI> labels = new ArrayList<LabelAPI>();
        List<LabelAPI> deltaLabels = new ArrayList<LabelAPI>();
        for (int i = 0; i < count; i++) {
            FactionAPI faction = factions.get(i);
            RelationshipAPI rel = faction.getRelToPlayer();
            float repValue = rel.getRel();
            String levelName = rel.getLevel().getDisplayName();
            String line = faction.getDisplayName() + "  " + formatRepValue(repValue) + "  " + levelName;
            Color color = rel.getRelColor();

            LabelAPI label = Global.getSettings().createLabel(line, fontAndLine.font);
            label.setColor(color);
            labels.add(label);

            LabelAPI deltaLabel = null;
            if (showChangeInOverlay) {
                RelationshipChangeStore.RecentChange change = recentChanges.get(faction.getId());
                if (change != null) {
                    String deltaStr = formatDelta(change.delta);
                    deltaLabel = Global.getSettings().createLabel(deltaStr, fontAndLine.font);
                    deltaLabel.setColor(change.delta >= 0 ? new Color(100, 255, 100) : new Color(255, 100, 100));
                }
            }
            deltaLabels.add(deltaLabel);
        }

        for (int i = 0; i < labels.size(); i++) {
            LabelAPI label = labels.get(i);
            LabelAPI deltaLabel = deltaLabels.get(i);
            float w = label.computeTextWidth(label.getText());
            float deltaW = (deltaLabel != null) ? deltaLabel.computeTextWidth(deltaLabel.getText()) + 4f : 0f;
            float totalW = w + deltaW;
            float y = screenH - (Y_PAD + (i + 1) * lineHeight);
            PositionAPI pos = label.getPosition();
            pos.setLocation(screenW - X_PAD - totalW, y);
            label.render(1f);
            if (deltaLabel != null) {
                PositionAPI deltaPos = deltaLabel.getPosition();
                deltaPos.setLocation(screenW - X_PAD - totalW + w, y);
                deltaLabel.render(1f);
            }
        }
    }

    private static String formatDelta(float delta) {
        int pct = (int) Math.round(delta * 100f);
        if (pct >= 0) {
            return " (+" + pct + ")";
        }
        return " (" + pct + ")";
    }

    private static String formatRepValue(float rel) {
        int pct = (int) Math.round(rel * 100f);
        if (pct >= 0) {
            return "+" + pct;
        }
        return String.valueOf(pct);
    }
}
