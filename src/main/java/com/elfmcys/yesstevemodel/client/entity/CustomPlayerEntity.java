package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.animation.molang.ClientChatDebugOutputSink;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.input.DebugAnimationKey;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import java.util.List;

public class CustomPlayerEntity extends LivingAnimatable<EntityPlayer> {
    public static final ResourceLocation DEFAULT_ID = ModelIdUtil.getMainId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(YesSteveModel.MOD_ID, "default/default.png");
    private static final int FPS = 60;

    private final ItemStack[] handItemsForAnimation = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY};
    private final Vector2f headRot = new Vector2f();
    private final @Nullable PlayerState state;
    private boolean controllersInstalled = false;
    private ResourceLocation mainModel = DEFAULT_ID;
    private ResourceLocation texture = DEFAULT_TEXTURE;
    private String previewAnimation = StringUtils.EMPTY;
    private float currentTick = -1;
    private boolean modelDirty = false;

    public CustomPlayerEntity(@Nullable EntityPlayer player) {
        super(player, FPS);
        this.state = player != null ? new PlayerState(player) : null;
    }

    public void installControllers() {
        if (this.controllersInstalled) {
            return;
        }
        ModelAssembly assembly = this.getModelAssembly();
        if (assembly != null) {
            this.setModelConfig(assembly.getAnimationBundle().getConditionManager());
            this.onModelLoaded(assembly);
            var installer = assembly.getAnimationBundle().getPlayerControllerInstaller();
            if (installer != null) {
                installer.accept(this);
            }
            this.controllersInstalled = true;
        }
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
        if (!this.mainModel.equals(mainModel)) {
            this.controllersInstalled = false;
            this.resetInitFlag();
            this.setModelConfig(null);
            this.clearAnimationControllers();
        }
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

    @Nullable
    @Override
    public ModelAssembly getModelAssembly() {
        ModelAssembly assembly = ClientModelManager.getModernModel(this.mainModel);
        if (assembly != null) {
            return assembly;
        }
        if (this.mainModel.getPath().endsWith("/main")) {
            return ClientModelManager.getModernModel(ModelIdUtil.getModelIdFromMainId(this.mainModel));
        }
        return null;
    }

    @Nullable
    public com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager getConditionManager() {
        ModelAssembly assembly = this.getModelAssembly();
        return assembly != null ? assembly.getAnimationBundle().getConditionManager() : null;
    }

    @Nullable
    @Override
    public Animation getAnimation(String name) {
        ModelAssembly assembly = this.getModelAssembly();
        if (assembly != null) {
            Animation animation = assembly.getAnimationBundle().getMainAnimations().get(name);
            if (animation != null) {
                return animation;
            }
        }
        return super.getAnimation(name);
    }

    @Nullable
    @Override
    public AnimationController getAnimationEntries(String controllerName) {
        ModelAssembly assembly = this.getModelAssembly();
        if (assembly != null) {
            return assembly.getAnimationBundle().getAnimationControllers().get(controllerName);
        }
        return super.getAnimationEntries(controllerName);
    }

    public boolean isModelSwitching() {
        return false;
    }

    public boolean isDisabledState() {
        return false;
    }

    public void enableModel() {
    }

    public String getSelectedModelId() {
        return this.mainModel.toString();
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
