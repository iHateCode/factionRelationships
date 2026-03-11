package com.factionrelationships;

import com.fs.starfarer.api.EveryFrameScript;

/**
 * Stub for save compatibility only. Old saves serialized this script in the
 * campaign engine; this class allows them to load. The script reports itself
 * done immediately so the engine removes it after load.
 */
public class FactionRelationshipsKeybindScript implements EveryFrameScript {

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
    }
}
