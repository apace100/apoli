package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.function.Consumer;

public class ActionOnCallbackPower extends Power {

    private final Consumer<Entity> entityActionRespawned;
    private final Consumer<Entity> entityActionRemoved;
    private final Consumer<Entity> entityActionGained;
    private final Consumer<Entity> entityActionLost;
    private final Consumer<Entity> entityActionAdded;

    public ActionOnCallbackPower(PowerType<?> type, LivingEntity entity, Consumer<Entity> entityActionRespawned, Consumer<Entity> entityActionRemoved, Consumer<Entity> entityActionGained, Consumer<Entity> entityActionLost, Consumer<Entity> entityActionAdded) {
        super(type, entity);
        this.entityActionRespawned = entityActionRespawned;
        this.entityActionRemoved = entityActionRemoved;
        this.entityActionGained = entityActionGained;
        this.entityActionLost = entityActionLost;
        this.entityActionAdded = entityActionAdded;
    }

    @Override
    public void onRespawn() {
        if(entityActionRespawned != null) {
            entityActionRespawned.accept(entity);
        }
    }

    @Override
    public void onGained() {
        if(entityActionGained != null) {
            entityActionGained.accept(entity);
        }
    }

    @Override
    public void onRemoved() {
        if(entityActionRemoved != null) {
            entityActionRemoved.accept(entity);
        }
    }

    @Override
    public void onLost() {
        if(entityActionLost != null) {
            entityActionLost.accept(entity);
        }
    }

    @Override
    public void onAdded() {
        if(entityActionAdded != null) {
            entityActionAdded.accept(entity);
        }
    }
}
