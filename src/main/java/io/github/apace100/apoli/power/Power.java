package io.github.apace100.apoli.power;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.PowerFactory;
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

public class Power {

    protected LivingEntity entity;
    protected PowerType<?> type;

    protected SerializableData.Instance dataInstance;
    protected SerializableData serializableData;

    private boolean shouldTick = false;
    private boolean shouldTickWhenInactive = false;

    protected List<Predicate<Entity>> conditions;

    public Power(PowerType<?> type, LivingEntity entity) {
        this.type = type;
        this.entity = entity;
        this.conditions = new LinkedList<>();
    }

    public Power addCondition(Predicate<Entity> condition) {
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

    protected final void setDataInstance(SerializableData.Instance dataInstance) {
        this.dataInstance = dataInstance;
    }

    protected final void setSerializableData(SerializableData serializableData) {
        this.serializableData = serializableData;
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

    public void onAdded(boolean onSync) {

    }

    public void onRemoved() {

    }

    public void onRemoved(boolean onSync) {

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

    public JsonObject toJson() {
        return serializableData.write(dataInstance);
    }

    public PowerType<?> getType() {
        return type;
    }

    public static PowerFactory createSimpleFactory(BiFunction<PowerType, LivingEntity, Power> powerConstructor, Identifier identifier) {
        return new PowerFactory<>(identifier,
            new SerializableData(), data -> powerConstructor::apply).allowCondition();
    }

}
