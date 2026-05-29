package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.function.entity.EntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;

public final class SoundFunction {
    private SoundFunction() {
    }

    private static int getSoundId(ExecutionContext<IContext<Entity>> context, Function.ArgumentCollection arguments, int index) {
        Object idValue = arguments.getValue(context, index);
        if (idValue instanceof Number) {
            int numericId = -((Number) idValue).intValue();
            return numericId > 0 ? 0 : numericId;
        }
        String id = String.valueOf(idValue);
        return StringPool.computeIfAbsent(id);
    }

    public static class PlaySoundFunction extends EntityFunction {
        @Override
        public Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
            IContext<Entity> animationContext = context.entity();
            if (!animationContext.isClientSide() || !animationContext.allowImpureOperations()) {
                return false;
            }
            int id = getSoundId(context, arguments, 0);
            if (id == 0 && arguments.getValue(context, 0) instanceof Number) {
                return false;
            }
            String soundName = arguments.getAsString(context, 1);
            if (StringUtils.isBlank(soundName) || animationContext.geoInstance().getEntity() == null) {
                return false;
            }
            int flags = arguments.size() >= 3 ? arguments.getAsInt(context, 2) : 0;
            if (flags < 0 || flags > 7) {
                return false;
            }
            AudioPlayerManager audioPlayerManager = animationContext.getAudioPlayerManager((flags & 2) == 2);
            if (audioPlayerManager == null) {
                return false;
            }
            int resolvedFlags = flags;
            return audioPlayerManager.playSound(animationContext.geoInstance(), id, soundName, (flags & 1) == 1, sound -> {
                if (sound == null) {
                    animationContext.logWarning("Sound not found: {}", soundName);
                    return;
                }
                sound.setLooping((resolvedFlags & 4) == 4);
                if (arguments.size() >= 4) {
                    sound.setVolume(MathHelper.clamp(arguments.getAsFloat(context, 3), 0.001f, 1000.0f));
                }
                if (arguments.size() >= 5) {
                    sound.setPitch(MathHelper.clamp(arguments.getAsFloat(context, 4), 0.001f, 1000.0f));
                }
            });
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size >= 2 && size <= 5;
        }
    }

    public static class StopSoundFunction extends EntityFunction {
        @Override
        public Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
            IContext<Entity> animationContext = context.entity();
            if (!animationContext.isClientSide() || !animationContext.allowImpureOperations()) {
                return false;
            }
            int id = getSoundId(context, arguments, 0);
            if (id == 0 && arguments.getValue(context, 0) instanceof Number) {
                return false;
            }
            AudioPlayerManager audioPlayerManager = animationContext.getAudioPlayerManager(arguments.size() == 2 && arguments.getAsBoolean(context, 1));
            return audioPlayerManager != null && audioPlayerManager.stopSound(id);
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size == 1 || size == 2;
        }
    }

    public static class StopAllSoundsFunction extends EntityFunction {
        @Override
        public Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
            IContext<Entity> animationContext = context.entity();
            if (!animationContext.isClientSide() || !animationContext.allowImpureOperations()) {
                return false;
            }
            AudioPlayerManager audioPlayerManager = animationContext.getAudioPlayerManager(arguments.size() > 0 && arguments.getAsBoolean(context, 0));
            if (audioPlayerManager == null) {
                return false;
            }
            audioPlayerManager.stopAll();
            return true;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size <= 1;
        }
    }
}
