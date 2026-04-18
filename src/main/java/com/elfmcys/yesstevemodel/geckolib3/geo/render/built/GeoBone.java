package com.elfmcys.yesstevemodel.geckolib3.geo.render.built;

import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneTopLevelSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import javax.vecmath.Vector3f;
import java.util.List;

public class GeoBone {
    private static final String GLOWING_PREFIX = "ysmGlow";

    private GeoBone parent;
    private final List<GeoBone> children;

    private final String name;
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

    public GeoBone(List<GeoBone> children, String name, Vector3f pivot, Vector3f rotation, GeoMesh mesh, Boolean mirror, Double inflate, Boolean dontRender, Boolean reset) {
        this.children = ObjectLists.unmodifiable(new ObjectArrayList<>(children));
        this.name = name;
        this.pivot = pivot;
        this.rotation = rotation;
        this.cubes = mesh;

        this.mirror = mirror;
        this.inflate = inflate;
        this.dontRender = dontRender;
        this.reset = reset;

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

    public Vector3f pivot() {
        return this.pivot;
    }

    public Vector3f rotation() {
        return this.rotation;
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

    public void setParent(GeoBone parent) {
        this.parent = parent;
    }
}
