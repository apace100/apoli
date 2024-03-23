package io.github.apace100.apoli.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(Entity.class)
public interface EntityAccessor {

	@Invoker
	boolean callIsBeingRainedOn();

	@Accessor("commandTags")
	Set<String> getOriginalCommandTags();

}
