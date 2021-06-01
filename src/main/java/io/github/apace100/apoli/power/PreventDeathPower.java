package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventDeathPower extends Power {

    private final Consumer<Entity> entityAction;
    private final Predicate<Pair<DamageSource, Float>> condition;

    public PreventDeathPower(PowerType<?> type, LivingEntity entity, Consumer<Entity> entityAction, Predicate<Pair<DamageSource, Float>> condition) {
        super(type, entity);
        this.entityAction = entityAction;
        this.condition = condition;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return condition == null || condition.test(new Pair<>(source, amount));
    }

    public void executeAction() {
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }
}
