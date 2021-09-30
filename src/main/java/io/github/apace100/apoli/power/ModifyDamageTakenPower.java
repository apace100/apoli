package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyDamageTakenPower extends ValueModifyingPower {

    private final Predicate<Pair<DamageSource, Float>> condition;

    private Consumer<Entity> attackerAction;
    private Consumer<Entity> selfAction;

    public ModifyDamageTakenPower(PowerType<?> type, LivingEntity entity, Predicate<Pair<DamageSource, Float>> condition) {
        super(type, entity);
        this.condition = condition;
    }

    public boolean doesApply(DamageSource source, float damageAmount) {
        return condition.test(new Pair(source, damageAmount));
    }

    public void setAttackerAction(Consumer<Entity> attackerAction) {
        this.attackerAction = attackerAction;
    }

    public void setSelfAction(Consumer<Entity> selfAction) {
        this.selfAction = selfAction;
    }

    public void executeActions(Entity attacker) {
        if(selfAction != null) {
            selfAction.accept(entity);
        }
        if(attackerAction != null) {
            attackerAction.accept(attacker);
        }
    }
}
