package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.behavior.MobBehaviorFactory;
import io.github.apace100.apoli.power.factory.behavior.MobBehavior;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class ModifyMobBehaviorPower extends Power {

    private final MobBehavior mobBehavior;

    private final int tickRate;

    public ModifyMobBehaviorPower(PowerType<?> type, LivingEntity entity, MobBehavior mobBehavior, int tickRate) {
        super(type, entity);
        this.mobBehavior = mobBehavior;
        this.tickRate = tickRate;
        this.setTicking(true);
    }

    @Override
    public void tick() {
        if (entity.world.isClient || entity.age % tickRate != 0) return;

        this.getMobBehavior().baseTick();
    }

    @Override
    public void onAdded() {
        if (entity.world.isClient) return;
        if (mobBehavior.usesGoals()) {
            mobBehavior.applyGoals();
        } else if (mobBehavior.usesBrain()) {
            mobBehavior.applyActivities();
        }
    }

    @Override
    public void onRemoved() {
        if (entity.world.isClient) return;
        this.mobBehavior.onRemoved();
        this.mobBehavior.removeGoals();
        this.mobBehavior.removeTasks();
    }

    public MobBehavior getMobBehavior() {
        return mobBehavior;
    }

    public NbtElement toTag() {
        return mobBehavior.toTag();
    }

    public void fromTag(NbtElement tag) {

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_mob_behavior"),
                new SerializableData()
                        .add("behavior", ApoliDataTypes.MOB_BEHAVIOR)
                        .add("tick_rate", SerializableDataTypes.INT, 10),
                data ->
                        (type, entity) -> {
                            if (!(entity instanceof MobEntity mob)) {
                                Apoli.LOGGER.warn("Tried applying ModifyMobBehavior power with id '{}' to non mob, the power will not do anything.", type.getIdentifier());
                                return new Power(type, entity);
                            }
                            return new ModifyMobBehaviorPower(type, entity, ((MobBehaviorFactory.Instance)data.get("behavior")).apply(mob), data.getInt("tick_rate"));
                        });
    }
}