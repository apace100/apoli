package io.github.apace100.apoli.power.type;

import de.dafuqs.additionalentityattributes.AdditionalEntityAttributes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Deprecated
public class LavaVisionPowerType extends PowerType implements AttributeModifying {

    public static final TypedDataObjectFactory<LavaVisionPowerType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("v", SerializableDataTypes.FLOAT),
        data -> new LavaVisionPowerType(
            data.get("v")
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("v", powerType.v)
    );

    private final AttributedEntityAttributeModifier modifier;
    private final float v;

    public LavaVisionPowerType(float v) {
        this.modifier = new AttributedEntityAttributeModifier(AdditionalEntityAttributes.LAVA_VISIBILITY, new EntityAttributeModifier(this.getPower().getId(), v - 1, EntityAttributeModifier.Operation.ADD_VALUE));
        this.v = v;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.LAVA_VISION;
    }

    @Override
    public void onAdded() {
        applyTempModifiers(getHolder());
    }

    @Override
    public void onRemoved() {
        removeTempModifiers(getHolder());
    }

    @Override
    public List<AttributedEntityAttributeModifier> attributedModifiers() {
        return ObjectArrayList.of(modifier);
    }

    @Override
    public boolean shouldUpdateHealth() {
        return false;
    }

}
