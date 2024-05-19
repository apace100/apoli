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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(EntityAttributeInstance.class)
public abstract class EntityAttributeInstanceMixin implements OwnableAttributeInstance {

    @Shadow
    public abstract Set<EntityAttributeModifier> getModifiers();

    @Shadow
    public abstract EntityAttribute getAttribute();

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
    public Entity apoli$getOwner() {
        return apoli$owner;
    }

    /**
     *  TODO: Optimize this impl. by using a modifier cache, injecting into {@link EntityAttributeInstance#computeValue()}, and calling
     *        {@link EntityAttributeInstance#onUpdate()} if the modifier cache is no longer up-to-date - eggohito
     */
    @ModifyReturnValue(method = "getValue", at = @At("RETURN"))
    private double apoli$modifyAttribute(double original) {

        List<Modifier> powerModifiers = PowerHolderComponent.getPowers(this.apoli$getOwner(), ModifyAttributePower.class)
            .stream()
            .filter(p -> p.getAttribute() == this.getAttribute())
            .flatMap(p -> p.getModifiers().stream())
            .toList();

        if (powerModifiers.isEmpty()) {
            return original;
        }

        List<Modifier> vanillaModifiers = this.getModifiers()
            .stream()
            .map(ModifierUtil::fromAttributeModifier)
            .toList();

        return ModifierUtil.applyModifiers(this.apoli$getOwner(), Stream.concat(powerModifiers.stream(), vanillaModifiers.stream()).toList(), this.getBaseValue());

    }

}
