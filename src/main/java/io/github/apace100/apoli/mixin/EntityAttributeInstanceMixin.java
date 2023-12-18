package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.OwnableAttributeInstance;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyAttributePower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.stream.Stream;

@Mixin(EntityAttributeInstance.class)
public abstract class EntityAttributeInstanceMixin implements OwnableAttributeInstance {

    @Shadow
    @Final
    private EntityAttribute type;

    @Shadow
    public abstract Set<EntityAttributeModifier> getModifiers();

    @Shadow
    public abstract double getBaseValue();

    @Unique
    @Nullable
    Entity apoli$owner;

    @Override
    public void apoli$setOwner(Entity owner) {
        apoli$owner = owner;
    }

    @Override
    @Nullable
    public Entity apoli$getOwner() {
        return apoli$owner;
    }

    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double apoli$modifyAttribute(double original) {

        if (!PowerHolderComponent.hasPower(this.apoli$getOwner(), ModifyAttributePower.class)) {
            return original;
        }

        Stream<Modifier> vanillaModifiers = this.getModifiers()
            .stream()
            .map(ModifierUtil::fromAttributeModifier);
        Stream<Modifier> powerModifiers = PowerHolderComponent.getPowers(this.apoli$getOwner(), ModifyAttributePower.class)
            .stream()
            .filter(p -> p.getAttribute() == type)
            .flatMap(p -> p.getModifiers().stream());

        return ModifierUtil.applyModifiers(this.apoli$getOwner(), Stream.concat(vanillaModifiers, powerModifiers).toList(), this.getBaseValue());

    }

}
