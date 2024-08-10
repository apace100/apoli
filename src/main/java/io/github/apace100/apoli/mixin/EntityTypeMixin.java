package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.apace100.apoli.access.EntityLinkedType;
import io.github.apace100.apoli.power.ModifyTypeTagPowerType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements EntityLinkedType {

    @Unique
    private Entity apoli$currentEntity;

    @Override
    public Entity apoli$getEntity() {
        return apoli$currentEntity;
    }

    @Override
    public void apoli$setEntity(Entity entity) {
        this.apoli$currentEntity = entity;
    }

    @ModifyReturnValue(method = "isIn(Lnet/minecraft/registry/tag/TagKey;)Z", at = @At("RETURN"))
    private boolean apoli$modifyTypeTag(boolean original, TagKey<EntityType<?>> tag) {
        return ModifyTypeTagPowerType.doesApply(this.apoli$getEntity(), tag, original);
    }

}
