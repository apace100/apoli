package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PowerType {

    protected LivingEntity entity;
    protected Power power;

    private boolean shouldTick = false;
    private boolean shouldTickWhenInactive = false;

    protected List<Predicate<Entity>> conditions;

    public PowerType(Power power, LivingEntity entity) {
        this.power = power;
        this.entity = entity;
        this.conditions = new LinkedList<>();
    }

    public PowerType addCondition(Predicate<Entity> condition) {
        this.conditions.add(condition);
        return this;
    }

    protected void setTicking() {
        this.setTicking(false);
    }

    protected void setTicking(boolean evenWhenInactive) {
        this.shouldTick = true;
        this.shouldTickWhenInactive = evenWhenInactive;
    }

    public boolean shouldTick() {
        return shouldTick;
    }

    public boolean shouldTickWhenInactive() {
        return shouldTickWhenInactive;
    }

    public void tick() {

    }

    public void onGained() {

    }

    public void onLost() {

    }

    public void onAdded() {

    }

    public void onRemoved() {

    }

    public void onRespawn() {

    }

    public boolean isActive() {
        return conditions.stream().allMatch(condition -> condition.test(entity));
    }

    public NbtElement toTag() {
        return new NbtCompound();
    }

    public void fromTag(NbtElement tag) {

    }

    public Power getPower() {
        return power;
    }

    public Identifier getId() {
        return this.getPower().getId();
    }

    public static <T extends PowerType> PowerTypeFactory<T> createSimpleFactory(Identifier id, BiFunction<Power, LivingEntity, T> powerConstructor) {
        return new PowerTypeFactory<>(id, new SerializableData(), data -> powerConstructor).allowCondition();
    }

}
