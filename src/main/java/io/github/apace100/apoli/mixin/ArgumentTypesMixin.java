package io.github.apace100.apoli.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.command.PowerHolderArgumentType;
import io.github.apace100.apoli.command.PowerOperation;
import io.github.apace100.apoli.command.PowerTypeArgumentType;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArgumentTypes.class)
public abstract class ArgumentTypesMixin {
    @Shadow
    private static <A extends ArgumentType<?>, T extends ArgumentSerializer.ArgumentTypeProperties<A>> ArgumentSerializer<A, T> register(Registry<ArgumentSerializer<?, ?>> registry, String string, Class<? extends A> clazz, ArgumentSerializer<A, T> argumentSerializer) {
        throw new AssertionError("Mixins for basic functionality are fun.");
    }
    @Inject(method = "register(Lnet/minecraft/registry/Registry;)Lnet/minecraft/command/argument/serialize/ArgumentSerializer;", at = @At("RETURN"))
    private static void registerApoliArgumentTypes(Registry<ArgumentSerializer<?, ?>> registry, CallbackInfoReturnable<ArgumentSerializer<?, ?>> cir) {
        register(registry, Apoli.MODID + ":power", PowerTypeArgumentType.class, ConstantArgumentSerializer.of(PowerTypeArgumentType::power));
        register(registry, Apoli.MODID + ":power_operation", PowerOperation.class, ConstantArgumentSerializer.of(PowerOperation::operation));
        register(registry , Apoli.MODID + ":power_holder", PowerHolderArgumentType.class, new EntityArgumentType.Serializer());
    }
}
