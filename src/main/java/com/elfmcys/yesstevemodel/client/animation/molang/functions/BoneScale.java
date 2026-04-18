package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;

import javax.annotation.Nonnull;

public final class BoneScale extends BoneParamFunction {
    @Override
    protected Vec3fStruct getParam(@Nonnull IBone bone) {
        return new BoneScaleStruct(bone);
    }

    private static final class BoneScaleStruct extends Vec3fStruct {
        private final IBone bone;

        public BoneScaleStruct(IBone bone) {
            this.bone = bone;
        }

        @Override
        protected float getX() {
            return this.bone.getScaleX();
        }

        @Override
        protected float getY() {
            return this.bone.getScaleY();
        }

        @Override
        protected float getZ() {
            return this.bone.getScaleZ();
        }
    }
}
