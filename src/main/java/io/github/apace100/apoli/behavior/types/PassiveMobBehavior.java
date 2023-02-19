package io.github.apace100.apoli.behavior.types;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.behavior.BehaviorFactory;
import io.github.apace100.apoli.behavior.MobBehavior;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public class PassiveMobBehavior extends MobBehavior {
    public PassiveMobBehavior(int priority) {
        super(priority);
    }

    @Override
    public boolean isPassive(MobEntity mob, LivingEntity target) {
        return true;
    }

    @Override
    protected void setToDataInstance(SerializableData.Instance dataInstance) {
        super.setToDataInstance(dataInstance);
    }

    public static BehaviorFactory<?> createFactory() {
        return new BehaviorFactory<>(Apoli.identifier("passive"),
                new SerializableData(),
                data -> new PassiveMobBehavior(0));
    }
}