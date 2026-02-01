package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.*;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.GeoModelProvider;
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

@SuppressWarnings("unused")
public interface IGeoRenderer<T> {
    MatrixStack MATRIX_STACK = new MatrixStack();
    String GLOW_PREFIX = "ysmGlow";

    @SuppressWarnings("rawtypes")
    GeoModelProvider getGeoModelProvider();

    ResourceLocation getTextureLocation(T animatable);

    @Nullable
    default GeoModel getGeoModel() {
        return null;
    }

    // TODO
    default void render(
            GeoModel model, T animatable, float partialTicks,
            float red, float green, float blue, float alpha
    ) {
        GlStateManager.disableCull();
        GlStateManager.enableRescaleNormal();

        this.renderEarly(animatable, partialTicks, red, green, blue, alpha);
        this.renderLate(animatable, partialTicks, red, green, blue, alpha);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder builder = tess.getBuffer();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        // 渲染所有根骨骼
        for (GeoBone group : model.topLevelBones) {
            this.renderRecursively(builder, group, red, green, blue, alpha);
        }
        tess.draw();
        // 由于此时我们至少渲染了一次，因此让我们将循环设置为重复
        this.setCurrentModelRenderCycle(EModelRenderCycle.REPEATED);
        GlStateManager.enableCull();
    }

    default void renderRecursively(
            BufferBuilder builder, GeoBone bone,
            float red, float green, float blue, float alpha
    ) {
        MATRIX_STACK.push();
        IGeoRenderer.MATRIX_STACK.translate(bone);
        IGeoRenderer.MATRIX_STACK.moveToPivot(bone);
        IGeoRenderer.MATRIX_STACK.rotate(bone);
        IGeoRenderer.MATRIX_STACK.scale(bone);
        IGeoRenderer.MATRIX_STACK.moveBackFromPivot(bone);
        if (bone.getName().startsWith(GLOW_PREFIX)) {
            float lastX = OpenGlHelper.lastBrightnessX;
            float lastY = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            this.renderCubesOfBone(builder, bone, red, green, blue, alpha);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
        } else {
            this.renderCubesOfBone(builder, bone, red, green, blue, alpha);
        }
        this.renderChildBones(builder, bone, red, green, blue, alpha);
        MATRIX_STACK.pop();
    }

    default void renderCubesOfBone(
            BufferBuilder builder, GeoBone bone,
            float red, float green, float blue, float alpha
    ) {
        if (bone.isHidden()) {
            return;
        }
        for (GeoCube cube : bone.childCubes) {
            if (!bone.cubesAreHidden()) {
                MATRIX_STACK.push();
                this.renderCube(builder, cube, red, green, blue, alpha);
                MATRIX_STACK.pop();
            }
        }
    }

    default void renderChildBones(
            BufferBuilder builder, GeoBone bone,
            float red, float green, float blue, float alpha
    ) {
        if (bone.childBonesAreHiddenToo()) {
            return;
        }
        for (GeoBone childBone : bone.childBones) {
            this.renderRecursively(builder, childBone, red, green, blue, alpha);
        }
    }

    default void renderCube(BufferBuilder builder, GeoCube cube, float red, float green, float blue, float alpha) {
        MATRIX_STACK.moveToPivot(cube);
        MATRIX_STACK.rotate(cube);
        MATRIX_STACK.moveBackFromPivot(cube);
        for (GeoQuad quad : cube.quads) {
            if (quad == null) {
                continue;
            }
            Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
            MATRIX_STACK.getNormalMatrix().transform(normal);
            if ((cube.size.y == 0 || cube.size.z == 0) && normal.getX() < 0) {
                normal.x *= -1;
            }
            if ((cube.size.x == 0 || cube.size.z == 0) && normal.getY() < 0) {
                normal.y *= -1;
            }
            if ((cube.size.x == 0 || cube.size.y == 0) && normal.getZ() < 0) {
                normal.z *= -1;
            }
            this.createVerticesOfQuad(quad, normal, builder, red, green, blue, alpha);
        }
    }

    default void createVerticesOfQuad(
            GeoQuad quad, Vector3f normal, BufferBuilder builder,
            float red, float green, float blue, float alpha
    ) {
        for (GeoVertex vertex : quad.vertices) {
            Vector4f vector4f = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1);
            MATRIX_STACK.getModelMatrix().transform(vector4f);
            builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).tex(vertex.textureU, vertex.textureV)
                    .color(red, green, blue, alpha).normal(normal.getX(), normal.getY(), normal.getZ()).endVertex();
        }
    }

    default void renderEarly(T animatable, float ticks, float red, float green, float blue, float partialTicks) {
        if (this.getCurrentModelRenderCycle() == EModelRenderCycle.INITIAL) {
            float width = this.getWidthScale(animatable);
            float height = this.getHeightScale(animatable);
            GlStateManager.scale(width, height, width);
        }
    }

    default void renderLate(T animatable, float ticks, float red, float green, float blue, float partialTicks) {
    }

    default Color getRenderColor(T animatable, float partialTicks) {
        return Color.WHITE;
    }

    default int getInstanceId(T animatable) {
        return animatable.hashCode();
    }

    @Nonnull
    default IRenderCycle getCurrentModelRenderCycle() {
        return EModelRenderCycle.INITIAL;
    }

    default void setCurrentModelRenderCycle(IRenderCycle cycle) {
    }

    default float getWidthScale(T animatable) {
        return 1F;
    }

    default float getHeightScale(T entity) {
        return 1F;
    }
}
