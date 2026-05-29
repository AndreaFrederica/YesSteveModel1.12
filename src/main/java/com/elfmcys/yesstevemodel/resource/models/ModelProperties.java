package com.elfmcys.yesstevemodel.resource.models;

import com.elfmcys.yesstevemodel.client.gui.custom.ExtraAnimationButtons;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.elfmcys.yesstevemodel.util.data.StringMapPair;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModelProperties {
    private final float heightScale;
    private final float widthScale;
    private final String defaultTexture;
    private final String previewAnimation;
    private final OrderedStringMap<String, String> extraAnimation;
    private final Map<String, ExtraAnimationButtons> extraAnimationButtons;
    private final Map<String, OrderedStringMap<String, String>> extraAnimationClassify;
    private final boolean free;
    private final boolean renderLayersFirst;
    private final boolean disablePreviewRotation;

    public ModelProperties(float heightScale, float widthScale, String defaultTexture, String previewAnimation, OrderedStringMap<String, String> extraAnimation, ExtraAnimationButtons[] extraAnimationButtons, StringMapPair[] extraAnimationClassify, boolean free, boolean renderLayersFirst, boolean disablePreviewRotation) {
        this.heightScale = heightScale;
        this.widthScale = widthScale;
        this.defaultTexture = defaultTexture;
        this.previewAnimation = previewAnimation;
        this.extraAnimation = extraAnimation;
        this.extraAnimationButtons = buildExtraAnimationButtonsMap(extraAnimationButtons);
        this.extraAnimationClassify = buildExtraAnimationClassifyMap(extraAnimationClassify);
        this.free = free;
        this.renderLayersFirst = renderLayersFirst;
        this.disablePreviewRotation = disablePreviewRotation;
    }

    private static Map<String, ExtraAnimationButtons> buildExtraAnimationButtonsMap(ExtraAnimationButtons[] extraAnimationButtons) {
        Map<String, ExtraAnimationButtons> map = new LinkedHashMap<>();
        for (ExtraAnimationButtons buttons : extraAnimationButtons) {
            map.put(buttons.getId(), buttons);
        }
        return Collections.unmodifiableMap(map);
    }

    private static Map<String, OrderedStringMap<String, String>> buildExtraAnimationClassifyMap(StringMapPair[] extraAnimationClassify) {
        Map<String, OrderedStringMap<String, String>> map = new LinkedHashMap<>();
        for (StringMapPair classify : extraAnimationClassify) {
            map.put(classify.getKey(), classify.getValueMap());
        }
        return Collections.unmodifiableMap(map);
    }

    public float getHeightScale() {
        return this.heightScale;
    }

    public float getWidthScale() {
        return this.widthScale;
    }

    public String getDefaultTexture() {
        return this.defaultTexture;
    }

    public String getPreviewAnimation() {
        return this.previewAnimation;
    }

    public OrderedStringMap<String, String> getExtraAnimation() {
        return this.extraAnimation;
    }

    public Map<String, ExtraAnimationButtons> getExtraAnimationButtons() {
        return this.extraAnimationButtons;
    }

    public Map<String, OrderedStringMap<String, String>> getExtraAnimationClassify() {
        return this.extraAnimationClassify;
    }

    public boolean isFree() {
        return this.free;
    }

    public boolean isRenderLayersFirst() {
        return this.renderLayersFirst;
    }

    public boolean isDisablePreviewRotation() {
        return this.disablePreviewRotation;
    }
}