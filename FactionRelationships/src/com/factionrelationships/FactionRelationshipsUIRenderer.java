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

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FactionRelationshipsUIRenderer implements CampaignUIRenderingListener {

    private static final float X_PAD = 20f;
    private static final float Y_PAD = 80f;
    private static final float LINE_HEIGHT = 14f;
    private static final int DEFAULT_MAX_FACTIONS = 15;
    private static final int MIN_MAX_FACTIONS = 1;
    private static final int MAX_MAX_FACTIONS = 50;
    private static final String CONFIG_PATH = "config/faction_relationships_config.json";
    private static final String MOD_ID = "factionrelationships";

    private static Integer cachedMaxFactions = null;

    private static int getMaxFactions() {
        if (cachedMaxFactions != null) {
            return cachedMaxFactions.intValue();
        }
        try {
            JSONObject config = Global.getSettings().loadJSON(CONFIG_PATH, MOD_ID);
            int value = config.optInt("maxFactions", DEFAULT_MAX_FACTIONS);
            cachedMaxFactions = Integer.valueOf(Math.max(MIN_MAX_FACTIONS, Math.min(MAX_MAX_FACTIONS, value)));
        } catch (IOException e) {
            cachedMaxFactions = Integer.valueOf(DEFAULT_MAX_FACTIONS);
        } catch (JSONException e) {
            cachedMaxFactions = Integer.valueOf(DEFAULT_MAX_FACTIONS);
        }
        return cachedMaxFactions.intValue();
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

        float screenW = Global.getSettings().getScreenWidth();
        float screenH = Global.getSettings().getScreenHeight();

        List<LabelAPI> labels = new ArrayList<LabelAPI>();
        for (int i = 0; i < count; i++) {
            FactionAPI faction = factions.get(i);
            RelationshipAPI rel = faction.getRelToPlayer();
            float repValue = rel.getRel();
            String levelName = rel.getLevel().getDisplayName();
            String line = faction.getDisplayName() + "  " + formatRepValue(repValue) + "  " + levelName;
            Color color = rel.getRelColor();

            LabelAPI label = Global.getSettings().createLabel(line, Fonts.VICTOR_10);
            label.setColor(color);
            labels.add(label);
        }

        for (int i = 0; i < labels.size(); i++) {
            LabelAPI label = labels.get(i);
            float w = label.computeTextWidth(label.getText());
            PositionAPI pos = label.getPosition();
            if (i == 0) {
                pos.inTR(X_PAD, Y_PAD);
            } else {
                float y = Y_PAD + i * LINE_HEIGHT;
                pos.setLocation(screenW - X_PAD - w, screenH - y - LINE_HEIGHT);
            }
            label.render(1f);
        }
    }

    private static String formatRepValue(float rel) {
        int pct = (int) Math.round(rel * 100f);
        if (pct >= 0) {
            return "+" + pct;
        }
        return String.valueOf(pct);
    }
}
