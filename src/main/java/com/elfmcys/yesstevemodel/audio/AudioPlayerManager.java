package com.elfmcys.yesstevemodel.audio;

import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.function.Consumer;

public class AudioPlayerManager {

    private final Int2ReferenceOpenHashMap<ManagedSound> activePlayers = new Int2ReferenceOpenHashMap<>();
    private final java.util.List<ManagedSound> transientPlayers = new java.util.ArrayList<>();

    public boolean playSound(AnimatableEntity<?> entity, int soundId, String soundName, boolean forceReplace,
                             @Nullable Consumer<ManagedSound> callback) {
        ResourceLocation soundLocation = ResourceUtil.tryParse(soundName);
        ManagedSound sound = soundLocation != null ? new ManagedSound(soundLocation, entity.getEntity()) : null;
        if (callback != null) {
            callback.accept(sound);
        }
        if (sound == null) {
            return false;
        }
        if (soundId != 0) {
            ManagedSound existing = this.activePlayers.get(soundId);
            if (existing != null && !existing.isStopped()) {
                if (!forceReplace) {
                    return false;
                }
                existing.release();
            }
            this.activePlayers.put(soundId, sound);
        } else {
            this.transientPlayers.add(sound);
        }
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        return true;
    }

    public boolean stopSound(int soundId) {
        ManagedSound sound = soundId != 0 ? this.activePlayers.remove(soundId) : null;
        if (sound == null) {
            return false;
        }
        sound.release();
        return true;
    }

    public void stopAll() {
        for (ManagedSound sound : this.activePlayers.values()) {
            sound.release();
        }
        for (ManagedSound sound : this.transientPlayers) {
            sound.release();
        }
        this.activePlayers.clear();
        this.transientPlayers.clear();
    }

    public void tick() {
        Iterator<ManagedSound> transientIterator = this.transientPlayers.iterator();
        while (transientIterator.hasNext()) {
            if (transientIterator.next().isStopped()) {
                transientIterator.remove();
            }
        }
        this.activePlayers.int2ReferenceEntrySet().removeIf(entry -> entry.getValue().isStopped());
    }

    public static class ManagedSound extends MovingSound {
        @Nullable
        private final Entity entity;
        private boolean stopped;

        public ManagedSound(ResourceLocation soundLocation, @Nullable Entity entity) {
            super(new SoundEvent(soundLocation), SoundCategory.PLAYERS);
            this.entity = entity;
            this.volume = 1.0f;
            this.pitch = 1.0f;
            this.repeat = false;
            this.repeatDelay = 0;
            this.attenuationType = ISound.AttenuationType.LINEAR;
            this.updatePosition();
        }

        @Override
        public void update() {
            if (this.entity == null || this.entity.isDead) {
                this.stopped = true;
                this.donePlaying = true;
                return;
            }
            this.updatePosition();
        }

        @Override
        public boolean isDonePlaying() {
            return this.stopped || super.isDonePlaying();
        }

        public boolean isStopped() {
            return this.stopped || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this);
        }

        public void release() {
            this.stopped = true;
            this.donePlaying = true;
            Minecraft.getMinecraft().getSoundHandler().stopSound(this);
        }

        public void setLooping(boolean looping) {
            this.repeat = looping;
        }

        public void setVolume(float volume) {
            this.volume = volume;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        private void updatePosition() {
            if (this.entity != null) {
                this.xPosF = (float) this.entity.posX;
                this.yPosF = (float) this.entity.posY;
                this.zPosF = (float) this.entity.posZ;
            }
        }
    }
}
