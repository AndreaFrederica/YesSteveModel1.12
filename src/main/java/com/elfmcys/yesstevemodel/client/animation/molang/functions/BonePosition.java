package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;

import javax.annotation.Nonnull;

public final class BonePosition extends BoneParamFunction {
    @Override
    protected Vec3fStruct getParam(@Nonnull IBone bone) {
        return new BonePositionStruct(bone);
    }

    private static final class BonePositionStruct extends Vec3fStruct {
        private final IBone bone;

        public BonePositionStruct(IBone bone) {
            this.bone = bone;
        }

        @Override
        protected float getX() {
            return this.bone.getPositionX();
        }

        @Override
        protected float getY() {
            return this.bone.getPositionY();
        }

        @Override
        protected float getZ() {
            return this.bone.getPositionZ();
        }
    }
}
