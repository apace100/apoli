package io.github.apace100.apoli.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Explosion.class)
public interface ExplosionAccessor {

    @Nullable
    @Invoker
    static LivingEntity callGetCausingEntity(@Nullable Entity fromEntity) {
        throw new AssertionError();
    }

}
