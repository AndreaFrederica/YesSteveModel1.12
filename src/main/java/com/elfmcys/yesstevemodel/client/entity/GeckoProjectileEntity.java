package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.model.ProjectileModelBundle;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class GeckoProjectileEntity<T extends Entity> extends GeoEntity<T> {
    private static final ResourceLocation DEFAULT_ID = ModelIdUtil.getMainId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    private static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(YesSteveModel.MOD_ID, "default/arrow.png");

    private ResourceLocation ownerModelId;
    private ResourceLocation projectileId;
    private ModelAssembly modelAssembly;
    private ProjectileModelBundle modelBundle;
    private boolean controllersInstalled;

    public GeckoProjectileEntity(@Nullable T entity, int fps) {
        super(entity, fps);
    }

    public void setProjectileModel(ResourceLocation ownerModelId, ResourceLocation projectileId) {
        if (ownerModelId.equals(this.ownerModelId) && projectileId.equals(this.projectileId)) {
            return;
        }
        this.ownerModelId = ownerModelId;
        this.projectileId = projectileId;
        this.modelAssembly = ClientModelManager.getModernModel(ownerModelId);
        this.modelBundle = this.modelAssembly != null ? this.modelAssembly.getProjectileModels().get(projectileId) : null;
        this.controllersInstalled = false;
        this.clearAnimationControllers();
        if (this.modelAssembly != null) {
            this.setAnimationMap(this.modelAssembly.getResourceBundle().getEvents());
        }
    }

    public void installControllers() {
        if (this.controllersInstalled || this.modelBundle == null) {
            return;
        }
        this.modelBundle.getControllerInitializer().accept(this);
        this.controllersInstalled = true;
    }

    @Nullable
    @Override
    public ModelAssembly getModelAssembly() {
        return this.modelAssembly;
    }

    @Nullable
    @Override
    public Animation getAnimation(String name) {
        if (this.modelBundle != null) {
            Animation animation = this.modelBundle.getAnimations().get(name);
            if (animation != null) {
                return animation;
            }
        }
        return super.getAnimation(name);
    }

    @Nullable
    @Override
    public AnimationController getAnimationEntries(String controllerName) {
        if (this.modelBundle != null) {
            return this.modelBundle.getAnimationControllers().get(controllerName);
        }
        return super.getAnimationEntries(controllerName);
    }

    @Nullable
    @Override
    public ResourceLocation getModelLocation() {
        return this.ownerModelId != null && this.projectileId != null ? ClientModelManager.getModernProjectileModelId(this.ownerModelId, this.projectileId) : DEFAULT_ID;
    }

    @Nullable
    @Override
    public ResourceLocation getTextureLocation() {
        return this.ownerModelId != null && this.projectileId != null ? ClientModelManager.getModernProjectileTextureId(this.ownerModelId, this.projectileId) : DEFAULT_TEXTURE;
    }

    @Nullable
    @Override
    public ResourceLocation getAnimationFileLocation() {
        return this.getModelLocation();
    }
}
