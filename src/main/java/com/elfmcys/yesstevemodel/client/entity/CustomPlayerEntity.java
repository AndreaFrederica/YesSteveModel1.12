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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

public class CustomPlayerEntity implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this, true);
    private final ItemStack[] handItemsForAnimation = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY};
    private ResourceLocation mainModel = CustomPlayerModel.DEFAULT_MAIN_MODEL;
    private ResourceLocation texture = CustomPlayerModel.DEFAULT_TEXTURE;
    private String previewAnimation = "";
    private EntityPlayer player = null;

    @Nonnull
    private static <P extends IAnimatable> PlayState playLoopAnimation(AnimationEvent<P> event, String animationName) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        // 越往后优先级越高
        AnimationManager manager = AnimationManager.getInstance();
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("pre_parallel_%d_controller", i);
            String animationName = String.format("pre_parallel%d", i);
            this.addManagedController(data, controllerName, 0, e -> manager.predicateParallel(e, animationName));
        }
        this.addManagedController(data, "main_controller", 2, manager::predicateMain);
        this.addManagedController(data, "hold_offhand_controller", 0, manager::predicateOffhandHold);
        this.addManagedController(data, "hold_mainhand_controller", 0, manager::predicateMainhandHold);
        this.addManagedController(data, "swing_controller", 2, manager::predicateSwing);
        this.addManagedController(data, "use_controller", 2, manager::predicateUse);
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("parallel_%d_controller", i);
            String animationName = String.format("parallel%d", i);
            this.addManagedController(data, controllerName, 0, e -> manager.predicateParallel(e, animationName));
        }
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                String controllerName = String.format("%s_controller", slot.getName());
                this.addManagedController(data, controllerName, 0, e -> manager.predicateArmor(e, slot));
            }
        }
        this.addManagedController(data, "cap_controller", 2, manager::predicateCap);
    }

    private void addManagedController(
            AnimationData data, String controllerName, float transitionLengthTicks,
            AnimationController.IAnimationPredicate<CustomPlayerEntity> predicate
    ) {
        data.addAnimationController(new AnimationController<>(this, controllerName, transitionLengthTicks, predicate));
    }

    public ResourceLocation getMainModel() {
        if (GeckoLibCache.getInstance().getGeoModels().containsKey(this.mainModel)) {
            return this.mainModel;
        }
        return CustomPlayerModel.DEFAULT_MAIN_MODEL;
    }

    public void setMainModel(ResourceLocation mainModel) {
        this.mainModel = mainModel;
    }

    public ResourceLocation getAnimation() {
        if (GeckoLibCache.getInstance().getAnimations().containsKey(this.mainModel)) {
            return this.mainModel;
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

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public String getPreviewAnimation() {
        return this.previewAnimation;
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
        return this.hasPreviewAnimation() && previewAnimation.equals(this.previewAnimation);
    }

    public ItemStack[] getHandItemsForAnimation() {
        return this.handItemsForAnimation;
    }
}
