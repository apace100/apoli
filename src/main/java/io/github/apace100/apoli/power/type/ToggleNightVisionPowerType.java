package io.github.apace100.apoli.power.type;


import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;

public class ToggleNightVisionPowerType extends NightVisionPowerType implements Active {

    private final Key key;
    private boolean toggled;

    public ToggleNightVisionPowerType(Power power, LivingEntity entity, boolean activeByDefault, float strength, Key key) {
        super(power, entity, strength);
        this.toggled = activeByDefault;
        this.key = key;
    }

    @Override
    public void onUse() {
        this.toggled = !this.toggled;
        PowerHolderComponent.syncPower(entity, this.power);
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

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("toggle_night_vision"),
            new SerializableData()
                .add("active_by_default", SerializableDataTypes.BOOLEAN, false)
                .add("strength", SerializableDataTypes.FLOAT, 1.0F)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data -> (power, entity) -> new ToggleNightVisionPowerType(power, entity,
                data.get("active_by_default"),
                data.get("strength"),
                data.get("key")
            )
        ).allowCondition();
    }
}
