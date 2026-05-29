package com.elfmcys.yesstevemodel.client.model.processor;

import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ControllerSlotBinder<T extends GeoEntity<?>, TModel> implements ModelProcessor<T, TModel> {

    private final Predicate<String> controllerNameMatcher;

    private final Predicate<String> molangEventMatcher;

    private final AnimationDataProvider<TModel> animationDataProvider;

    private final BiFunction<String, T, IAnimationController<T>> controllerFactory;

    public ControllerSlotBinder(String prefix, String slotName, AnimationDataProvider<TModel> animationDataProvider, BiFunction<String, T, IAnimationController<T>> controllerFactory) {
        Pattern controllerPattern = Pattern.compile(String.format("^%s\\.%s(_.+){0,1}$", prefix, slotName));
        Pattern molangPattern = Pattern.compile(String.format("^%s_ctrl_%s(_.+){0,1}$", prefix, slotName));
        this.controllerNameMatcher = s -> controllerPattern.matcher(s).matches();
        this.molangEventMatcher = s -> molangPattern.matcher(s).matches();
        this.animationDataProvider = animationDataProvider;
        this.controllerFactory = controllerFactory;
    }

    @Override
    public ControllerFactory<T> process(TModel modelData, ModelResourceBundle resourceBundle) {
        ObjectRBTreeSet<String> controllerNames = new ObjectRBTreeSet<>();
        this.animationDataProvider.getAnimationEntries(modelData, resourceBundle).forEach((key, value) -> {
            if (this.controllerNameMatcher.test(key)) {
                controllerNames.add(key);
            }
        });
        resourceBundle.getEvents().forEach((key, value) -> {
            if (this.molangEventMatcher.test(key)) {
                controllerNames.add(key.replace("_ctrl_", "."));
            }
        });
        return (entity, consumer) -> {
            for (String controllerName : controllerNames) {
                consumer.accept(this.controllerFactory.apply(controllerName, entity));
            }
        };
    }
}
