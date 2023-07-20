package io.github.apace100.apoli.access;

import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;

public interface SubmergableEntity {

    boolean isSubmergedInLoosely(TagKey<Fluid> fluidTag);

    double getFluidHeightLoosely(TagKey<Fluid> fluidTag);
}
