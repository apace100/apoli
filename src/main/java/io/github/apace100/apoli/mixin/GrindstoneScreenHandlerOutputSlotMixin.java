package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.power.ModifyGrindstonePower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Objects;

@Mixin(targets = "net/minecraft/screen/GrindstoneScreenHandler$4")
public class GrindstoneScreenHandlerOutputSlotMixin {

    @Final
    @Shadow
    GrindstoneScreenHandler field_16780;

    @ModifyReturnValue(method = "getExperience(Lnet/minecraft/world/World;)I", at = @At("RETURN"))
    private int apoli$modifyExperience(int original, World world) {

        if (!(field_16780 instanceof PowerModifiedGrindstone powerModifiedGrindstone)) {
            return original;
        }

        List<Modifier> modifiers = powerModifiedGrindstone.apoli$getAppliedPowers()
            .stream()
            .map(ModifyGrindstonePower::getExperienceModifier)
            .filter(Objects::nonNull)
            .toList();

        return (int) ModifierUtil.applyModifiers(powerModifiedGrindstone.apoli$getPlayer(), modifiers, original);

    }

}
