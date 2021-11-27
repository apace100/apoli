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

public class TogglePower extends Power implements Active {

    private boolean isActive;
    private final boolean shouldRetainState;

    public TogglePower(PowerType<?> type, LivingEntity entity) {
        this(type, entity, false, true);
    }

    public TogglePower(PowerType<?> type, LivingEntity entity, boolean activeByDefault) {
        this(type, entity, activeByDefault, true);
    }

    public TogglePower(PowerType<?> type, LivingEntity entity, boolean activeByDefault, boolean shouldRetainState) {
        super(type, entity);
        this.shouldRetainState = shouldRetainState;
        this.isActive = activeByDefault;
    }

    @Override
    public boolean shouldTick() {
        return !shouldRetainState && this.conditions.size() > 0;
    }

    @Override
    public boolean shouldTickWhenInactive() {
        return true;
    }

    @Override
    public void tick() {
        if(!super.isActive() && this.isActive) {
            this.isActive = false;
            PowerHolderComponent.syncPower(entity, this.type);
        }
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
        isActive = ((NbtByte)tag).byteValue() > 0;
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
        return new PowerFactory<TogglePower>(Apoli.identifier("toggle"),
            new SerializableData()
                .add("active_by_default", SerializableDataTypes.BOOLEAN, true)
                .add("retain_state", SerializableDataTypes.BOOLEAN, true)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    TogglePower power = new TogglePower(type, player,
                        data.getBoolean("active_by_default"),
                        data.getBoolean("retain_state"));
                    power.setKey(data.get("key"));
                    return power;
                })
            .allowCondition();
    }
}
