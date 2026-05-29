package com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers;

import com.elfmcys.yesstevemodel.client.animation.StopAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.client.animation.predicate.EquipmentSlotAnimationPredicate;
import com.elfmcys.yesstevemodel.client.animation.predicate.NamedAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity;
import com.elfmcys.yesstevemodel.client.model.AnimationDataProvider;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.client.model.PlayerModelBundle;
import com.elfmcys.yesstevemodel.client.model.processor.ArmorSlotProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.NamedModelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.ParallelProcessor;
import com.elfmcys.yesstevemodel.client.model.processor.ProcessorPipeline;
import com.elfmcys.yesstevemodel.client.model.processor.TriFunction;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.CompositeAnimationController;
import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import net.minecraft.inventory.EntityEquipmentSlot;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class FirstPersonArmAnimationController {
    private static final String FP_ARM_PREFIX = "fp.arm";
    private static final ProcessorPipeline<PlayerGeoEntity, PlayerModelBundle> REGISTRY = new ProcessorPipeline<>();

    private FirstPersonArmAnimationController() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerControllers() {
        registerNamedController("misc", null, true, (animationEntryKey, entity) ->
                new CompositeAnimationController(entity, animationEntryKey, 0.0f, new StopAnimationPredicate()));
        registerParallelController("parallel", (animationEntryKey, entity, linkedAnimationName) ->
                new CompositeAnimationController(entity, animationEntryKey, 0.0f,
                        linkedAnimationName != null ? new NamedAnimationPredicate(linkedAnimationName) : StopAnimationPredicate.INSTANCE, true));
        registerArmorController("armor", (animationEntryKey, entity, equipmentSlot) ->
                new CompositeAnimationController(entity, animationEntryKey, 0.0f, new EquipmentSlotAnimationPredicate(equipmentSlot)));
    }

    public static Consumer<PlayerGeoEntity> buildControllers(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
        if (REGISTRY.isEmpty()) {
            registerControllers();
        }
        return REGISTRY.buildAll(modelBundle, resourceBundle);
    }

    @SuppressWarnings("unused")
    private static void registerController(String slotName, BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        String controllerKey = String.format("%s.%s", FP_ARM_PREFIX, slotName);
        REGISTRY.register((modelData, resourceBundle) -> (entity, consumer) -> consumer.accept(controllerFactory.apply(controllerKey, entity)));
    }

    private static void registerNamedController(String slotName, String[] requiredAnimations, boolean checkAnimationEntries, BiFunction<String, PlayerGeoEntity, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        REGISTRY.register(new NamedModelProcessor<>(FP_ARM_PREFIX, slotName, requiredAnimations, checkAnimationEntries, ArmAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static void registerParallelController(String slotName, TriFunction<String, PlayerGeoEntity, String, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        REGISTRY.register(new ParallelProcessor<>(FP_ARM_PREFIX, slotName, true, ArmAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static void registerArmorController(String category, TriFunction<String, PlayerGeoEntity, EntityEquipmentSlot, IAnimationController<PlayerGeoEntity>> controllerFactory) {
        REGISTRY.register(new ArmorSlotProcessor<>(FP_ARM_PREFIX, category, ArmAnimationDataProvider.INSTANCE, controllerFactory));
    }

    private static class ArmAnimationDataProvider implements AnimationDataProvider<PlayerModelBundle> {
        private static final ArmAnimationDataProvider INSTANCE = new ArmAnimationDataProvider();

        private ArmAnimationDataProvider() {
        }

        @Override
        public Object2ReferenceMap<String, AnimationController> getAnimationEntries(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getAnimationControllers();
        }

        @Override
        public Object2ReferenceMap<String, Animation> getAnimations(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getArmAnimations();
        }

        @Override
        public ConditionArmor getConditionArmor(PlayerModelBundle modelBundle, ModelResourceBundle resourceBundle) {
            return modelBundle.getModelProcessor().getConditionArmor();
        }
    }
}
