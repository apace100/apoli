package io.github.apace100.apoli.power.factory.behavior.types;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.behavior.MobBehaviorFactory;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.world.GameRules;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class RevengeMobBehavior extends HostileMobBehavior {
    @Nullable
    private UUID revengeTarget;
    private int revengeTimer;
    private final int resetTime;

    public RevengeMobBehavior(MobEntity mob, int priority, Predicate<Pair<LivingEntity, LivingEntity>> bientityCondition, int attackCooldown, float speed, int resetTime) {
        super(mob, priority, bientityCondition, attackCooldown, speed);
        this.resetTime = resetTime;
    }

    @Override
    public void tick() {
        super.tick();

        if (revengeTarget != null) {
            ++this.revengeTimer;
        }

        Entity entity = ((ServerWorld)mob.getWorld()).getEntity(revengeTarget);
        if (entity instanceof LivingEntity living && !living.isAlive() && (!(living instanceof PlayerEntity) || mob.getWorld().getGameRules().getBoolean(GameRules.FORGIVE_DEAD_PLAYERS)) || revengeTimer > resetTime) {
            this.revengeTarget = null;
            this.revengeTimer = 0;
            this.resetAttackTargets();
        }
    }

    @Override
    public void onAttacked(Entity attacker) {
        if (attacker instanceof LivingEntity livingAttacker && this.doesApply(livingAttacker)) {
            Apoli.LOGGER.info("On attacked");
            this.revengeTarget = livingAttacker.getUuid();
            this.revengeTimer = 0;
        }

        super.onAttacked(attacker);
    }

    public boolean isHostile(LivingEntity target) {
        return super.isHostile(target) && target.getUuid().equals(revengeTarget);
    }

    @Override
    public boolean isPassive(LivingEntity target) {
        return this.doesApply(target) && !target.getUuid().equals(revengeTarget);
    }

    public NbtElement toTag() {
        NbtCompound compound = new NbtCompound();
        if (revengeTarget != null) {
            compound.putUuid("RevengeTarget", revengeTarget);
            compound.putInt("RevengeTimer", revengeTimer);
        }
        return compound;
    }

    public void fromTag(NbtElement tag) {
        if (tag instanceof NbtCompound compound && compound.containsUuid("RevengeTarget")) {
            this.revengeTarget = compound.getUuid("RevengeTarget");
            this.revengeTimer = compound.getInt("RevengeTimer");
        }
    }

    public static MobBehaviorFactory<?> createFactory() {
        return new MobBehaviorFactory<>(Apoli.identifier("revenge"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
                        .add("attack_cooldown", SerializableDataTypes.INT, 20)
                        .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                        .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                        .add("speed", SerializableDataTypes.FLOAT, 1.0F)
                        .add("reset_time", SerializableDataTypes.INT),
                (data, mob) -> {
                    RevengeMobBehavior behavior = new RevengeMobBehavior(mob, data.getInt("priority"), data.get("bientity_condition"), data.getInt("attack_cooldown"), data.getFloat("speed"), data.getInt("reset_time"));
                    if (data.isPresent("modifier")) {
                        behavior.addModifier(data.get("modifier"));
                    }
                    if (data.isPresent("modifiers")) {
                        ((List<AttributedEntityAttributeModifier>)data.get("modifiers")).forEach(behavior::addModifier);
                    }
                    return behavior;
                });
    }
}
