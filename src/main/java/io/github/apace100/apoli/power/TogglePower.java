package io.github.apace100.apoli.power;

import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;

public class TogglePower extends Power implements Active {

    private boolean isActive;

    public TogglePower(PowerType<?> type, LivingEntity entity) {
        this(type, entity, false);
    }

    public TogglePower(PowerType<?> type, LivingEntity entity, boolean activeByDefault) {
        super(type, entity);
        this.isActive = activeByDefault;
    }

    @Override
    public void onUse() {
        this.isActive = !this.isActive;
        PowerHolderComponent.sync(entity);
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
}
