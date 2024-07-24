package io.github.apace100.apoli.mixin;

import net.minecraft.client.gui.LayeredDrawer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(LayeredDrawer.class)
public interface LayeredDrawerAccessor {

    @Accessor
    List<LayeredDrawer.Layer> getLayers();

}
