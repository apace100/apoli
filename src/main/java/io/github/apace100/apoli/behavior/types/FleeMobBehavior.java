package io.github.apace100.apoli.behavior.types;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.ModifiableMobWithGoals;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.apoli.mixin.MobEntityAccessor;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Pair;

public class FleeMobBehavior extends MobBehavior {
    private final float fleeDistance;
    private final double slowSpeed;
    private final double fastSpeed;

    public FleeMobBehavior(int priority, float fleeDistance, double slowSpeed, double fastSpeed) {
        super(priority);
        this.fleeDistance = fleeDistance;
        this.slowSpeed = slowSpeed;
        this.fastSpeed = fastSpeed;
    }

    @Override
    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        super.setToDataInstance(dataInstance);
        dataInstance.set("distance", this.fleeDistance);
        dataInstance.set("slow_speed", this.slowSpeed);
        dataInstance.set("fast_speed", this.fastSpeed);
    }

    @Override
    public void initGoals(MobEntity mob) {
        if (!(mob instanceof PathAwareEntity)) return;
        Goal fleeGoal = new FleeEntityGoal<>((PathAwareEntity) mob, LivingEntity.class, fleeDistance, this.slowSpeed, this.fastSpeed, entity ->
                mobRelatedPredicates.test(new Pair<>(entity, mob)) && entityRelatedPredicates.test(entity));
        ((MobEntityAccessor)mob).getGoalSelector().add(this.priority, fleeGoal);
        ((ModifiableMobWithGoals)mob).getModifiedTargetSelectorGoals().add(new Pair<>(this, fleeGoal));
    }

    @Override
    public boolean isPassive(MobEntity mob, LivingEntity target) {
        return true;
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("flee"),
                new SerializableData()
                        .add("priority", SerializableDataTypes.INT, 0)
                        .add("distance", SerializableDataTypes.FLOAT)
                        .add("slow_speed", SerializableDataTypes.DOUBLE)
                        .add("fast_speed", SerializableDataTypes.DOUBLE),
                data -> new FleeMobBehavior(data.getInt("priority"), data.getFloat("distance"), data.getDouble("slow_speed"), data.getDouble("fast_speed")));
    }
}