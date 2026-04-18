package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.animation.AnimationManager;
import com.elfmcys.yesstevemodel.client.animation.molang.ClientChatDebugOutputSink;
import com.elfmcys.yesstevemodel.client.input.DebugAnimationKey;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.DebugOutputSink;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import java.util.List;

public class CustomPlayerEntity extends AnimatableEntity<EntityPlayer> {
    public static final ResourceLocation DEFAULT_ID = ModelIdUtil.getMainId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(YesSteveModel.MOD_ID, "default/default.png");
    private static final int FPS = 60;

    private final ItemStack[] handItemsForAnimation = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY};
    private final Vector2f headRot = new Vector2f();
    private final @Nullable PlayerState state;
    private ResourceLocation mainModel = DEFAULT_ID;
    private ResourceLocation texture = DEFAULT_TEXTURE;
    private String previewAnimation = StringUtils.EMPTY;
    private float currentTick = -1;
    private boolean modelDirty = false;

    public CustomPlayerEntity(@Nullable EntityPlayer player) {
        super(player, FPS);
        this.state = player != null ? new PlayerState(player) : null;
        this.registerControllers();
    }

    private void registerControllers() {
        AnimationManager manager = AnimationManager.getInstance();
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("pre_parallel_%d_controller", i);
            String animationName = String.format("pre_parallel%d", i);
            this.addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateParallel(e, animationName)));
        }
        this.addAnimationController(new AnimationController<>(this, "main", 2, manager::predicateMain));
        this.addAnimationController(new AnimationController<>(this, "hold_offhand", 0, manager::predicateOffhandHold));
        this.addAnimationController(new AnimationController<>(this, "hold_mainhand", 0, manager::predicateMainhandHold));
        this.addAnimationController(new AnimationController<>(this, "swing", 0, manager::predicateSwing));
        this.addAnimationController(new AnimationController<>(this, "use", 2, manager::predicateUse));
//        this.addAnimationController(new AnimationController<>(this, "magic_casting", 2, manager::predicateMagicCastingAnimation));
//        this.addAnimationController(new AnimationController<>(this, "misc", 2, manager::predicateMisc));
        this.addAnimationController(new AnimationController<>(this, "passenger", 2, manager::predicatePassengerAnimation));
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("parallel_%d_controller", i);
            String animationName = String.format("parallel%d", i);
            this.addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateParallel(e, animationName)));
        }
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                String controllerName = String.format("%s_controller", slot.getName());
                this.addAnimationController(new AnimationController<>(this, controllerName, 0, e -> manager.predicateArmor(e, slot)));
            }
        }
        this.addAnimationController(new AnimationController<>(this, "cap_controller", 2, manager::predicateCap));
    }

    @Override
    public boolean setCustomAnimations(AnimationContext<?> context, @Nonnull AnimationEvent<?> event) {
        List<Object> extraData = event.getExtraData();
        //MolangParser parser = GeckoLibCache.getInstance().parser;
        if (!Minecraft.getMinecraft().isGamePaused() && extraData.size() == 1 && extraData.get(0) instanceof EntityModelData data) {
            var update = super.setCustomAnimations(context, event);
            AnimatedGeoModel currentModel = this.getCurrentModel();
            if (currentModel != null) {
                this.updateHead(data, currentModel, update);
            }
            return update;
        } else {
            return super.setCustomAnimations(context, event);
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void updateHead(EntityModelData data, @Nonnull AnimatedGeoModel currentModel, boolean update) {
        if (currentModel.head() != null) {
            IBone head = currentModel.head();
            if (update) {
                this.headRot.set(head.getRotationX(), head.getRotationY());
            }
            head.setRotationX(this.headRot.x + (float) Math.toRadians(data.headPitch));
            head.setRotationY(this.headRot.y + (float) Math.toRadians(data.netHeadYaw));
        }
    }

    @Override
    protected boolean forceUpdate(AnimationEvent<?> animationEvent) {
        // UI 不锁帧
        if (this.state == null || this.entity == null) return true;
        // 对玩家自己不锁帧
        if (this.entity.equals(Minecraft.getMinecraft().player)) return true;

        // 姿态改变时，强制动画更新
        var tick = (float) this.getCurrentTick(animationEvent);
        if (tick > this.currentTick) {
            this.currentTick = tick;
            this.state.updateState();
            this.modelDirty = false;
            return false;
        }
        if (this.modelDirty || !this.state.compareState()) {
            this.state.updateState();
            this.modelDirty = false;
            return true;
        }
        return false;
    }

    public String getPreviewAnimation() {
        return this.previewAnimation;
    }

    public void setPreviewAnimation(String previewAnimation) {
        this.previewAnimation = previewAnimation;
    }

    public void clearPreviewAnimation() {
        this.previewAnimation = StringUtils.EMPTY;
    }

    public boolean hasPreviewAnimation() {
        return StringUtils.isNoneBlank(this.previewAnimation);
    }

    public boolean hasPreviewAnimation(String previewAnimation) {
        return this.hasPreviewAnimation() && previewAnimation.equals(this.previewAnimation);
    }

    public void setModelLocation(ResourceLocation mainModel) {
        this.mainModel = mainModel;
    }

    public void setTextureLocation(ResourceLocation texture) {
        this.texture = texture;
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

    public ItemStack[] getHandItemsForAnimation() {
        return this.handItemsForAnimation;
    }

    /*
    AnimatableEntity
     */

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

    @Nullable
    @Override
    public DebugOutputSink getDebugOutputSink() {
        Minecraft mc = Minecraft.getMinecraft();
        // 仅给玩家自己输出调试信息，且必须开启调试模式
        if (!DebugAnimationKey.DEBUG || this.entity == null || mc.player == null || this.entity != mc.player) {
            return null;
        }
        return ClientChatDebugOutputSink.INSTANCE;
    }

    private static class PlayerState {
        private final EntityPlayer player;

        private float yHeadRot = 0;
        private float yBodyRot = 0;

        private PlayerState(EntityPlayer player) {
            this.player = player;
        }

        public boolean compareState() {
            return this.yHeadRot == this.player.rotationYawHead && this.yBodyRot == this.player.renderYawOffset;
        }

        public void updateState() {
            this.yHeadRot = this.player.rotationYawHead;
            this.yBodyRot = this.player.renderYawOffset;
        }
    }
}
