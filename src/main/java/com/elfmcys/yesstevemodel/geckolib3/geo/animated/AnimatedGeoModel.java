package com.elfmcys.yesstevemodel.geckolib3.geo.animated;

import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnimatedGeoModel implements ILocationModel {
    private final GeoModel geoModel;
    private final List<AnimatedGeoBone> topLevelBones;
    private final Map<String, AnimatedGeoBone> bones;

    private final List<AnimatedGeoBone> leftHandBones;
    private final List<AnimatedGeoBone> rightHandBones;
    private final List<AnimatedGeoBone> leftWaistBones;
    private final List<AnimatedGeoBone> rightWaistBones;
    private final List<AnimatedGeoBone> elytraBones;
    private final List<AnimatedGeoBone> backpackBones;
    private final List<AnimatedGeoBone> tacPistolBones;
    private final List<AnimatedGeoBone> tacRifleBones;
    private final List<AnimatedGeoBone> headBones;

    @Nullable
    private final AnimatedGeoBone head;
    @Nullable
    private final AnimatedGeoBone hat;
    @Nullable
    private final AnimatedGeoBone leftArm;
    @Nullable
    private final AnimatedGeoBone rightArm;
    @Nullable
    private final AnimatedGeoBone background;

    public AnimatedGeoModel(GeoModel model) {
        this.geoModel = model;

        var bones = new Object2ObjectOpenHashMap<String, AnimatedGeoBone>();
        this.topLevelBones = ObjectLists.unmodifiable(new ObjectArrayList<>(model.topLevelBones().stream().map(b -> new AnimatedGeoBone(b, bones)).collect(Collectors.toList())));
        this.bones = Object2ObjectMaps.unmodifiable(bones);

        // https://ysm.cfpa.team/wiki/tph2yetr/#%E4%B8%BB%E6%A8%A1%E5%9E%8B
        this.leftHandBones = this.getLocatorHierarchy("LeftHandLocator"); // 左手手持物品的定位组
        this.rightHandBones = this.getLocatorHierarchy("RightHandLocator"); // 右手手持物品的定位组
        this.leftWaistBones = this.getLocatorHierarchy("LeftWaistLocator"); // 主手拔刀剑定位组
        this.rightWaistBones = this.getLocatorHierarchy("RightWaistLocator"); // 副手拔刀剑定位组
        this.elytraBones = this.getLocatorHierarchy("ElytraLocator"); // 鞘翅定位组
        this.backpackBones = this.getLocatorHierarchy("BackpackLocator"); // 背包定位组
        this.tacPistolBones = this.getLocatorHierarchy("PistolLocator"); // 手枪模型定位组
        this.tacRifleBones = this.getLocatorHierarchy("RifleLocator"); // 其他枪械定位组
        this.headBones = this.getLocatorHierarchy("Head"); // 头部定位组

        this.head = bones.get("Head");
        // fixme: 有 hat 部分吗？
        this.hat = bones.get("Hat");
        this.leftArm = bones.get("LeftArm");
        this.rightArm = bones.get("RightArm");
        this.background = bones.get("Background");
    }

    private List<AnimatedGeoBone> getLocatorHierarchy(String locatorName) {
        var bone = this.bones.get(locatorName);
        if (bone == null) {
            return ObjectLists.emptyList();
        }

        var list = new ObjectArrayList<AnimatedGeoBone>();
        while (true) {
            list.add(bone);
            if (bone.geoBone().parent() != null) {
                bone = Objects.requireNonNull(this.bones.get(bone.geoBone().parent().name()));
            } else {
                break;
            }
        }

        Collections.reverse(list);
        return ObjectLists.unmodifiable(list);
    }

    public GeoModel geoModel() {
        return this.geoModel;
    }

    public List<AnimatedGeoBone> topLevelBones() {
        return this.topLevelBones;
    }

    public Map<String, AnimatedGeoBone> bones() {
        return this.bones;
    }

    @Override
    public List<AnimatedGeoBone> leftHandBones() {
        return this.leftHandBones;
    }

    @Override
    public List<AnimatedGeoBone> rightHandBones() {
        return this.rightHandBones;
    }

    @Override
    public List<AnimatedGeoBone> leftWaistBones() {
        return this.leftWaistBones;
    }

    @Override
    public List<AnimatedGeoBone> rightWaistBones() {
        return this.rightWaistBones;
    }

    @Override
    public List<AnimatedGeoBone> elytraBones() {
        return this.elytraBones;
    }

    @Override
    public List<AnimatedGeoBone> backpackBones() {
        return this.backpackBones;
    }

    @Override
    public List<AnimatedGeoBone> tacPistolBones() {
        return this.tacPistolBones;
    }

    @Override
    public List<AnimatedGeoBone> tacRifleBones() {
        return this.tacRifleBones;
    }

    @Override
    public List<AnimatedGeoBone> headBones() {
        return this.headBones;
    }

    @Nullable
    public AnimatedGeoBone head() {
        return this.head;
    }

    @Nullable
    public AnimatedGeoBone hat() {
        return this.hat;
    }

    @Nullable
    public AnimatedGeoBone leftArm() {
        return this.leftArm;
    }

    @Nullable
    public AnimatedGeoBone rightArm() {
        return this.rightArm;
    }

    @Nullable
    public AnimatedGeoBone background() {
        return this.background;
    }
}
