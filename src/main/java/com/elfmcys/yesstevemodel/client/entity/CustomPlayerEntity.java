package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.animation.AnimationManager;
import com.elfmcys.yesstevemodel.client.model.CustomPlayerModel;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationFactory;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.GeckoLibUtil;
import com.elfmcys.yesstevemodel.util.Keep;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

public class CustomPlayerEntity implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this, false);
    private ResourceLocation mainModel = CustomPlayerModel.DEFAULT_MAIN_MODEL;
    private ResourceLocation texture = CustomPlayerModel.DEFAULT_TEXTURE;
    private String previewAnimation = "";
    private PlayerEntity player = null;

    @Nonnull
    private static <P extends IAnimatable> PlayState playLoopAnimation(AnimationEvent<P> event, String animationName) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    @Keep
    @SuppressWarnings("all")
    public void registerControllers(AnimationData data) {
        AnimationManager manager = AnimationManager.getInstance();
        data.addAnimationController(new AnimationController(this, "main_controller", 2, manager::predicate));
        data.addAnimationController(new AnimationController(this, "use_controller", 10, manager::predicateUse));
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("parallel_%d_controller", i);
            String animationName = String.format("parallel%d", i);
            data.addAnimationController(new AnimationController<>(this, controllerName, 2, e -> playLoopAnimation(e, animationName)));
        }
    }

    public ResourceLocation getMainModel() {
        if (GeckoLibCache.getInstance().getGeoModels().containsKey(this.mainModel)) {
            return mainModel;
        }
        return CustomPlayerModel.DEFAULT_MAIN_MODEL;
    }

    public void setMainModel(ResourceLocation mainModel) {
        this.mainModel = mainModel;
    }

    public ResourceLocation getAnimation() {
        if (GeckoLibCache.getInstance().getAnimations().containsKey(this.mainModel)) {
            return mainModel;
        }
        return CustomPlayerModel.DEFAULT_MAIN_ANIMATION;
    }

    public float getHeightScale() {
        if (ClientModelManager.SCALE_INFO.containsKey(this.mainModel)) {
            return ClientModelManager.SCALE_INFO.get(this.mainModel).getLeft().floatValue();
        }
        return 0.7f;
    }

    public float getWidthScale() {
        if (ClientModelManager.SCALE_INFO.containsKey(this.mainModel)) {
            return ClientModelManager.SCALE_INFO.get(this.mainModel).getRight().floatValue();
        }
        return 0.7f;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    @Override
    @Keep
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public String getPreviewAnimation() {
        return previewAnimation;
    }

    public void setPreviewAnimation(String previewAnimation) {
        this.previewAnimation = previewAnimation;
    }

    public void clearPreviewAnimation() {
        this.previewAnimation = "";
    }

    public boolean hasPreviewAnimation() {
        return StringUtils.isNoneBlank(this.previewAnimation);
    }

    public boolean hasPreviewAnimation(String previewAnimation) {
        return hasPreviewAnimation() && previewAnimation.equals(this.previewAnimation);
    }
}
