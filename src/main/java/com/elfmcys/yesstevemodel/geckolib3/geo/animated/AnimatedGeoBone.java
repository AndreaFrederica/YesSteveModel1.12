package com.elfmcys.yesstevemodel.geckolib3.geo.animated;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.core.snapshot.BoneSnapshot;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import javax.annotation.Nullable;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimatedGeoBone implements IBone {
    private final GeoBone geoBone;

    private final List<AnimatedGeoBone> children;

    private boolean isHidden = false;
    private boolean areCubesHidden = false;
    private boolean hideChildBonesToo = false;

    private final Vector3f scale = new Vector3f(1, 1, 1);
    private final Vector3f position = new Vector3f();
    private final Vector3f rotation = new Vector3f();
    private final Vector3f initialRotation;
    private final Vector3f pivotAbs = new Vector3f();
    private final int boneId;
    private boolean trackingXform;

    public AnimatedGeoBone(GeoBone geoBone, @Nullable Map<String, AnimatedGeoBone> bones) {
        this.geoBone = geoBone;
        this.rotation.set(geoBone.rotation());
        this.initialRotation = new Vector3f(geoBone.rotation());
        this.boneId = StringPool.computeIfAbsent(geoBone.name());

        if (bones != null) {
            bones.put(geoBone.name(), this);
            this.children = ObjectLists.unmodifiable(new ObjectArrayList<>(geoBone.children().stream().map(b -> new AnimatedGeoBone(b, bones)).collect(Collectors.toList())));
        } else {
            this.children = ObjectLists.emptyList();
        }
    }

    public GeoBone geoBone() {
        return this.geoBone;
    }

    public List<AnimatedGeoBone> children() {
        return this.children;
    }

    @Override
    public BoneSnapshot getInitialSnapshot() {
        return this.geoBone.initialSnapshot();
    }

    @Override
    public String getName() {
        return this.geoBone.name();
    }

    @Override
    public Vector3f getInitialRotation() {
        return this.initialRotation;
    }

    @Override
    public int getBoneId() {
        return this.boneId;
    }

    @Override
    public float getRotationX() {
        return this.rotation.x;
    }

    @Override
    public void setRotationX(float value) {
        this.rotation.x = value;
    }

    @Override
    public float getRotationY() {
        return this.rotation.y;
    }

    @Override
    public void setRotationY(float value) {
        this.rotation.y = value;
    }

    @Override
    public float getRotationZ() {
        return this.rotation.z;
    }

    @Override
    public void setRotationZ(float value) {
        this.rotation.z = value;
    }

    @Override
    public float getPositionX() {
        return this.position.x;
    }

    @Override
    public void setPositionX(float value) {
        this.position.x = value;
    }

    @Override
    public float getPositionY() {
        return this.position.y;
    }

    @Override
    public void setPositionY(float value) {
        this.position.y = value;
    }

    @Override
    public float getPositionZ() {
        return this.position.z;
    }

    @Override
    public void setPositionZ(float value) {
        this.position.z = value;
    }

    @Override
    public float getScaleX() {
        return this.scale.x;
    }

    @Override
    public void setScaleX(float value) {
        this.scale.x = value;
    }

    @Override
    public float getScaleY() {
        return this.scale.y;
    }

    @Override
    public void setScaleY(float value) {
        this.scale.y = value;
    }

    @Override
    public float getScaleZ() {
        return this.scale.z;
    }

    @Override
    public void setScaleZ(float value) {
        this.scale.z = value;
    }

    @Override
    public boolean isHidden() {
        return this.isHidden;
    }

    @Override
    public void setHidden(boolean hidden) {
        this.setHidden(hidden, hidden);
    }

    @Override
    public float getPivotX() {
        return this.geoBone.pivot().x;
    }

    @Override
    public float getPivotY() {
        return this.geoBone.pivot().y;
    }

    @Override
    public float getPivotZ() {
        return this.geoBone.pivot().z;
    }

    @Override
    public boolean isTrackingXform() {
        return this.trackingXform;
    }

    @Override
    public void setTrackXform(boolean track) {
        this.trackingXform = track;
    }

    @Override
    public float getPivotAbsX() {
        return this.pivotAbs.x;
    }

    @Override
    public float getPivotAbsY() {
        return this.pivotAbs.y;
    }

    @Override
    public float getPivotAbsZ() {
        return this.pivotAbs.z;
    }

    public void setPivotAbs(float x, float y, float z) {
        this.pivotAbs.set(x, y, z);
    }

    @Override
    public boolean cubesAreHidden() {
        return this.areCubesHidden;
    }

    @Override
    public boolean childBonesAreHiddenToo() {
        return this.hideChildBonesToo;
    }

    @Override
    public void setCubesHidden(boolean hidden) {
        this.areCubesHidden = hidden;
    }

    @Override
    public void setHidden(boolean selfHidden, boolean skipChildRendering) {
        this.isHidden = selfHidden;
        this.hideChildBonesToo = skipChildRendering;
    }

    public void addPositionX(float x) {
        this.setPositionX(this.getPositionX() + x);
    }

    public void addPositionY(float y) {
        this.setPositionY(this.getPositionY() + y);
    }

    public void addPositionZ(float z) {
        this.setPositionZ(this.getPositionZ() + z);
    }

    public void setPosition(float x, float y, float z) {
        this.setPositionX(x);
        this.setPositionY(y);
        this.setPositionZ(z);
    }

    public Vector3d getPosition() {
        return new Vector3d(this.getPositionX(), this.getPositionY(), this.getPositionZ());
    }

    public void setPosition(Vector3d vec) {
        this.setPosition((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void addRotation(Vector3d vec) {
        this.addRotation((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void addRotation(float x, float y, float z) {
        this.addRotationX(x);
        this.addRotationY(y);
        this.addRotationZ(z);
    }

    public void addRotationX(float x) {
        this.setRotationX(this.getRotationX() + x);
    }

    public void addRotationY(float y) {
        this.setRotationY(this.getRotationY() + y);
    }

    public void addRotationZ(float z) {
        this.setRotationZ(this.getRotationZ() + z);
    }

    public void setRotation(float x, float y, float z) {
        this.setRotationX(x);
        this.setRotationY(y);
        this.setRotationZ(z);
    }

    public Vector3d getRotation() {
        return new Vector3d(this.getRotationX(), this.getRotationY(), this.getRotationZ());
    }

    public void setRotation(Vector3d vec) {
        this.setRotation((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void multiplyScale(Vector3d vec) {
        this.multiplyScale((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void multiplyScale(float x, float y, float z) {
        this.setScaleX(this.getScaleX() * x);
        this.setScaleY(this.getScaleY() * y);
        this.setScaleZ(this.getScaleZ() * z);
    }

    public void setScale(float x, float y, float z) {
        this.setScaleX(x);
        this.setScaleY(y);
        this.setScaleZ(z);
    }

    public Vector3d getScale() {
        return new Vector3d(this.getScaleX(), this.getScaleY(), this.getScaleZ());
    }

    public void setScale(Vector3d vec) {
        this.setScale((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void addRotationOffsetFromBone(AnimatedGeoBone source) {
        this.setRotationX(this.getRotationX() + source.getRotationX() - source.getInitialSnapshot().rotationValueX);
        this.setRotationY(this.getRotationY() + source.getRotationY() - source.getInitialSnapshot().rotationValueY);
        this.setRotationZ(this.getRotationZ() + source.getRotationZ() - source.getInitialSnapshot().rotationValueZ);
    }
}
