package com.elfmcys.yesstevemodel.geckolib3.geo.render.built;

import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ModelProperties;
import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import java.util.Arrays;
import java.util.List;

@Desugar
public class GeoModel {
    private final List<GeoBone> topLevelBones;
    private final ModelProperties properties;
    private final com.elfmcys.yesstevemodel.resource.models.GeometryDescription geometryProperties;
    private boolean[] translucentTexture;

    public List<BakedBone> bakedBones = ObjectLists.emptyList();
    public final float[] boneTransformData = new float[0];

    public static class BakedBone {
        public String name;
        public boolean glow;
        public int parentIdx = -1;
        public float pivotX;
        public float pivotY;
        public float pivotZ;
        public float rotX;
        public float rotY;
        public float rotZ;
        public List<BakedCube> cubes = new ObjectArrayList<>();
        public int partMask;
    }

    public static class BakedCube {
        public boolean cullable;
        public List<BakedQuad> quads = new ObjectArrayList<>();
    }

    public static class BakedQuad {
        public org.joml.Vector3f[] positions = new org.joml.Vector3f[4];
        public org.joml.Vector2f[] uvs = new org.joml.Vector2f[4];
        public org.joml.Vector3f normal;
    }

    public GeoModel(List<GeoBone> topLevelBones, ModelProperties properties) {
        this.topLevelBones = ObjectLists.unmodifiable(new ObjectArrayList<>(topLevelBones));
        this.properties = properties;
        this.geometryProperties = convertGeometry(properties);
        this.translucentTexture = new boolean[1];
    }

    public GeoModel(GeoBone[] geoBones, String[][] boneNameArrays, boolean[] flags, com.elfmcys.yesstevemodel.resource.models.GeometryDescription properties, boolean[] translucencyArray) {
        this.topLevelBones = ObjectLists.unmodifiable(new ObjectArrayList<>(Arrays.asList(geoBones)));
        this.properties = convertLegacyProperties(properties);
        this.geometryProperties = properties;
        this.translucentTexture = translucencyArray == null ? new boolean[1] : Arrays.copyOf(translucencyArray, Math.max(1, translucencyArray.length));
    }

    public List<GeoBone> topLevelBones() {
        return this.topLevelBones;
    }

    public ModelProperties properties() {
        return this.properties;
    }

    public com.elfmcys.yesstevemodel.resource.models.GeometryDescription getProperties() {
        return this.geometryProperties;
    }

    public float[] getBoneTransformData() {
        return this.boneTransformData;
    }

    public boolean isTranslucentTexture(int index) {
        if (index < 0 || index >= this.translucentTexture.length) {
            return false;
        }
        return this.translucentTexture[index];
    }

    public void setTranslucentTexture(int index, boolean translucent) {
        ensureTranslucentTextureCapacity(index);
        this.translucentTexture[index] = translucent;
    }

    public void buildNativeCache() {
    }

    public void freeNativeCache() {
    }

    private void ensureTranslucentTextureCapacity(int maxIndex) {
        if (maxIndex >= this.translucentTexture.length) {
            boolean[] expanded = new boolean[maxIndex + 1];
            System.arraycopy(this.translucentTexture, 0, expanded, 0, this.translucentTexture.length);
            this.translucentTexture = expanded;
        }
    }

    private static com.elfmcys.yesstevemodel.resource.models.GeometryDescription convertGeometry(ModelProperties properties) {
        if (properties == null) {
            return null;
        }
        return new com.elfmcys.yesstevemodel.resource.models.GeometryDescription(
                properties.getIdentifier(),
                properties.getTextureWidth(),
                properties.getTextureHeight(),
                properties.getVisibleBoundsWidth(),
                properties.getVisibleBoundsHeight(),
                properties.getVisibleBoundsOffset()
        );
    }

    private static ModelProperties convertLegacyProperties(com.elfmcys.yesstevemodel.resource.models.GeometryDescription properties) {
        ModelProperties converted = new ModelProperties();
        if (properties == null) {
            return converted;
        }
        converted.setIdentifier(properties.getIdentifier());
        converted.setTextureWidth(properties.getTextureWidth());
        converted.setTextureHeight(properties.getTextureHeight());
        converted.setVisibleBoundsWidth(properties.getVisibleBoundsWidth());
        converted.setVisibleBoundsHeight(properties.getVisibleBoundsHeight());
        converted.setVisibleBoundsOffset(properties.getVisibleBoundsOffset());
        return converted;
    }
}
