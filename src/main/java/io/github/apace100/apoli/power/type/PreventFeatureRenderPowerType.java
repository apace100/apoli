package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.registry.ApoliClassDataClient;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.entity.LivingEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PreventFeatureRenderPowerType extends PowerType {

    private final Set<String> classStrings;

    public PreventFeatureRenderPowerType(Power power, LivingEntity entity, String classString, List<String> classStrings) {
        super(power, entity);
        this.classStrings = new HashSet<>();

        if (classString != null) {
            this.classStrings.add(classString);
        }

        if (classStrings != null) {
            this.classStrings.addAll(classStrings);
        }

    }

    @Environment(EnvType.CLIENT)
    public <T extends FeatureRenderer<?, ?>> boolean doesApply(T featureRenderer) {
        return classStrings.isEmpty() || classStrings
            .stream()
            .map(ApoliClassDataClient.FEATURE_RENDERERS::mapStringToClass)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .anyMatch(cls -> cls.isAssignableFrom(featureRenderer.getClass()));
    }

    public static PowerTypeFactory<PreventFeatureRenderPowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_feature_render"),
            new SerializableData()
                .add("feature", SerializableDataTypes.STRING, null)
                .add("features", SerializableDataTypes.STRINGS, null),
            data -> (power, entity) -> new PreventFeatureRenderPowerType(power, entity,
                data.get("feature"),
                data.get("features")
            )
        ).allowCondition();
    }

}
