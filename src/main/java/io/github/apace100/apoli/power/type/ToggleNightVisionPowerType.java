package io.github.apace100.apoli.power.type;


import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ToggleNightVisionPowerType extends NightVisionPowerType implements Active {

    public static final TypedDataObjectFactory<ToggleNightVisionPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Key())
            .add("active_by_default", SerializableDataTypes.BOOLEAN, false)
            .add("strength", SerializableDataTypes.FLOAT, 1.0F),
        (data, condition) -> new ToggleNightVisionPowerType(
            data.get("key"),
            data.get("active_by_default"),
            data.get("strength"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("key", powerType.getKey())
            .set("active_by_default", powerType.activeByDefault)
            .set("strength", powerType.getStrength())
    );

    private final Key key;
    private final boolean activeByDefault;

    private boolean toggled;

    public ToggleNightVisionPowerType(Key key, boolean activeByDefault, float strength, Optional<EntityCondition> condition) {
        super(strength, condition);
        this.key = key;
        this.activeByDefault = activeByDefault;
        this.toggled = activeByDefault;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.TOGGLE_NIGHT_VISION;
    }

    @Override
    public void onUse() {
        this.toggled = !this.toggled;
        PowerHolderComponent.syncPower(getHolder(), getPower());
    }

    public boolean isActive() {
        return this.toggled && super.isActive();
    }

    @Override
    public NbtElement toTag() {
        return NbtByte.of(toggled);
    }

    @Override
    public void fromTag(NbtElement tag) {

        if (tag instanceof NbtByte nbtByte) {
            toggled = nbtByte.byteValue() > 0;
        }

    }

    @Override
    public Key getKey() {
        return key;
    }

}
