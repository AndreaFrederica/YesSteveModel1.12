package com.elfmcys.yesstevemodel.client.model.processor;

import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceRBTreeMap;

import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ParallelProcessor<T extends GeoEntity<?>, TModel> implements ModelProcessor<T, TModel> {

    private final String prefix;

    private final String slotName;

    private final Predicate<String> animationEntryMatcher;

    private final Predicate<String> controllerEntryMatcher;

    private final Predicate<String> animationNameMatcher;

    private final AnimationDataProvider<TModel> animationDataProvider;

    private final TriFunction<String, T, String, com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController<T>> controllerFactory;

    public ParallelProcessor(String prefix, String slotName, boolean allowExtraSlots, AnimationDataProvider<TModel> animationDataProvider, TriFunction<String, T, String, com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController<T>> controllerFactory) {
        this.prefix = prefix;
        this.slotName = slotName;
        if (allowExtraSlots) {
            Pattern animPattern = Pattern.compile(String.format("^%s\\.%s_.+", prefix, slotName));
            Pattern ctrlPattern = Pattern.compile(String.format("^%s_ctrl_%s_.+", prefix, slotName));
            this.animationEntryMatcher = s -> animPattern.matcher(s).matches();
            this.controllerEntryMatcher = s -> ctrlPattern.matcher(s).matches();
        } else {
            Pattern animPattern = Pattern.compile(String.format("^%s\\.%s_[0-7]$", prefix, slotName));
            Pattern ctrlPattern = Pattern.compile(String.format("^%s_ctrl_%s_[0-7]$", prefix, slotName));
            this.animationEntryMatcher = s -> animPattern.matcher(s).matches();
            this.controllerEntryMatcher = s -> ctrlPattern.matcher(s).matches();
        }
        Pattern namePattern = Pattern.compile(String.format("^%s[0-7]$", slotName));
        this.animationNameMatcher = s -> namePattern.matcher(s).matches();
        this.animationDataProvider = animationDataProvider;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public ControllerFactory<T> process(TModel modelData, ModelResourceBundle resourceBundle) {
        Object2ReferenceRBTreeMap<String, String> matchedSlots = new Object2ReferenceRBTreeMap<>();
        this.animationDataProvider.getAnimationEntries(modelData, resourceBundle).forEach((key, value) -> {
            if (this.animationEntryMatcher.test(key)) {
                matchedSlots.put(key, null);
            }
        });
        Object2ReferenceMap<String, Animation> animations = this.animationDataProvider.getAnimations(modelData, resourceBundle);
        resourceBundle.getEvents().forEach((key, value) -> {
            if (this.controllerEntryMatcher.test(key)) {
                String controllerName = key.replace("_ctrl_", ".");
                try {
                    String suffix = controllerName.substring(this.prefix.length() + this.slotName.length() + 2);
                    int slotIndex = Integer.parseInt(suffix);
                    if (slotIndex >= 0 && slotIndex <= 7) {
                        String animationName = this.slotName + suffix;
                        if (animations.containsKey(animationName)) {
                            matchedSlots.put(controllerName, animationName);
                            return;
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
                matchedSlots.put(controllerName, null);
            }
        });
        animations.forEach((animName, animValue) -> {
            if (!animValue.isEmpty() && this.animationNameMatcher.test(animName)) {
                matchedSlots.put(String.format("%s.%s_%s", this.prefix, this.slotName, animName.substring(this.slotName.length())), animName);
            }
        });
        return (entity, consumer) -> {
            for (Map.Entry<String, String> slot : matchedSlots.entrySet()) {
                consumer.accept(this.controllerFactory.apply(slot.getKey(), entity, slot.getValue()));
            }
        };
    }
}
