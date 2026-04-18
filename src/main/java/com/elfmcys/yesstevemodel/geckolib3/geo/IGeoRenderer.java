package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoMesh;
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.MatrixStack;
import com.elfmcys.yesstevemodel.geckolib3.util.VectorUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

public interface IGeoRenderer<T> {
    MatrixStack MATRIX_STACK = new MatrixStack();
    Vector3f C000 = new Vector3f();
    Vector3f C100 = new Vector3f();
    Vector3f C110 = new Vector3f();
    Vector3f C010 = new Vector3f();
    Vector3f C001 = new Vector3f();
    Vector3f C101 = new Vector3f();
    Vector3f C111 = new Vector3f();
    Vector3f C011 = new Vector3f();
    Vector3f dx = new Vector3f();
    Vector3f dy = new Vector3f();
    Vector3f dz = new Vector3f();
    Vector3f nx = new Vector3f();
    Vector3f ny = new Vector3f();
    Vector3f nz = new Vector3f();

    default void render(
            AnimatedGeoModel model, T entity, float partialTick,
            float red, float green, float blue, float alpha
    ) {
        this.renderEarly(entity, partialTick, red, green, blue, alpha);
        this.renderLate(entity, partialTick, red, green, blue, alpha);

        Tessellator tess = Tessellator.getInstance();
        tess.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        // 渲染所有根骨骼
        for (AnimatedGeoBone group : model.topLevelBones()) {
            this.renderRecursively(group, tess, red, green, blue, alpha);
        }
        tess.draw();
        // 由于此时我们至少渲染了一次，因此让我们将循环设置为重复
        this.setCurrentModelRenderCycle(EModelRenderCycle.REPEATED);
    }

    default void renderRecursively(
            AnimatedGeoBone bone, Tessellator tess,
            float red, float green, float blue, float alpha
    ) {
        if ((bone.getScaleX() == 0 ? 0 : 1) + (bone.getScaleY() == 0 ? 0 : 1) + (bone.getScaleZ() == 0 ? 0 : 1) < 2) {
            return;
        }
        MATRIX_STACK.push();
        MATRIX_STACK.prep(bone);
        BufferBuilder buffer = tess.getBuffer();
//        if (!SodiumCompat.sodiumRenderCubesOfBone(bone, poseStack, buffer, cubePackedLight, packedOverlay, red, green, blue, alpha)) {
        if (bone.geoBone().glow()) {
            // 先绘制出已有的顶点
            tess.draw();
            // 设置自发光条件
            boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
            GlStateManager.disableLighting();
            float lastX = OpenGlHelper.lastBrightnessX;
            float lastY = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            // 绘制特殊的顶点
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            this.renderCubesOfBone(bone, buffer, red, green, blue, alpha);
            tess.draw();
            // 恢复状态
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
            if (lighting) GlStateManager.enableLighting();
            else GlStateManager.disableLighting();
            // 重新开始绘制
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        } else {
            this.renderCubesOfBone(bone, buffer, red, green, blue, alpha);
        }
//        }
        this.renderChildBones(bone, tess, red, green, blue, alpha);
        MATRIX_STACK.pop();
    }

