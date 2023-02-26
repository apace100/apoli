package io.github.apace100.apoli.mixin;

import com.mojang.datafixers.kinds.App;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TaskTriggerer.class)
public interface TaskTriggererAccessor {
    @Invoker
    static <E extends LivingEntity, M> TaskTriggerer.TaskFunction<E, M> invokeGetFunction(App<TaskTriggerer.K1<E>, M> app) {
        throw new RuntimeException("Evil invoker exception! >:)");
    }
}
