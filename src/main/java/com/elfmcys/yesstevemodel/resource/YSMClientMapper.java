package com.elfmcys.yesstevemodel.resource;

import com.elfmcys.yesstevemodel.audio.AudioCodec;
import com.elfmcys.yesstevemodel.audio.AudioTrackData;
import com.elfmcys.yesstevemodel.client.ClientModelInfo;
import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.CheckboxConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RadioConfig;
import com.elfmcys.yesstevemodel.client.gui.custom.configs.RangeConfig;
import com.elfmcys.yesstevemodel.client.model.MainModelData;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.BoneAnimation;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.BoneKeyFrameProcessor;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.EasingType;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.bone.RawBoneKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.ParticleEventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.DoubleValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationControllerFile;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.file.ModelExtraResourcesFile;
import com.elfmcys.yesstevemodel.geckolib3.file.ProjectileModelFiles;
import com.elfmcys.yesstevemodel.geckolib3.file.VehicleModelFiles;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable;
import com.elfmcys.yesstevemodel.geckolib3.util.LinearKeyframeInterpolator;
import com.elfmcys.yesstevemodel.geckolib3.util.TicksInterpolator;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.resource.models.AuthorInfo;
import com.elfmcys.yesstevemodel.resource.models.GeometryDescription;
import com.elfmcys.yesstevemodel.resource.models.MainModelInfo;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.resource.models.ModelProperties;
import com.elfmcys.yesstevemodel.resource.pojo.RawYsmModel;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.elfmcys.yesstevemodel.util.data.StringMapPair;
import com.elfmcys.yesstevemodel.util.data.StringPair;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Vector2f;
import org.joml.Vector3f;
import rip.ysm.compat.oculus.ShadersTextureType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class YSMClientMapper {

    public static class TranslucencyScanner {
        private final BufferedImage[] images;
        private final boolean[] results;

        public static final int STATE_INVISIBLE = 0;
        public static final int STATE_OPAQUE = 1;
        public static final int STATE_TRANSLUCENT = 2;

        public TranslucencyScanner(BufferedImage[] images, int expectedCount) {
            this.images = images;
            this.results = new boolean[Math.max(expectedCount, images.length)];
        }

        public boolean[] getResults() {
            return this.results;
        }

        public int scan(RawYsmModel.RawFace face) {
            float minU = face.u[0], maxU = face.u[0];
            float minV = face.v[0], maxV = face.v[0];
            for (int i = 1; i < 4; i++) {
                minU = Math.min(minU, face.u[i]);
                maxU = Math.max(maxU, face.u[i]);
                minV = Math.min(minV, face.v[i]);
                maxV = Math.max(maxV, face.v[i]);
            }

            boolean hasValidImage = false;
            boolean faceHasVisiblePixel = false;
            boolean faceHasTransparentPixel = false;

            for (int i = 0; i < this.images.length; i++) {
                if (this.images[i] == null) {
                    continue;
                }
                hasValidImage = true;

                BufferedImage img = this.images[i];
                int imgW = img.getWidth();
                int imgH = img.getHeight();

                int startX = (int) Math.floor(minU * imgW + 0.01f);
                int endX = (int) Math.floor(maxU * imgW - 0.01f);
                if (endX < startX) {
                    endX = startX;
                }

                int startY = (int) Math.floor(minV * imgH + 0.01f);
                int endY = (int) Math.floor(maxV * imgH - 0.01f);
                if (endY < startY) {
                    endY = startY;
                }

                startX = Math.max(0, Math.min(startX, imgW - 1));
                endX = Math.max(0, Math.min(endX, imgW - 1));
                startY = Math.max(0, Math.min(startY, imgH - 1));
                endY = Math.max(0, Math.min(endY, imgH - 1));

                boolean imageHasVisiblePixel = false;
                boolean imageHasTransparentPixel = false;
                boolean imageHasColoredTranslucentPixel = false;

                for (int x = startX; x <= endX; x++) {
                    for (int y = startY; y <= endY; y++) {
                        int alpha = (img.getRGB(x, y) >>> 24) & 0xFF;

                        if (alpha > 0) {
                            imageHasVisiblePixel = true;
                            if (alpha < 255) {
                                imageHasColoredTranslucentPixel = true;
                            }
                        }

                        if (alpha < 255) {
                            imageHasTransparentPixel = true;
                        }

                        if (imageHasVisiblePixel && imageHasTransparentPixel && imageHasColoredTranslucentPixel) {
                            break;
                        }
                    }

                    if (imageHasVisiblePixel && imageHasTransparentPixel && imageHasColoredTranslucentPixel) {
                        break;
                    }
                }

                if (imageHasVisiblePixel) {
                    faceHasVisiblePixel = true;
                    if (imageHasTransparentPixel) {
                        faceHasTransparentPixel = true;
                    }
                    if (imageHasColoredTranslucentPixel) {
                        this.results[i] = true;
                    }
                }
            }

            if (!hasValidImage) {
                return STATE_OPAQUE;
            }
            if (!faceHasVisiblePixel) {
                return STATE_INVISIBLE;
            }
            if (faceHasTransparentPixel) {
                return STATE_TRANSLUCENT;
            }
            return STATE_OPAQUE;
        }
    }

    private static BufferedImage decodeToImage(byte[] data, int imageFormat, int width, int height) {
        if (data == null || data.length == 0) {
            return null;
        }

        if (imageFormat == 0) {
            imageFormat = YSMFolderDeserializer.detectFormat(data);
            if (imageFormat == 0) {
                imageFormat = 1;
            }
        }

        try {
            if (imageFormat == -1) {
                if (width > 0 && height > 0 && data.length >= width * height * 4) {
                    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    int[] pixels = new int[width * height];
                    for (int i = 0; i < pixels.length; i++) {
                        int r = data[i * 4] & 0xFF;
                        int g = data[i * 4 + 1] & 0xFF;
                        int b = data[i * 4 + 2] & 0xFF;
                        int a = data[i * 4 + 3] & 0xFF;
                        pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
                    }
                    img.setRGB(0, 0, width, height, pixels, 0, width);
                    return img;
                }
                throw new RuntimeException("Invalid RGBA texture");
            }

            switch (imageFormat) {
                case 1:
                case 2:
                case 3:
                    return ImageIO.read(new ByteArrayInputStream(data));
                case 4:
                case 5:
                    return ImageIO.read(new ByteArrayInputStream(data));
                default:
                    return ImageIO.read(new ByteArrayInputStream(data));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] encodeToPng(BufferedImage img, byte[] fallbackData) {
        if (img != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                return baos.toByteArray();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fallbackData;
    }

    public static byte[] toPng(byte[] data, int imageFormat, int width, int height) {
        if (imageFormat == 2) {
            return data;
        }
        BufferedImage img = decodeToImage(data, imageFormat, width, height);
        return encodeToPng(img, data);
    }

    public static ClientModelInfo buildParsedBundle(RawYsmModel raw, String modelId) {
        Map<String, OuterFileTexture> mainTextures = new LinkedHashMap<>();
        int textureCount = Math.max(1, raw.mainEntity.textures.size());

        List<BufferedImage> imagesList = new ArrayList<>();
        for (RawYsmModel.RawTexture rt : raw.mainEntity.textures.values()) {
            BufferedImage img = decodeToImage(rt.data, rt.imageFormat, rt.width, rt.height);
            imagesList.add(img);

            byte[] processedData = rt.imageFormat == 2 ? rt.data : encodeToPng(img, rt.data);
            OuterFileTexture texture = new OuterFileTexture(processedData);

            Map<ShadersTextureType, OuterFileTexture> suffixTextures = new LinkedHashMap<>();
            for (RawYsmModel.RawTexture.SubTexture sub : rt.subTextures) {
                if (sub.data == null) {
                    continue;
                }
                byte[] processedSubData = toPng(sub.data, sub.imageFormat, sub.width, sub.height);
                if (sub.specularType == 1) {
                    suffixTextures.put(ShadersTextureType.NORMAL, new OuterFileTexture(processedSubData));
                } else if (sub.specularType == 2) {
                    suffixTextures.put(ShadersTextureType.SPECULAR, new OuterFileTexture(processedSubData));
                }
            }
            texture.setSuffixTextures(suffixTextures);
            mainTextures.put(rt.name, texture);
        }

        Map<String, OuterFileTexture> avatarTextures = new LinkedHashMap<>();
        for (RawYsmModel.RawMetadata.Author author : raw.metadata.authors) {
            if (author.avatarImage == null) {
                continue;
            }
            byte[] processedAvatarData = toPng(author.avatarImage.data, author.avatarImage.format, author.avatarImage.width, author.avatarImage.height);
            avatarTextures.put(author.avatarImage.name, new OuterFileTexture(processedAvatarData));
        }

        OrderedStringMap<String, OuterFileTexture> textureMap = buildTextureMap(mainTextures);
        RawYsmModel.RawGeometry baseGeometry = raw.mainEntity.mainModel != null ? raw.mainEntity.mainModel : raw.mainEntity.armModel;
        GeometryDescription context = buildContext(baseGeometry);

        BufferedImage[] imagesArray = imagesList.toArray(new BufferedImage[0]);
        TranslucencyScanner mainScanner = raw.mainEntity.mainModel != null ? new TranslucencyScanner(imagesArray, textureCount) : null;
        TranslucencyScanner armScanner = raw.mainEntity.armModel != null ? new TranslucencyScanner(imagesArray, textureCount) : null;

        GeoModel mainMesh = buildMesh(raw.mainEntity.mainModel, context, textureCount, mainScanner, raw.properties.allCutout);
        GeoModel armMesh = raw.mainEntity.armModel != null ? buildMesh(raw.mainEntity.armModel, context, textureCount, armScanner, raw.properties.allCutout) : mainMesh;
        GeoModel[] meshes = new GeoModel[]{mainMesh, armMesh};

        Map<String, AnimationFile> animations = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawAnimationFile> entry : raw.mainEntity.animationFiles.entrySet()) {
            animations.put(entry.getKey(), new AnimationFile(buildAnimations(entry.getValue(), raw.properties.mergeMultilineExpr)));
        }

        List<AnimationControllerFile> controllersList = new ArrayList<>();
        if (raw.mainEntity.animationControllerFiles != null) {
            for (RawYsmModel.RawAnimationControllerFile file : raw.mainEntity.animationControllerFiles) {
                Map<String, AnimationController> controllerMap = buildControllers(file.controllers, raw.properties.mergeMultilineExpr);
                if (!controllerMap.isEmpty()) {
                    controllersList.add(new AnimationControllerFile(controllerMap));
                }
            }
        }

        MainModelData mainModelData = new MainModelData(meshes, animations, controllersList.toArray(new AnimationControllerFile[0]), textureMap);
        ServerModelInfo modelInfo = buildModelInfo(raw);
        ModelExtraResourcesFile extraResources = buildExtraResources(raw);
        ProjectileModelFiles[] extraItemModels = buildExtraItemModels(raw, context, raw.properties.mergeMultilineExpr);
        VehicleModelFiles[] extraEntityModels = buildExtraEntityModels(raw, context, raw.properties.mergeMultilineExpr);
        Map<String, OuterFileTexture> extraTextures = buildExtraTextures(raw);
        return new ClientModelInfo(mainModelData, extraItemModels, extraEntityModels, extraResources, modelInfo, avatarTextures, extraTextures);
    }

    private static GeoModel buildMesh(RawYsmModel.RawGeometry rawGeo, GeometryDescription context, int textureCount, TranslucencyScanner scanner, boolean allCutout) {
        if (rawGeo == null || rawGeo.bones.isEmpty()) {
            boolean[] fallbackArray = scanner != null ? scanner.getResults() : new boolean[Math.max(1, textureCount)];
            return buildMesh(new GeoBone[0], new HashMap<String, String>(), context, fallbackArray);
        }

        List<GeoBone> geoBones = new ArrayList<>();
        List<GeoModel.BakedBone> bakedBones = new ArrayList<>();
        Map<String, String> parentMap = new HashMap<>();

        for (RawYsmModel.RawBone rawBone : rawGeo.bones) {
            parentMap.put(rawBone.name, rawBone.parentName);
            geoBones.add(new GeoBone(rawBone.name, false, false, false, rawBone.pivot[0], rawBone.pivot[1], rawBone.pivot[2], rawBone.rotation[0], rawBone.rotation[1], rawBone.rotation[2]));

            GeoModel.BakedBone bakedBone = new GeoModel.BakedBone();
            bakedBone.name = rawBone.name;
            bakedBone.glow = rawBone.name.startsWith("ysmGlow");
            bakedBone.pivotX = rawBone.pivot[0];
            bakedBone.pivotY = rawBone.pivot[1];
            bakedBone.pivotZ = rawBone.pivot[2];
            bakedBone.rotX = rawBone.rotation[0];
            bakedBone.rotY = rawBone.rotation[1];
            bakedBone.rotZ = rawBone.rotation[2];

            boolean forceCull = allCutout;
            for (RawYsmModel.RawCube rawCube : rawBone.cubes) {
                GeoModel.BakedCube bakedCube = new GeoModel.BakedCube();
                int validFaceCount = 0;
                boolean hasTranslucentFace = false;

                for (RawYsmModel.RawFace rawFace : rawCube.faces) {
                    int faceState = scanner != null ? scanner.scan(rawFace) : TranslucencyScanner.STATE_OPAQUE;
                    if (faceState == TranslucencyScanner.STATE_INVISIBLE) {
                        continue;
                    }
                    if (faceState == TranslucencyScanner.STATE_TRANSLUCENT) {
                        hasTranslucentFace = true;
                    }
                    if (!forceCull && isNegativeSizedFace(rawFace)) {
                        forceCull = true;
                    }

                    GeoModel.BakedQuad bakedQuad = new GeoModel.BakedQuad();
                    bakedQuad.normal = new Vector3f(rawFace.normal[0], rawFace.normal[1], rawFace.normal[2]);
                    bakedQuad.positions = new Vector3f[4];
                    bakedQuad.uvs = new Vector2f[4];
                    for (int i = 0; i < 4; i++) {
                        bakedQuad.positions[i] = new Vector3f(rawFace.positions[i][0], rawFace.positions[i][1], rawFace.positions[i][2]);
                        bakedQuad.uvs[i] = new Vector2f(rawFace.u[i], rawFace.v[i]);
                    }
                    bakedCube.quads.add(bakedQuad);
                    validFaceCount++;
                }

                boolean isZeroThickness = true;
                if (!bakedCube.quads.isEmpty()) {
                    Vector3f baseNormal = bakedCube.quads.get(0).normal;
                    Vector3f basePos = bakedCube.quads.get(0).positions[0];
                    for (GeoModel.BakedQuad quad : bakedCube.quads) {
                        for (int i = 0; i < 4; i++) {
                            Vector3f pos = quad.positions[i];
                            float dx = pos.x - basePos.x;
                            float dy = pos.y - basePos.y;
                            float dz = pos.z - basePos.z;
                            float distance = dx * baseNormal.x + dy * baseNormal.y + dz * baseNormal.z;
                            if (Math.abs(distance) > 1e-3f) {
                                isZeroThickness = false;
                                break;
                            }
                        }
                        if (!isZeroThickness) {
                            break;
                        }
                    }
                } else {
                    isZeroThickness = false;
                }

                if (forceCull) {
                    bakedCube.cullable = true;
                } else if (hasTranslucentFace) {
                    bakedCube.cullable = false;
                } else if (isZeroThickness && validFaceCount > 1) {
                    bakedCube.cullable = true;
                } else {
                    bakedCube.cullable = validFaceCount >= 5;
                }

                if (!bakedCube.quads.isEmpty()) {
                    bakedBone.cubes.add(bakedCube);
                }
            }
            bakedBones.add(bakedBone);
        }

        for (GeoModel.BakedBone bakedBone : bakedBones) {
            String parentName = parentMap.get(bakedBone.name);
            if (parentName != null && !parentName.isEmpty()) {
                for (int i = 0; i < bakedBones.size(); i++) {
                    if (bakedBones.get(i).name.equals(parentName)) {
                        bakedBone.parentIdx = i;
                        break;
                    }
                }
            }
            if ("LeftArm".equals(bakedBone.name)) {
                bakedBone.partMask = 1;
            } else if ("RightArm".equals(bakedBone.name)) {
                bakedBone.partMask = 2;
            } else if ("Background".equals(bakedBone.name)) {
                bakedBone.partMask = 3;
            } else if (bakedBone.parentIdx != -1) {
                bakedBone.partMask = bakedBones.get(bakedBone.parentIdx).partMask;
            } else {
                bakedBone.partMask = 0;
            }
        }

        boolean[] translucencyArray = scanner != null ? scanner.getResults() : new boolean[Math.max(1, textureCount)];
        GeoModel mesh = buildMesh(geoBones.toArray(new GeoBone[0]), parentMap, context, translucencyArray);
        mesh.bakedBones = bakedBones;
        return mesh;
    }

    private static Map<String, Animation> buildAnimations(RawYsmModel.RawAnimationFile animFile, boolean mergeMultilineExpr) {
        Map<String, Animation> result = new LinkedHashMap<>();
        for (RawYsmModel.RawAnimation rawAnimation : animFile.animations.values()) {
            ILoopType loopMode = ILoopType.EDefaultLoopTypes.PLAY_ONCE;
            if (rawAnimation.loopMode == 1) {
                loopMode = ILoopType.EDefaultLoopTypes.LOOP;
            } else if (rawAnimation.loopMode == 3) {
                loopMode = ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME;
            }

            List<BoneAnimation> boneAnimations = new ArrayList<>();
            for (RawYsmModel.RawBoneAnimation rawBoneAnimation : rawAnimation.boneAnimations) {
                List<BoneKeyFrame> rotFrames = parseKeyframes(rawBoneAnimation.rotation, true);
                List<BoneKeyFrame> posFrames = parseKeyframes(rawBoneAnimation.position, false);
                List<BoneKeyFrame> scaleFrames = parseKeyframes(rawBoneAnimation.scale, false);
                boneAnimations.add(new BoneAnimation(rawBoneAnimation.boneName, rotFrames, posFrames, scaleFrames));
            }

            List<EventKeyFrame<IValue[]>> timelineEvents = new ArrayList<>();
            for (RawYsmModel.RawTimelineEvent rawTimelineEvent : rawAnimation.timelineEvents) {
                List<IValue> values = parse(rawTimelineEvent.events, mergeMultilineExpr);
                timelineEvents.add(new EventKeyFrame<IValue[]>(rawTimelineEvent.timestamp * 20.0f, values.toArray(new IValue[0])));
            }

            if (rawAnimation.blendWeight instanceof Float) {
                new DoubleValue((Float) rawAnimation.blendWeight);
            } else if (rawAnimation.blendWeight instanceof String) {
                try {
                    parse((String) rawAnimation.blendWeight);
                } catch (Exception ignore) {
                }
            }

            Animation animation = new Animation(rawAnimation.name, rawAnimation.length * 20.0f, loopMode,
                    null, null, null, null,
                    boneAnimations.toArray(new BoneAnimation[0]),
                    new EventKeyFrame[0],
                    new ParticleEventKeyFrame[0],
                    timelineEvents.toArray(new EventKeyFrame[0]));
            result.put(rawAnimation.name, animation);
        }
        return result;
    }

    private static List<BoneKeyFrame> parseKeyframes(List<RawYsmModel.RawKeyframe> frames, boolean isRotation) {
        List<RawBoneKeyFrame> builders = new ArrayList<>();
        for (RawYsmModel.RawKeyframe rawKeyframe : frames) {
            RawBoneKeyFrame builder = new RawBoneKeyFrame();
            builder.startTick = rawKeyframe.timestamp * 20.0f;
            builder.easingType = rawKeyframe.interpolationMode == 2 ? EasingType.CATMULLROM : EasingType.LINEAR;
            builder.contiguous = !rawKeyframe.hasPreData;

            if (rawKeyframe.hasPreData) {
                assignToBuilder(builder, rawKeyframe.preData, true);
                assignToBuilder(builder, rawKeyframe.postData, false);
            } else {
                assignToBuilder(builder, rawKeyframe.postData, true);
            }
            builders.add(builder);
        }
        return BoneKeyFrameProcessor.process(builders, isRotation);
    }

    private static void assignToBuilder(RawBoneKeyFrame builder, Object[] data, boolean isPre) {
        for (int axis = 0; axis < 3; axis++) {
            double doubleValue = 0.0;
            IValue expressionValue = null;
            Object value = data[axis];
            if (value instanceof Float) {
                doubleValue = (Float) value;
            } else if (value instanceof String) {
                try {
                    expressionValue = parse((String) value);
                } catch (Exception ignore) {
                }
            }
            if (isPre) {
                if (axis == 0) {
                    builder.preX = doubleValue;
                    builder.preXValue = expressionValue;
                } else if (axis == 1) {
                    builder.preY = doubleValue;
                    builder.preYValue = expressionValue;
                } else {
                    builder.preZ = doubleValue;
                    builder.preZValue = expressionValue;
                }
            } else {
                if (axis == 0) {
                    builder.postX = doubleValue;
                    builder.postXValue = expressionValue;
                } else if (axis == 1) {
                    builder.postY = doubleValue;
                    builder.postYValue = expressionValue;
                } else {
                    builder.postZ = doubleValue;
                    builder.postZValue = expressionValue;
                }
            }
        }
    }

    private static Map<String, AnimationController> buildControllers(Map<String, RawYsmModel.RawAnimationController> rawControllers, boolean mergeMultilineExpr) {
        Map<String, AnimationController> result = new LinkedHashMap<>();
        for (RawYsmModel.RawAnimationController rawController : rawControllers.values()) {
            List<AnimationState> states = new ArrayList<>();
            for (RawYsmModel.RawControllerState rawState : rawController.states) {
                List<Pair<String, IValue>> animations = new ArrayList<>();
                for (Map.Entry<String, String> entry : rawState.animations) {
                    IValue blend = null;
                    if (!entry.getValue().isEmpty()) {
                        try {
                            blend = parse(entry.getValue());
                        } catch (Exception ignore) {
                        }
                    }
                    animations.add(Pair.of(entry.getKey(), blend));
                }

                List<Pair<String, IValue>> transitions = new ArrayList<>();
                for (Map.Entry<String, String> entry : rawState.transitions) {
                    transitions.add(Pair.of(entry.getKey(), parse(entry.getValue())));
                }

                List<IValue> onEntry = parse(rawState.onEntry, mergeMultilineExpr);
                List<IValue> onExit = parse(rawState.onExit, mergeMultilineExpr);

                IInterpolable blendTransition;
                if (!rawState.blendTransitions.isEmpty()) {
                    float[] keys = new float[rawState.blendTransitions.size()];
                    float[] values = new float[rawState.blendTransitions.size()];
                    int i = 0;
                    for (Map.Entry<Float, Float> entry : rawState.blendTransitions.entrySet()) {
                        keys[i] = entry.getKey();
                        values[i] = entry.getValue();
                        i++;
                    }
                    blendTransition = new LinearKeyframeInterpolator(keys, values);
                } else {
                    blendTransition = new TicksInterpolator(rawState.blendTransitionValue);
                }

                states.add(new AnimationState(rawState.name, animations.toArray(new Pair[0]), transitions.toArray(new Pair[0]), rawState.soundEffects.toArray(new String[0]), onEntry.toArray(new IValue[0]), onExit.toArray(new IValue[0]), blendTransition, rawState.blendViaShortestPath));
            }

            result.put(rawController.animationName, new AnimationController(rawController.initialState.isEmpty() ? "default" : rawController.initialState, states.toArray(new AnimationState[0])));
        }
        return result;
    }

    public static ServerModelInfo buildModelInfo(RawYsmModel raw) {
        RawYsmModel.RawMetadata rawMetadata = raw.metadata;
        List<AuthorInfo> authors = new ArrayList<>();
        for (RawYsmModel.RawMetadata.Author author : rawMetadata.authors) {
            authors.add(new AuthorInfo(author.name, author.role, orderedStringMap(author.contacts), author.comment));
        }

        Metadata metadata = new Metadata(rawMetadata.name, rawMetadata.tips, new StringPair(rawMetadata.licenseType, rawMetadata.licenseDescription), authors.toArray(new AuthorInfo[0]), orderedStringMap(rawMetadata.links));

        RawYsmModel.RawProperties rawProperties = raw.properties;
        List<StringMapPair> classifyList = new ArrayList<>();
        for (RawYsmModel.ExtraAnimationClassify classify : rawProperties.extraAnimationClassifies) {
            classifyList.add(new StringMapPair(classify.id, orderedStringMap(classify.extras)));
        }

        List<ExtraAnimationButtons> buttonsList = new ArrayList<>();
        for (RawYsmModel.ExtraAnimationButton rawButton : rawProperties.extraAnimationButtons) {
            List<AbstractConfig> metaList = new ArrayList<>();
            for (RawYsmModel.ConfigForm form : rawButton.forms) {
                if ("checkbox".equals(form.type)) {
                    metaList.add(new CheckboxConfig(form.title, form.description, form.defaultValue));
                } else if ("radio".equals(form.type)) {
                    metaList.add(new RadioConfig(form.title, form.description, form.defaultValue, orderedStringMap(form.labels)));
                } else if ("range".equals(form.type)) {
                    metaList.add(new RangeConfig(form.title, form.description, form.defaultValue, form.step, form.min, form.max));
                }
            }
            buttonsList.add(new ExtraAnimationButtons(rawButton.id, rawButton.name, rawButton.description, metaList.toArray(new AbstractConfig[0])));
        }

        ModelProperties properties = new ModelProperties(rawProperties.heightScale, rawProperties.widthScale, rawProperties.defaultTexture, rawProperties.previewAnimation, orderedStringMap(rawProperties.extraAnimations), buttonsList.toArray(new ExtraAnimationButtons[0]), classifyList.toArray(new StringMapPair[0]), rawProperties.isFree, rawProperties.renderLayersFirst, rawProperties.disablePreviewRotation);

        int bones = 0;
        int cubes = 0;
        int faces = 0;
        if (raw.mainEntity.mainModel != null) {
            bones = raw.mainEntity.mainModel.bones.size();
            for (RawYsmModel.RawBone bone : raw.mainEntity.mainModel.bones) {
                cubes += bone.cubes.size();
                for (RawYsmModel.RawCube cube : bone.cubes) {
                    faces += cube.faces.size();
                }
            }
        }

        MainModelInfo stats = new MainModelInfo(bones, cubes, faces);
        RawYsmModel.RawFooter footer = raw.footer;
        return new ServerModelInfo(metadata, properties, stats, footer.version, rawProperties.sha256 != null ? rawProperties.sha256 : "", footer.extra, footer.time, footer.rand);
    }

    private static ModelExtraResourcesFile buildExtraResources(RawYsmModel raw) {
        Map<String, AudioTrackData> sounds = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawDataFile> entry : raw.soundFiles.entrySet()) {
            AudioTrackData track = parseAudioTrackData(entry.getValue().data);
            if (track != null) {
                sounds.put(entry.getKey(), track);
            }
        }

        Map<String, IValue> functions = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawDataFile> entry : raw.functionFiles.entrySet()) {
            String molangScript = new String(entry.getValue().data, StandardCharsets.UTF_8);
            try {
                functions.put(entry.getKey(), GeckoLibCache.getMolangParser().parseExpression(molangScript, true));
            } catch (Exception ignore) {
            }
        }

        Map<String, Map<String, String>> translations = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawLanguageFile> entry : raw.languageFiles.entrySet()) {
            translations.put(entry.getKey(), entry.getValue().data);
        }
        return new ModelExtraResourcesFile(sounds, functions, translations);
    }

    private static AudioTrackData parseAudioTrackData(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return null;
        }

        String header = new String(audioData, 0, Math.min(audioData.length, 100), StandardCharsets.US_ASCII);
        AudioCodec codec;
        if (header.contains("OpusHead")) {
            codec = AudioCodec.OPUS;
        } else if (header.contains("vorbis")) {
            codec = AudioCodec.VORBIS;
        } else {
            codec = AudioCodec.UNDEFINED;
        }

        ByteBuffer buffer = ByteBuffer.wrap(audioData);
        return new AudioTrackData(buffer, codec.ordinal(), 0, 0L);
    }

    private static ProjectileModelFiles[] buildExtraItemModels(RawYsmModel raw, GeometryDescription context, boolean mergeMultilineExpr) {
        List<ProjectileModelFiles> list = new ArrayList<>();
        for (Map.Entry<String, RawYsmModel.RawSubEntity> entry : raw.projectiles.entrySet()) {
            list.add(buildSubEntityHolder(entry.getValue(), context, 1, mergeMultilineExpr));
        }
        return list.toArray(new ProjectileModelFiles[0]);
    }

    private static VehicleModelFiles[] buildExtraEntityModels(RawYsmModel raw, GeometryDescription context, boolean mergeMultilineExpr) {
        List<VehicleModelFiles> list = new ArrayList<>();
        for (Map.Entry<String, RawYsmModel.RawSubEntity> entry : raw.vehicles.entrySet()) {
            list.add(buildSubEntityWrapper(entry.getValue(), context, 1, mergeMultilineExpr));
        }
        return list.toArray(new VehicleModelFiles[0]);
    }

    private static ProjectileModelFiles buildSubEntityHolder(RawYsmModel.RawSubEntity sub, GeometryDescription context, int textureCount, boolean mergeMultilineExpr) {
        OuterFileTexture texture = null;
        TranslucencyScanner subScanner = null;

        if (!sub.textures.isEmpty()) {
            List<BufferedImage> imgList = new ArrayList<>();
            for (RawYsmModel.RawTexture rawTexture : sub.textures.values()) {
                BufferedImage img = decodeToImage(rawTexture.data, rawTexture.imageFormat, rawTexture.width, rawTexture.height);
                imgList.add(img);
                byte[] processedData = rawTexture.imageFormat == 2 ? rawTexture.data : encodeToPng(img, rawTexture.data);
                if (texture == null) {
                    texture = new OuterFileTexture(processedData);
                }
            }
            if (sub.model != null) {
                subScanner = new TranslucencyScanner(imgList.toArray(new BufferedImage[0]), textureCount);
            }
        }

        GeoModel mesh = buildMesh(sub.model, context, textureCount, subScanner, true);

        Map<String, Animation> allAnimations = new LinkedHashMap<>();
        for (Map.Entry<String, RawYsmModel.RawAnimationFile> entry : sub.animationFiles.entrySet()) {
            allAnimations.putAll(buildAnimations(entry.getValue(), mergeMultilineExpr));
        }

        Map<String, AnimationController> controllerMap = new LinkedHashMap<>();
        if (sub.animationControllerFiles != null) {
            for (RawYsmModel.RawAnimationControllerFile file : sub.animationControllerFiles) {
                if (file.controllers != null && !file.controllers.isEmpty()) {
                    controllerMap.putAll(buildControllers(file.controllers, mergeMultilineExpr));
                }
            }
        }

        String[] matchIds = sub.matchIds != null ? sub.matchIds : new String[]{sub.identifier};
        return new ProjectileModelFiles(matchIds, mesh, new AnimationFile(allAnimations), new AnimationControllerFile(controllerMap), texture);
    }

    private static VehicleModelFiles buildSubEntityWrapper(RawYsmModel.RawSubEntity sub, GeometryDescription context, int textureCount, boolean mergeMultilineExpr) {
        OuterFileTexture texture = null;
        TranslucencyScanner subScanner = null;

        if (!sub.textures.isEmpty()) {
            List<BufferedImage> imgList = new ArrayList<>();
            for (RawYsmModel.RawTexture rawTexture : sub.textures.values()) {
                BufferedImage img = decodeToImage(rawTexture.data, rawTexture.imageFormat, rawTexture.width, rawTexture.height);
                imgList.add(img);
                byte[] processedData = rawTexture.imageFormat == 2 ? rawTexture.data : encodeToPng(img, rawTexture.data);
                if (texture == null) {
                    texture = new OuterFileTexture(processedData);
                }
            }
            if (sub.model != null) {
                subScanner = new TranslucencyScanner(imgList.toArray(new BufferedImage[0]), textureCount);
            }
        }

        GeoModel mesh = buildMesh(sub.model, context, textureCount, subScanner, true);

        Map<String, Animation> allAnimations = new LinkedHashMap<>();
        for (RawYsmModel.RawAnimationFile animFile : sub.animationFiles.values()) {
            allAnimations.putAll(buildAnimations(animFile, mergeMultilineExpr));
        }

        Map<String, AnimationController> controllerMap = new LinkedHashMap<>();
        if (sub.animationControllerFiles != null) {
            for (RawYsmModel.RawAnimationControllerFile file : sub.animationControllerFiles) {
                if (file.controllers != null && !file.controllers.isEmpty()) {
                    controllerMap.putAll(buildControllers(file.controllers, mergeMultilineExpr));
                }
            }
        }

        String[] matchIds = sub.matchIds != null ? sub.matchIds : new String[]{sub.identifier};
        return new VehicleModelFiles(matchIds, mesh, new AnimationFile(allAnimations), new AnimationControllerFile(controllerMap), texture);
    }

    private static Map<String, OuterFileTexture> buildExtraTextures(RawYsmModel raw) {
        Map<String, OuterFileTexture> result = new LinkedHashMap<>();
        for (RawYsmModel.RawImage img : raw.properties.backgroundImages) {
            if (img.name != null && !img.name.isEmpty()) {
                result.put(img.name, new OuterFileTexture(toPng(img.data, img.format, img.width, img.height)));
            }
        }
        return result;
    }

    public static List<IValue> parse(List<String> array, boolean mergeMultilineExpr) {
        List<IValue> values = new ArrayList<>();
        if (!mergeMultilineExpr) {
            for (String expr : array) {
                values.add(parse(expr));
            }
            return values;
        }

        try {
            StringBuilder parserText = new StringBuilder();
            for (int i = 0; i < array.size(); i++) {
                parserText.append(array.get(i));
                if (i < array.size() - 1) {
                    parserText.append("\n");
                }
            }
            values.add(parse(parserText.toString()));
        } catch (Throwable ex) {
            values.add(DoubleValue.ZERO);
        }
        return values;
    }

    public static IValue parse(String str) {
        try {
            return parseMolang(str);
        } catch (Throwable ex) {
            return DoubleValue.ZERO;
        }
    }

    private static IValue parseMolang(String expression) {
        return GeckoLibCache.getMolangParser().parseExpression(expression);
    }

    private static String[] buildPath(String targetBone, Map<String, String> parentMap) {
        if (!parentMap.containsKey(targetBone)) {
            return new String[0];
        }
        List<String> path = new ArrayList<>();
        String current = targetBone;
        while (current != null && !current.isEmpty()) {
            path.add(current);
            current = parentMap.get(current);
        }
        Collections.reverse(path);
        return path.toArray(new String[0]);
    }

    private static String[][] buildBoneNameArrays(Map<String, String> parentMap) {
        String[][] arrays = new String[35][];
        String[] targetLocators = new String[]{
                "LeftHandLocator",
                "RightHandLocator",
                "ElytraLocator",
                "PistolLocator",
                "RifleLocator",
                "LeftWaistLocator",
                "RightWaistLocator",
                "LeftShoulderLocator",
                "RightShoulderLocator",
                "BladeLocator",
                "SheathLocator",
                "Head",
                "BackpackLocator",
                "LeftHandLocator2",
                "LeftHandLocator3",
                "LeftHandLocator4",
                "LeftHandLocator5",
                "LeftHandLocator6",
                "LeftHandLocator7",
                "LeftHandLocator8",
                "RightHandLocator2",
                "RightHandLocator3",
                "RightHandLocator4",
                "RightHandLocator5",
                "RightHandLocator6",
                "RightHandLocator7",
                "RightHandLocator8",
                "PassengerLocator",
                "PassengerLocator2",
                "PassengerLocator3",
                "PassengerLocator4",
                "PassengerLocator5",
                "PassengerLocator6",
                "PassengerLocator7",
                "PassengerLocator8"
        };

        for (int i = 0; i < arrays.length; i++) {
            arrays[i] = targetLocators[i] != null && !targetLocators[i].isEmpty() ? buildPath(targetLocators[i], parentMap) : new String[0];
        }
        return arrays;
    }

    public static GeoModel buildMesh(GeoBone[] bones, Map<String, String> parentMap, GeometryDescription context, boolean[] translucencyArray) {
        String[][] boneNameArrays = buildBoneNameArrays(parentMap);
        boolean[] flags = new boolean[]{parentMap.containsKey("LeftArm"), parentMap.containsKey("RightArm"), parentMap.containsKey("Background")};
        return new GeoModel(bones, boneNameArrays, flags, context, translucencyArray);
    }

    public static OrderedStringMap<String, OuterFileTexture> buildTextureMap(Map<String, OuterFileTexture> textures) {
        if (textures.isEmpty()) {
            return new OrderedStringMap<String, OuterFileTexture>(new String[0], new OuterFileTexture[0]);
        }
        String[] keys = textures.keySet().toArray(new String[0]);
        OuterFileTexture[] values = textures.values().toArray(new OuterFileTexture[0]);
        return new OrderedStringMap<String, OuterFileTexture>(keys, values);
    }

    private static OrderedStringMap<String, String> orderedStringMap(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return new OrderedStringMap<String, String>(new String[0], new String[0]);
        }
        String[] keys = source.keySet().toArray(new String[0]);
        String[] values = source.values().toArray(new String[0]);
        return new OrderedStringMap<String, String>(keys, values);
    }

    public static GeometryDescription buildContext(RawYsmModel.RawGeometry model) {
        if (model == null) {
            return null;
        }
        return new GeometryDescription(
                model.identifier,
                model.textureWidth,
                model.textureHeight,
                model.visibleBoundsWidth,
                model.visibleBoundsHeight,
                IntStream.range(0, model.visibleBoundsOffset.length).mapToDouble(i -> model.visibleBoundsOffset[i]).toArray()
        );
    }

    private static boolean isNegativeSizedFace(RawYsmModel.RawFace face) {
        float[] p0 = face.positions[0];
        float[] p1 = face.positions[1];
        float[] p2 = face.positions[2];

        float ax = p1[0] - p0[0];
        float ay = p1[1] - p0[1];
        float az = p1[2] - p0[2];
        float bx = p2[0] - p0[0];
        float by = p2[1] - p0[1];
        float bz = p2[2] - p0[2];

        float nx = ay * bz - az * by;
        float ny = az * bx - ax * bz;
        float nz = ax * by - ay * bx;

        float len2 = nx * nx + ny * ny + nz * nz;
        if (len2 <= 1e-10f) {
            float[] p3 = face.positions[3];
            bx = p3[0] - p0[0];
            by = p3[1] - p0[1];
            bz = p3[2] - p0[2];
            nx = ay * bz - az * by;
            ny = az * bx - ax * bz;
            nz = ax * by - ay * bx;
            len2 = nx * nx + ny * ny + nz * nz;
            if (len2 <= 1e-10f) {
                return false;
            }
        }

        float dot = nx * face.normal[0] + ny * face.normal[1] + nz * face.normal[2];
        return dot < -1e-5f;
    }
}
