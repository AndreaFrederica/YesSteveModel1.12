package com.elfmcys.yesstevemodel.client.animation.molang.functions;

import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;

import javax.annotation.Nonnull;

public final class BonePivotAbs extends BoneParamFunction {
    @Override
    protected Vec3fStruct getParam(@Nonnull IBone bone) {
        if (!bone.isTrackingXform()) {
            bone.setTrackXform(true);
        }
        return new BonePivotAbsStruct(bone);
    }

    private static final class BonePivotAbsStruct extends Vec3fStruct {
        private final IBone bone;

        private BonePivotAbsStruct(IBone bone) {
            this.bone = bone;
        }

        @Override
        protected float getX() {
            return this.bone.getPivotAbsX();
        }

        @Override
        protected float getY() {
            return this.bone.getPivotAbsY();
        }

        @Override
        protected float getZ() {
            return this.bone.getPivotAbsZ();
        }
    }
}
