package io.github.apace100.apoli.power;


import io.github.apace100.apoli.component.PowerHolderComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
}