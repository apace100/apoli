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
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;

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
        if (entity.getWorld().isClient || entity.age % tickRate != 0) return;

        this.getMobBehavior().baseTick();
    }

    @Override
    public void onAdded() {
        if (entity.getWorld().isClient) return;
        if (mobBehavior.usesGoals()) {
            mobBehavior.applyGoals();
        } else if (mobBehavior.usesBrain()) {
            mobBehavior.applyActivities();
        }
    }

    @Override
    public void onRemoved() {
        if (entity.getWorld().isClient) return;
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
        this.mobBehavior.fromTag(tag);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_mob_behavior"),
                new SerializableData()
                        .add("behavior", ApoliDataTypes.MOB_BEHAVIOR)
                        .add("tick_rate", SerializableDataTypes.INT, 1),
                data ->
                        (type, entity) -> {
                            if (!(entity instanceof MobEntity mob)) {
                                if (entity != null) {
                                    Apoli.LOGGER.warn("Tried applying ModifyMobBehavior power with id '{}' to an entity with the type '{}', which is not a mob. This power will not do anything.", type.getIdentifier(), Registries.ENTITY_TYPE.getId(entity.getType()));
                                }
                                return new Power(type, entity);
                            }
                            return new ModifyMobBehaviorPower(type, entity, ((MobBehaviorFactory.Instance)data.get("behavior")).apply(mob), data.getInt("tick_rate"));
                        });
    }
}