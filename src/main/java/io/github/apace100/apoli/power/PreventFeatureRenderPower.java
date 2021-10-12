package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.ClassDataRegistry;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.entity.LivingEntity;

import java.util.LinkedList;
import java.util.List;

public class PreventFeatureRenderPower extends Power {

    private final List<Class<? extends FeatureRenderer<?, ?>>> classes = new LinkedList<>();

    public PreventFeatureRenderPower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    public void addClass(Class<? extends FeatureRenderer<?, ?>> cls) {
        classes.add(cls);
    }

    public boolean doesApply(Class<? extends FeatureRenderer<?, ?>> cls) {
        return classes.stream().anyMatch(c -> c.isAssignableFrom(cls));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("prevent_feature_render"),
            new SerializableData()
                .add("feature", ClassDataRegistry.get(FeatureRenderer.class).get().getDataType(), null)
                .add("features", ClassDataRegistry.get(FeatureRenderer.class).get().getListDataType(), null),
            data ->
                (type, entity) -> {
                    PreventFeatureRenderPower power = new PreventFeatureRenderPower(type, entity);
                    data.ifPresent("feature", power::addClass);
                    data.<List<Class<FeatureRenderer<?, ?>>>>ifPresent("features",
                        list -> list.forEach(power::addClass));
                    return power;
                })
            .allowCondition();
    }
}
