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

public class TogglePowerType extends PowerType implements Active {

    private final Key key;
    private final boolean shouldRetainState;

    private boolean toggled;

    public TogglePowerType(Power power, LivingEntity entity, boolean activeByDefault, boolean shouldRetainState, Key key) {
        super(power, entity);
        this.key = key;
        this.shouldRetainState = shouldRetainState;
        this.toggled = activeByDefault;
    }

    @Override
    public boolean shouldTick() {
        return !shouldRetainState && !this.conditions.isEmpty();
    }

    @Override
    public boolean shouldTickWhenInactive() {
        return true;
    }

    @Override
    public void tick() {

        if (!super.isActive() && this.toggled) {
            this.toggled = false;
            PowerHolderComponent.syncPower(entity, this.power);
        }

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
            this.toggled = nbtByte.byteValue() > 0;
        }

    }

    @Override
    public Key getKey() {
        return key;
    }

    public static PowerTypeFactory<TogglePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("toggle"),
            new SerializableData()
                .add("active_by_default", SerializableDataTypes.BOOLEAN, true)
                .add("retain_state", SerializableDataTypes.BOOLEAN, true)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data -> (power, entity) -> new TogglePowerType(power, entity,
                data.get("active_by_default"),
                data.get("retain_state"),
                data.get("key")
            )
        ).allowCondition();
    }

}