    default void renderCubesOfBone(
            AnimatedGeoBone bone, BufferBuilder buffer,
            float red, float green, float blue, float alpha
    ) {
        if (bone.isHidden()) {
            return;
        }
        if (bone.cubesAreHidden()) {
            return;
        }

        GeoMesh mesh = bone.geoBone().cubes();

        for (int i = 0; i < mesh.cubeCount(); i++) {
            Matrix4f pose = MATRIX_STACK.getModelMatrix();
            VectorUtils.mulPosition(pose, mesh.position(i), C000);
            pose.transform(mesh.dx(i), dx);
            pose.transform(mesh.dy(i), dy);
            pose.transform(mesh.dz(i), dz);

            C100.add(C000, dx);
            C110.add(C100, dy);
            C010.add(C000, dy);
            C001.add(C000, dz);
            C101.add(C100, dz);
            C111.add(C110, dz);
            C011.add(C010, dz);

            nx.cross(dy, dz);
            nx.normalize();
            ny.cross(dz, dx);
            ny.normalize();
            nz.cross(dx, dy);
            nz.normalize();

            int faces = mesh.faces(i);
            boolean mirrored = (faces & 0b1000000) != 0;
            if (mirrored) {
                nx.scale(-1);
                ny.scale(-1);
                nz.scale(-1);
            }

            if ((faces & 0b000001) != 0) // DOWN
            {
                buildVertex(buffer, C101.x, C101.y, C101.z, red, green, blue, alpha, mesh.downU0(i), mesh.downV1(i),
                        -ny.x, -ny.y, -ny.z);
                buildVertex(buffer, C001.x, C001.y, C001.z, red, green, blue, alpha, mesh.downU1(i), mesh.downV1(i),
                        -ny.x, -ny.y, -ny.z);
                buildVertex(buffer, C000.x, C000.y, C000.z, red, green, blue, alpha, mesh.downU1(i), mesh.downV0(i),
                        -ny.x, -ny.y, -ny.z);
                buildVertex(buffer, C100.x, C100.y, C100.z, red, green, blue, alpha, mesh.downU0(i), mesh.downV0(i),
                        -ny.x, -ny.y, -ny.z);
            }
            if ((faces & 0b000010) != 0) // UP
            {
                buildVertex(buffer, C110.x, C110.y, C110.z, red, green, blue, alpha, mesh.upU0(i), mesh.upV1(i),
                        ny.x, ny.y, ny.z);
                buildVertex(buffer, C010.x, C010.y, C010.z, red, green, blue, alpha, mesh.upU1(i), mesh.upV1(i),
                        ny.x, ny.y, ny.z);
                buildVertex(buffer, C011.x, C011.y, C011.z, red, green, blue, alpha, mesh.upU1(i), mesh.upV0(i),
                        ny.x, ny.y, ny.z);
                buildVertex(buffer, C111.x, C111.y, C111.z, red, green, blue, alpha, mesh.upU0(i), mesh.upV0(i),
                        ny.x, ny.y, ny.z);
            }
            if ((faces & 0b000100) != 0) // NORTH
            {
                buildVertex(buffer, C100.x, C100.y, C100.z, red, green, blue, alpha, mesh.northU0(i), mesh.northV1(i),
                        -nz.x, -nz.y, -nz.z);
                buildVertex(buffer, C000.x, C000.y, C000.z, red, green, blue, alpha, mesh.northU1(i), mesh.northV1(i),
                        -nz.x, -nz.y, -nz.z);
                buildVertex(buffer, C010.x, C010.y, C010.z, red, green, blue, alpha, mesh.northU1(i), mesh.northV0(i),
                        -nz.x, -nz.y, -nz.z);
                buildVertex(buffer, C110.x, C110.y, C110.z, red, green, blue, alpha, mesh.northU0(i), mesh.northV0(i),
                        -nz.x, -nz.y, -nz.z);
            }
            if ((faces & 0b001000) != 0) // SOUTH
            {
                buildVertex(buffer, C001.x, C001.y, C001.z, red, green, blue, alpha, mesh.southU0(i), mesh.southV1(i),
                        nz.x, nz.y, nz.z);
                buildVertex(buffer, C101.x, C101.y, C101.z, red, green, blue, alpha, mesh.southU1(i), mesh.southV1(i),
                        nz.x, nz.y, nz.z);
                buildVertex(buffer, C111.x, C111.y, C111.z, red, green, blue, alpha, mesh.southU1(i), mesh.southV0(i),
                        nz.x, nz.y, nz.z);
                buildVertex(buffer, C011.x, C011.y, C011.z, red, green, blue, alpha, mesh.southU0(i), mesh.southV0(i),
                        nz.x, nz.y, nz.z);
            }
            if ((faces & 0b010000) != 0) // WEST
            {
                buildVertex(buffer, C000.x, C000.y, C000.z, red, green, blue, alpha, mesh.westU0(i), mesh.westV1(i),
                        -nx.x, -nx.y, -nx.z);
                buildVertex(buffer, C001.x, C001.y, C001.z, red, green, blue, alpha, mesh.westU1(i), mesh.westV1(i),
                        -nx.x, -nx.y, -nx.z);
                buildVertex(buffer, C011.x, C011.y, C011.z, red, green, blue, alpha, mesh.westU1(i), mesh.westV0(i),
                        -nx.x, -nx.y, -nx.z);
                buildVertex(buffer, C010.x, C010.y, C010.z, red, green, blue, alpha, mesh.westU0(i), mesh.westV0(i),
                        -nx.x, -nx.y, -nx.z);
            }
            if ((faces & 0b100000) != 0) // EAST
            {
                buildVertex(buffer, C101.x, C101.y, C101.z, red, green, blue, alpha, mesh.eastU0(i), mesh.eastV1(i),
                        nx.x, nx.y, nx.z);
                buildVertex(buffer, C100.x, C100.y, C100.z, red, green, blue, alpha, mesh.eastU1(i), mesh.eastV1(i),
                        nx.x, nx.y, nx.z);
                buildVertex(buffer, C110.x, C110.y, C110.z, red, green, blue, alpha, mesh.eastU1(i), mesh.eastV0(i),
                        nx.x, nx.y, nx.z);
                buildVertex(buffer, C111.x, C111.y, C111.z, red, green, blue, alpha, mesh.eastU0(i), mesh.eastV0(i),
                        nx.x, nx.y, nx.z);
            }
        }
    }

    static void buildVertex(
            BufferBuilder buffer,
            float x, float y, float z,
            float red, float green, float blue, float alpha,
            float texU, float texV,
            float normalX, float normalY, float normalZ
    ) {
        buffer.pos(x, y, z).tex(texU, texV).color(red, green, blue, alpha).normal(normalX, normalY, normalZ).endVertex();
    }

    default void renderChildBones(
            AnimatedGeoBone bone, Tessellator tess,
            float red, float green, float blue, float alpha
    ) {
        if (bone.childBonesAreHiddenToo()) {
            return;
        }
        for (AnimatedGeoBone childBone : bone.children()) {
            this.renderRecursively(childBone, tess, red, green, blue, alpha);
        }
    }

    default void renderEarly(
            T entity, float partialTick, float red, float green, float blue, float alpha
    ) {
        if (this.getCurrentModelRenderCycle() == EModelRenderCycle.INITIAL) {
            float width = this.getWidthScale(entity);
            float height = this.getHeightScale(entity);
            GlStateManager.scale(width, height, width);
        }
    }

    default void renderLate(
            T entity, float partialTick, float red, float green, float blue, float alpha
    ) {
    }

    default Color getRenderColor(T entity, float partialTicks) {
        return Color.WHITE;
    }

    @Nonnull
    default IRenderCycle getCurrentModelRenderCycle() {
        return EModelRenderCycle.INITIAL;
    }

    default void setCurrentModelRenderCycle(IRenderCycle cycle) {
    }

    default float getWidthScale(T entity) {
        return 1F;
    }

    default float getHeightScale(T entity) {
        return 1F;
    }
}
