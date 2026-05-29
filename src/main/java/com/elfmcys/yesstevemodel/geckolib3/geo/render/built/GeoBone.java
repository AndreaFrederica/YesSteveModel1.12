package com.elfmcys.yesstevemodel.geckolib3.geo.render.built;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import javax.vecmath.Vector3f;
import java.util.List;

public class GeoBone {
    private static final String GLOWING_PREFIX = "ysmGlow";
    private static final GeoMesh EMPTY_MESH = new GeoMesh(0, new int[0], new Vector3f[0], new Vector3f[0], new Vector3f[0], new Vector3f[0], new float[0], new float[0], new float[0], new float[0]);

    private GeoBone parent;
    private final List<GeoBone> children;

    private final String name;
    private final int boneId;
    private final Vector3f pivot;
    private final Vector3f rotation;
    private final GeoMesh cubes;

    private final Boolean mirror;
    private final Double inflate;
    private final Boolean dontRender;
    /**
     * 我也不知道这个参数有啥用，但是 json 里面就有
     */
    private final Boolean reset;

    private final BoneSnapshot initialSnapshot;
    private final boolean glow;
    private final boolean hidden;
    private final boolean cubesHidden;
    private final boolean hideChildBonesToo;

    public int partMask = 0;
    public int parentIdx = -1;
    public String parentName = "";

    public GeoBone(List<GeoBone> children, String name, Vector3f pivot, Vector3f rotation, GeoMesh mesh, Boolean mirror, Double inflate, Boolean dontRender, Boolean reset) {
        this.children = ObjectLists.unmodifiable(new ObjectArrayList<>(children));
        this.name = name;
        this.boneId = StringPool.computeIfAbsent(name);
        this.pivot = pivot;
        this.rotation = rotation;
        this.cubes = mesh;

        this.mirror = mirror;
        this.inflate = inflate;
        this.dontRender = dontRender;
        this.reset = reset;

        this.hidden = Boolean.TRUE.equals(dontRender);
        this.cubesHidden = false;
        this.hideChildBonesToo = false;
        this.initialSnapshot = new BoneTopLevelSnapshot(new AnimatedGeoBone(this, null));
        this.glow = name.startsWith(GLOWING_PREFIX);
    }

    public GeoBone(String name, boolean isHidden, boolean areCubesHidden, boolean hideChildBonesToo, float pivotX, float pivotY, float pivotZ, float rotationX, float rotationY, float rotationZ) {
        this.children = ObjectLists.emptyList();
        this.name = name;
        this.boneId = StringPool.computeIfAbsent(name);
        this.pivot = new Vector3f(pivotX, pivotY, pivotZ);
        this.rotation = new Vector3f(rotationX, rotationY, rotationZ);
        this.cubes = EMPTY_MESH;
        this.mirror = null;
        this.inflate = null;
        this.dontRender = isHidden;
        this.reset = false;
        this.hidden = isHidden;
        this.cubesHidden = areCubesHidden;
        this.hideChildBonesToo = hideChildBonesToo;
        this.initialSnapshot = new BoneTopLevelSnapshot(new AnimatedGeoBone(this, null));
        this.glow = name.startsWith(GLOWING_PREFIX);
    }

    public GeoBone parent() {
        return this.parent;
    }

    public List<GeoBone> children() {
        return this.children;
    }

    public GeoMesh cubes() {
        return this.cubes;
    }

    public String name() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public int getBoneId() {
        return this.boneId;
    }

    public Vector3f pivot() {
        return this.pivot;
    }

    public float getPivotX() {
        return this.pivot.x;
    }

    public float getPivotY() {
        return this.pivot.y;
    }

    public float getPivotZ() {
        return this.pivot.z;
    }

    public Vector3f rotation() {
        return this.rotation;
    }

    public float getRotX() {
        return this.rotation.x;
    }

    public float getRotY() {
        return this.rotation.y;
    }

    public float getRotZ() {
        return this.rotation.z;
    }

    public Boolean mirror() {
        return this.mirror;
    }

    public Double inflate() {
        return this.inflate;
    }

    public Boolean dontRender() {
        return this.dontRender;
    }

    public Boolean reset() {
        return this.reset;
    }

    public BoneSnapshot initialSnapshot() {
        return this.initialSnapshot;
    }

    public boolean glow() {
        return this.glow;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean cubesAreHidden() {
        return this.cubesHidden;
    }

    public boolean childBonesAreHiddenToo() {
        return this.hideChildBonesToo;
    }

    public void setParent(GeoBone parent) {
        this.parent = parent;
    }
}
