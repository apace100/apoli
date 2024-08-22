package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyBlockRenderPowerType;
import io.github.apace100.apoli.power.type.ModifyFluidRenderPowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ChunkRendererRegion.class)
public class ChunkRendererRegionMixin {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void modifyBlockRender(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null && client.player != null) {
            for(ModifyBlockRenderPowerType power : PowerHolderComponent.getPowerTypes(client.player, ModifyBlockRenderPowerType.class)) {
                if(power.doesPrevent(client.world, pos)) {
                    cir.setReturnValue(power.getBlockState());
                    return;
                }
            }
        }
    }

    @Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
    private void modifyFluidRender(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.world != null && client.player != null) {
            for(ModifyFluidRenderPowerType power : PowerHolderComponent.getPowerTypes(client.player, ModifyFluidRenderPowerType.class)) {
                if(power.doesPrevent(client.world, pos)) {
                    cir.setReturnValue(power.getFluidState());
                    return;
                }
            }
        }
    }
}
