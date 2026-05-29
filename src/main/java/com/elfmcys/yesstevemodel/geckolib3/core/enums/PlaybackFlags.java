package com.elfmcys.yesstevemodel.geckolib3.core.enums;

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager;

public class PlaybackFlags {

    private final boolean audioEnabled;
    private final AudioPlayerManager audioPlayerManager = new AudioPlayerManager();

    private boolean paused;

    private boolean stopped;

    public PlaybackFlags(boolean z) {
        this.audioEnabled = z;
    }

    public void setPaused(boolean z) {
        this.paused = z;
    }

    public void setStopped(boolean z) {
        this.stopped = z;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public boolean isAudioEnabled() {
        return this.audioEnabled;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        return this.audioPlayerManager;
    }
}
