package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.registry.ApoliClassDataClient;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PreventFeatureRenderPowerType extends PowerType {

    public static final TypedDataObjectFactory<PreventFeatureRenderPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("feature", SerializableDataTypes.STRING, null)
            .addFunctionedDefault("features", SerializableDataTypes.STRINGS, data -> MiscUtil.singletonListOrEmpty(data.get("feature"))),
        (data, condition) -> new PreventFeatureRenderPowerType(
            data.get("features"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("features", powerType.featureRendererReferences)
    );

    private final List<String> featureRendererReferences;

    public PreventFeatureRenderPowerType(List<String> featureRendererReferences, Optional<EntityCondition> condition) {
        super(condition);
        this.featureRendererReferences = featureRendererReferences
            .stream()
            .distinct()
            .collect(Collectors.toCollection(ObjectArrayList::new));
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.PREVENT_FEATURE_RENDER;
    }

    @Environment(EnvType.CLIENT)
    public <T extends FeatureRenderer<?, ?>> boolean doesApply(T featureRenderer) {
        return featureRendererReferences.isEmpty() || featureRendererReferences
            .stream()
            .map(ApoliClassDataClient.FEATURE_RENDERERS::mapStringToClass)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .anyMatch(cls -> cls.isAssignableFrom(featureRenderer.getClass()));
    }

}
