package io.github.apace100.apoli.access;

import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;

public interface SubmergableEntity {

    boolean isSubmergedInLoosely(Tag<Fluid> fluidTag);

    double getFluidHeightLoosely(Tag<Fluid> fluidTag);
}
