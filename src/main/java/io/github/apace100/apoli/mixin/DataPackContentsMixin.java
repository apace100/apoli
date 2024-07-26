package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.apace100.apoli.Apoli;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Mixin(DataPackContents.class)
public abstract class DataPackContentsMixin {

    @Inject(method = "method_58296", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/SimpleResourceReload;start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;"))
    private static void apoli$cacheDataPackContents(FeatureSet featureSet, CommandManager.RegistrationEnvironment registrationEnvironment, int i, ResourceManager resourceManager, Executor executor, Executor executor2, CombinedDynamicRegistries<?> reloadedDynamicRegistries, CallbackInfoReturnable<CompletionStage<?>> cir, @Local DataPackContents dataPackContents) {
        Apoli.DATA_PACK_CONTENTS.put(Unit.INSTANCE, dataPackContents);
    }

}
