package io.github.apace100.apoli.util;

import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.data.DamageSourceDescription;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public final class MiscUtil {

    public static BlockState getInWallBlockState(LivingEntity playerEntity) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(int i = 0; i < 8; ++i) {
            double d = playerEntity.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            double e = playerEntity.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = playerEntity.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * playerEntity.getWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = playerEntity.world.getBlockState(mutable);
            if (blockState.getRenderType() != BlockRenderType.INVISIBLE && blockState.shouldBlockVision(playerEntity.world, mutable)) {
                return blockState;
            }
        }

        return null;
    }

    public static <T> Predicate<T> combineOr(Predicate<T> a, Predicate<T> b) {
        if(a == null) {
            return b;
        }
        if(b == null) {
            return a;
        }
        return a.or(b);
    }

    public static <T> Predicate<T> combineAnd(Predicate<T> a, Predicate<T> b) {
        if(a == null) {
            return b;
        }
        if(b == null) {
            return a;
        }
        return a.and(b);
    }

    public static DamageSource createDamageSource(DamageSources damageSources,
                                                  @Nullable DamageSourceDescription damageSourceDescription,
                                                  @Nullable RegistryKey<DamageType> damageType) {
        if(damageSourceDescription == null && damageType == null) {
            throw new JsonSyntaxException("Either a legacy damage source or an ID of a damage type must be specified");
        }
        return damageSourceDescription == null ? damageSources.create(damageType) : damageSourceDescription.create(damageSources);
    }

    public static DamageSource createDamageSource(DamageSources damageSources,
                                                  @Nullable DamageSourceDescription damageSourceDescription,
                                                  @Nullable RegistryKey<DamageType> damageType, Entity attacker) {
        if(damageSourceDescription == null && damageType == null) {
            throw new JsonSyntaxException("Either a legacy damage source or an ID of a damage type must be specified");
        }
        return damageSourceDescription == null ? damageSources.create(damageType, attacker) : damageSourceDescription.create(damageSources, attacker);
    }

    public static DamageSource createDamageSource(DamageSources damageSources,
                                                  @Nullable DamageSourceDescription damageSourceDescription,
                                                  @Nullable RegistryKey<DamageType> damageType, Entity source, Entity attacker) {
        if(damageSourceDescription == null && damageType == null) {
            throw new JsonSyntaxException("Either a legacy damage source or an ID of a damage type must be specified");
        }
        return damageSourceDescription == null ? damageSources.create(damageType, source, attacker) : damageSourceDescription.create(damageSources, source, attacker);
    }
}
