package io.github.apace100.apoli.access;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public interface SubmergableEntity {

    boolean apoli$isSubmergedInLoosely(TagKey<Fluid> fluidTag);

    double apoli$getFluidHeightLoosely(TagKey<Fluid> fluidTag);
}
