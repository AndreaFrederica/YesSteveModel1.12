package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class CustomArrowEntity extends AnimatableEntity<EntityArrow> {
    private static final ResourceLocation DEFAULT_ID = ModelIdUtil.getMainId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(YesSteveModel.MOD_ID, "default/arrow.png");
    private static final int FPS = 60;

    private ResourceLocation mainModel = DEFAULT_ID;
    private ResourceLocation texture = DEFAULT_TEXTURE;

    public CustomArrowEntity(EntityArrow entity) {
        super(entity, FPS);
        this.registerControllers();
    }

    private void registerControllers() {
        this.addAnimationController(new AnimationController<>(this, "main", 2, CustomArrowEntity::predicateMain));
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("parallel_%d_controller", i);
            String animationName = String.format("parallel%d", i);
            this.addAnimationController(new AnimationController<>(this, controllerName, 0, e -> predicateParallel(e, animationName)));
        }
    }

    public void setModelLocation(ResourceLocation mainModel) {
        this.mainModel = mainModel;
    }

    public void setTextureLocation(ResourceLocation texture) {
        this.texture = texture;
    }

    @Override
    public ResourceLocation getModelLocation() {
        if (GeckoLibCache.getInstance().getGeoModels().containsKey(this.mainModel)) {
            return this.mainModel;
        }
        return DEFAULT_ID;
    }

    @Override
    public ResourceLocation getAnimationFileLocation() {
        if (GeckoLibCache.getInstance().getAnimations().containsKey(this.mainModel)) {
            return this.mainModel;
        }
        return DEFAULT_ID;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return this.texture;
    }

    public static PlayState predicateMain(AnimationEvent<CustomArrowEntity> event) {
        EntityArrow arrowEntity = event.getAnimatableEntity().getEntity();
        if (arrowEntity == null) {
            return PlayState.STOP;
        }
        if (arrowEntity.isInWater()) {
            return playAnimation(event, "water");
        }
        if (arrowEntity.isBurning()) {
            return playAnimation(event, "fire");
        }
        if (arrowEntity.inGround) {
            return playAnimation(event, "ground");
        } else {
            return playAnimation(event, "air");
        }
    }

    public static PlayState predicateParallel(AnimationEvent<CustomArrowEntity> event, String animationName) {
        if (Minecraft.getMinecraft().isGamePaused()) {
            return PlayState.STOP;
        }
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, ILoopType.EDefaultLoopTypes.LOOP));
        return playAnimation(event, animationName);
    }

    @Nonnull
    private static <P extends AnimatableEntity<?>> PlayState playAnimation(AnimationEvent<P> event, String animationName) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }
}
