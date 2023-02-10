package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.AnimationRegister;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.model.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.data.EntityModelData;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.RenderUtils;
import com.elfmcys.yesstevemodel.util.Keep;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.IHasArm;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("all")
public class CustomPlayerModel extends AnimatedGeoModel implements IHasArm {
    public static final ResourceLocation DEFAULT_MAIN_MODEL = ModelIdUtil.getMainId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    public static final ResourceLocation DEFAULT_MAIN_ANIMATION = ModelIdUtil.getMainId(new ResourceLocation(YesSteveModel.MOD_ID, "default"));
    public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation(YesSteveModel.MOD_ID, "default/default.png");

    @Override
    @Keep
    public ResourceLocation getModelLocation(Object object) {
        if (object instanceof CustomPlayerEntity) {
            CustomPlayerEntity customPlayer = (CustomPlayerEntity) object;
            return customPlayer.getMainModel();
        }
        return DEFAULT_MAIN_MODEL;
    }

    @Override
    @Keep
    public ResourceLocation getTextureLocation(Object object) {
        if (object instanceof CustomPlayerEntity) {
            CustomPlayerEntity customPlayer = (CustomPlayerEntity) object;
            return customPlayer.getTexture();
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    @Keep
    public ResourceLocation getAnimationFileLocation(Object object) {
        if (object instanceof CustomPlayerEntity) {
            CustomPlayerEntity customPlayer = (CustomPlayerEntity) object;
            return customPlayer.getAnimation();
        }
        return DEFAULT_MAIN_ANIMATION;
    }

    @Override
    @Keep
    public void setCustomAnimations(IAnimatable animatable, int instanceId, AnimationEvent animationEvent) {
        super.setCustomAnimations(animatable, instanceId, animationEvent);
        List extraData = animationEvent.getExtraData();
        MolangParser parser = GeckoLibCache.getInstance().parser;
        if (!Minecraft.getInstance().isPaused() && extraData.size() == 1 && extraData.get(0) instanceof EntityModelData && animatable instanceof CustomPlayerEntity) {
            CustomPlayerEntity customPlayer = (CustomPlayerEntity) animatable;
            PlayerEntity player = customPlayer.getPlayer();
            EntityModelData data = (EntityModelData) extraData.get(0);
            if (player == null) {
                return;
            }
            AnimationRegister.setParserValue(animationEvent, parser, data, player);
            this.codeAnimation(animationEvent, data, player);
        }
    }

    private void codeAnimation(AnimationEvent animationEvent, EntityModelData data, PlayerEntity player) {
        IBone head = getBone("Head");
        if (head != null) {
            head.setRotationX(head.getRotationX() + (float) Math.toRadians(data.headPitch));
            head.setRotationY(head.getRotationY() + (float) Math.toRadians(data.netHeadYaw));
        }
    }

    @Override
    @Keep
    public void translateToHand(HandSide arm, MatrixStack poseStack) {
        if (arm == HandSide.LEFT) {
            IBone leftHandLocator = getBone("LeftHandLocator");
            moveToBone(poseStack, "LeftHand");
            if (leftHandLocator instanceof GeoBone) {
                GeoBone cube = (GeoBone) leftHandLocator;
                RenderUtils.translateAndRotateMatrixForBone(poseStack, cube);
            }
        } else {
            moveToBone(poseStack, "RightHand");
            IBone rightHandLocator = getBone("RightHandLocator");
            if (rightHandLocator instanceof GeoBone) {
                GeoBone cube = (GeoBone) rightHandLocator;
                RenderUtils.translateAndRotateMatrixForBone(poseStack, cube);
            }
        }
    }

    private void moveToBone(MatrixStack poseStack, String name) {
        IBone bone = getBone(name);
        if (bone instanceof GeoBone) {
            List<GeoBone> boneList = new ArrayList<>();
            GeoBone geoBone = (GeoBone) bone;
            getBoneParent(geoBone, boneList);
            Collections.reverse(boneList);
            for (GeoBone subBone : boneList) {
                RenderUtils.prepMatrixForBone(poseStack, subBone);
            }
        }
    }

    private void getBoneParent(GeoBone bone, List<GeoBone> boneList) {
        boneList.add(bone);
        if (bone.parent != null) {
            getBoneParent(bone.parent, boneList);
        }
    }

    @Override
    @Keep
    @Nullable
    public IBone getBone(String boneName) {
        return getAnimationProcessor().getBone(boneName);
    }

    @Override
    @Keep
    public void setMolangQueries(IAnimatable animatable, double seekTime) {
    }
}
