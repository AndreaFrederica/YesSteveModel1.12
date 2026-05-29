package com.elfmcys.yesstevemodel.audio;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class AudioTrackData {
    @Nullable
    private final ByteBuffer data;
    private final AudioCodec codec;
    private final int sampleRate;
    private final long duration;

    public AudioTrackData(@Nullable ByteBuffer byteBuffer, int codecType, int sampleRate, long duration) {
        if (byteBuffer != null) {
            if (codecType == 2) {
                this.data = ByteBuffer.allocateDirect(byteBuffer.remaining());
            } else {
                this.data = ByteBuffer.allocate(byteBuffer.remaining());
            }
            this.data.duplicate().put(byteBuffer.duplicate());
        } else {
            this.data = null;
        }

        AudioCodec resolvedCodec;
        switch (codecType) {
            case 1:
                resolvedCodec = AudioCodec.VORBIS;
                break;
            case 2:
                resolvedCodec = AudioCodec.OPUS;
                break;
            default:
                resolvedCodec = AudioCodec.UNDEFINED;
                break;
        }
        this.codec = resolvedCodec;
        this.sampleRate = sampleRate;
        this.duration = duration;
    }

    public long getDuration() {
        return this.duration;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public AudioCodec getCodec() {
        return this.codec;
    }

    @Nullable
    public ByteBuffer getData() {
        return this.data;
    }
}