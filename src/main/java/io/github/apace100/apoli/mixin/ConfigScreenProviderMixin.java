package io.github.apace100.apoli.mixin;

import io.github.apace100.apoli.util.ApoliConfigClient;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;

@Pseudo
@Mixin(ConfigScreenProvider.class)
public class ConfigScreenProviderMixin {

    @Redirect(method = "get", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getDeclaredFields()[Ljava/lang/reflect/Field;"))
    private Field[] getEvenSuperFields(Class aClass) {
        if(aClass == ApoliConfigClient.class) {
            return aClass.getFields();
        } else {
            return aClass.getDeclaredFields();
        }
    }
}
