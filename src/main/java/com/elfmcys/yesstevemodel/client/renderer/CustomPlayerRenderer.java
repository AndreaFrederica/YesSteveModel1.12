package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.model.CustomPlayerModel;
import com.elfmcys.yesstevemodel.client.renderer.layer.CustomPlayerItemInHandLayer;
import com.elfmcys.yesstevemodel.event.api.SpecialPlayerRenderEvent;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.util.Keep;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

public class CustomPlayerRenderer extends GeoReplacedEntityRenderer<CustomPlayerEntity> {
    @SuppressWarnings("all")
    public CustomPlayerRenderer(EntityRendererManager ctx) {
        super(ctx, new CustomPlayerModel(), new CustomPlayerEntity());
        addLayer(new CustomPlayerItemInHandLayer<>(this));
    }


    @Override
    @Keep
    public void render(Entity entity, float entityYaw, float partialTick, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int packedLight) {
        if (this.animatable != null && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
                this.animatable.setPlayer(player);
                this.animatable.setMainModel(ModelIdUtil.getMainId(cap.getModelId()));
                this.animatable.setTexture(cap.getSelectTexture());
            });
            if (MinecraftForge.EVENT_BUS.post(new SpecialPlayerRenderEvent(player, this.animatable, ModelIdUtil.getModelIdFromMainId(this.animatable.getMainModel())))) {
                return;
            }
        }
        ResourceLocation location = this.modelProvider.getModelLocation(animatable);
        GeoModel geoModel = GeckoLibCache.getInstance().getGeoModels().get(location);
        if (geoModel != null) {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    @Keep
    public RenderType getRenderType(Object animatable, float partialTick, MatrixStack poseStack, @Nullable IRenderTypeBuffer bufferSource, @Nullable IVertexBuilder buffer, int packedLight, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    @Keep
    public boolean shouldShowName(Entity entity) {
        double distance = this.entityRenderDispatcher.distanceToSqr(entity);
        float renderDistance = entity.isDiscrete() ? 32.0F : 64.0F;
        if (distance >= (double) (renderDistance * renderDistance)) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            ClientPlayerEntity player = minecraft.player;
            if (player == null) {
                return false;
            }
            boolean invisible = !entity.isInvisibleTo(player);
            if (entity != player) {
                Team team1 = entity.getTeam();
                Team team2 = player.getTeam();
                if (team1 != null) {
                    Team.Visible visibility = team1.getNameTagVisibility();
                    switch (visibility) {
                        case ALWAYS:
                            return invisible;
                        case NEVER:
                            return false;
                        case HIDE_FOR_OTHER_TEAMS:
                            return team2 == null ? invisible : team1.isAlliedTo(team2) && (team1.canSeeFriendlyInvisibles() || invisible);
                        case HIDE_FOR_OWN_TEAM:
                            return team2 == null ? invisible : !team1.isAlliedTo(team2) && invisible;
                        default:
                            throw new IllegalArgumentException();
                    }
                }
            }
            return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && invisible && !entity.isVehicle();
        }
    }

    @Override
    @Keep
    @SuppressWarnings("all")
    protected void renderNameTag(Entity entity, ITextComponent displayName, MatrixStack poseStack, IRenderTypeBuffer buffer, int packedLight) {
        double distance = this.entityRenderDispatcher.distanceToSqr(entity);
        poseStack.pushPose();
        if (distance < 100 && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            Scoreboard scoreboard = player.getScoreboard();
            ScoreObjective objective = scoreboard.getDisplayObjective(2);
            if (objective != null) {
                Score score = scoreboard.getOrCreatePlayerScore(player.getScoreboardName(), objective);
                super.renderNameTag(player, (new StringTextComponent(Integer.toString(score.getScore()))).append(" ").append(objective.getDisplayName()), poseStack, buffer, packedLight);
                poseStack.translate(0, 9.0 * 1.15 * 0.025, 0);
            }
        }
        super.renderNameTag(entity, displayName, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    @Override
    @Keep
    public float getWidthScale(Object animatable) {
        if (this.animatable != null) {
            return this.animatable.getWidthScale();
        }
        return super.getWidthScale(animatable);
    }

    @Override
    @Keep
    public float getHeightScale(Object animatable) {
        if (this.animatable != null) {
            return this.animatable.getHeightScale();
        }
        return super.getHeightScale(animatable);
    }
}
