package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.access.OwnableAttributeContainer;
import io.github.apace100.apoli.access.OwnableAttributeInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DefaultAttributeContainer.class)
public abstract class DefaultAttributeContainerMixin implements OwnableAttributeContainer {

    @Unique
    private Entity apoli$owner;

    @Override
    @Nullable
    public Entity apoli$getOwner() {
        return apoli$owner;
    }

    @Override
    public void apoli$setOwner(Entity owner) {
        this.apoli$owner = owner;
    }

    @ModifyExpressionValue(method = "getValue", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/attribute/DefaultAttributeContainer;require(Lnet/minecraft/entity/attribute/EntityAttribute;)Lnet/minecraft/entity/attribute/EntityAttributeInstance;"))
    private EntityAttributeInstance apoli$setInstanceOwner(EntityAttributeInstance original) {
        ((OwnableAttributeInstance) original).apoli$setOwner(this.apoli$getOwner());
        return original;
    }

}
