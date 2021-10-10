package io.github.apace100.apoli.power;


import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;

public class ToggleNightVisionPower extends NightVisionPower implements Active {
    private boolean isActive;

    public ToggleNightVisionPower(PowerType<?> type, LivingEntity entity) {
        this(type, entity, 1.0F, true);
    }

    public ToggleNightVisionPower(PowerType<?> type, LivingEntity entity, float strength, boolean activeByDefault) {
        super(type, entity, strength);
        this.isActive = activeByDefault;
    }

    @Override
    public void onUse() {
        this.isActive = !this.isActive;
        PowerHolderComponent.syncPower(entity, this.type);
    }

    public boolean isActive() {
        return this.isActive && super.isActive();
    }

    @Override
    public NbtElement toTag() {
        return NbtByte.of(isActive);
    }

    @Override
    public void fromTag(NbtElement tag) {
        if(tag instanceof NbtByte) {
            isActive = ((NbtByte)tag).byteValue() > 0;
        }
    }

    private Key key;

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("toggle_night_vision"),
            new SerializableData()
                .add("active_by_default", SerializableDataTypes.BOOLEAN, false)
                .add("strength", SerializableDataTypes.FLOAT, 1.0F)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, entity) -> {
                    ToggleNightVisionPower power = new ToggleNightVisionPower(type, entity, data.getFloat("strength"), data.getBoolean("active_by_default"));
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                })
            .allowCondition();
    }
}