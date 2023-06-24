package io.github.apace100.apoli.component;

import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.integration.ModifyValueCallback;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.util.AttributeUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface PowerHolderComponent extends AutoSyncedComponent, ServerTickingComponent {

    ComponentKey<PowerHolderComponent> KEY = ComponentRegistry.getOrCreate(Apoli.identifier("powers"), PowerHolderComponent.class);

    void removePower(PowerType<?> powerType, Identifier source);

    int removeAllPowersFromSource(Identifier source);

    List<PowerType<?>> getPowersFromSource(Identifier source);

    boolean addPower(PowerType<?> powerType, Identifier source);

    boolean hasPower(PowerType<?> powerType);

    boolean hasPower(PowerType<?> powerType, Identifier source);

    <T extends Power> T getPower(PowerType<T> powerType);

    List<Power> getPowers();

    Set<PowerType<?>> getPowerTypes(boolean getSubPowerTypes);

    <T extends Power> List<T> getPowers(Class<T> powerClass);

    <T extends Power> List<T> getPowers(Class<T> powerClass, boolean includeInactive);

    List<Identifier> getSources(PowerType<?> powerType);

    void sync();

    static void sync(Entity entity) {
        KEY.sync(entity);
    }

    static void syncPower(Entity entity, PowerType<?> powerType) {
        if(entity == null || entity.getWorld().isClient) {
            return;
        }
        if(powerType instanceof PowerTypeReference) {
            powerType = ((PowerTypeReference<?>)powerType).getReferencedPowerType();
        }
        if(powerType == null) {
            return;
        }
        PowerType<?> finalPowerType = powerType;
        KEY.maybeGet(entity).ifPresent(phc -> {
            if(phc.hasPower(finalPowerType)) {
                Power power = phc.getPower(finalPowerType);
                NbtElement elem = power.toTag();
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeInt(entity.getId());
                buf.writeIdentifier(finalPowerType.getIdentifier());
                NbtCompound compound = new NbtCompound();
                compound.put("Data", elem);
                buf.writeNbt(compound);
                for(ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
                    ServerPlayNetworking.send(player, ModPackets.SYNC_POWER, buf);
                }
                if(entity instanceof ServerPlayerEntity self) {
                    ServerPlayNetworking.send(self, ModPackets.SYNC_POWER, buf);
                }
            }
        });
    }

    static <T extends Power> void withPower(Entity entity, Class<T> powerClass, Predicate<T> power, Consumer<T> with) {
        if(entity instanceof LivingEntity) {
            Optional<T> optional = KEY.get(entity).getPowers(powerClass).stream().filter(p -> power == null || power.test(p)).findAny();
            optional.ifPresent(with);
        }
    }

    static <T extends Power> void withPowers(Entity entity, Class<T> powerClass, Predicate<T> filter, Consumer<T> with) {
        if(entity instanceof LivingEntity) {
            KEY.get(entity).getPowers(powerClass).stream().filter(p -> filter == null || filter.test(p)).forEach(with);
        }
    }

    static <T extends Power> List<T> getPowers(Entity entity, Class<T> powerClass) {
        if(entity instanceof LivingEntity) {
            return KEY.get(entity).getPowers(powerClass);
        }
        return Lists.newArrayList();
    }

    static <T extends Power> boolean hasPower(Entity entity, Class<T> powerClass) {
        return hasPower(entity, powerClass, null);
    }

    static <T extends Power> boolean hasPower(Entity entity, Class<T> powerClass, Predicate<T> powerFilter) {
        if(entity instanceof LivingEntity) {
            return KEY.get(entity).getPowers().stream()
                .anyMatch(p -> powerClass.isAssignableFrom(p.getClass()) && p.isActive() &&
                    (powerFilter == null || powerFilter.test((T)p)));
        }
        return false;
    }

    static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue) {
        return (float)modify(entity, powerClass, (double)baseValue, null, null);
    }

    static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue, Predicate<T> powerFilter) {
        return (float)modify(entity, powerClass, (double)baseValue, powerFilter, null);
    }

    static <T extends ValueModifyingPower> float modify(Entity entity, Class<T> powerClass, float baseValue, Predicate<T> powerFilter, Consumer<T> powerAction) {
        return (float)modify(entity, powerClass, (double)baseValue, powerFilter, powerAction);
    }

    static <T extends ValueModifyingPower> double modify(Entity entity, Class<T> powerClass, double baseValue) {
        return modify(entity, powerClass, baseValue, null, null);
    }

    static <T extends ValueModifyingPower> double modify(Entity entity, Class<T> powerClass, double baseValue, Predicate<T> powerFilter, Consumer<T> powerAction) {
        if(entity instanceof LivingEntity living) {
            PowerHolderComponent powerHolder = PowerHolderComponent.KEY.get(entity);
            List<T> powers = powerHolder.getPowers(powerClass);
            List<Modifier> mps = powers.stream()
                .filter(p -> powerFilter == null || powerFilter.test(p))
                .flatMap(p -> p.getModifiers().stream()).collect(Collectors.toList());
            if(powerAction != null) {
                powers.stream().filter(p -> powerFilter == null || powerFilter.test(p)).forEach(powerAction);
            }

            powerHolder.getPowers(AttributeModifyTransferPower.class).stream()
                .filter(p -> p.doesApply(powerClass)).forEach(p -> p.addModifiers(mps));
            ModifyValueCallback.EVENT.invoker().collectModifiers(living, powerClass, baseValue, mps);
            return ModifierUtil.applyModifiers(entity, mps, baseValue);
        }
        return baseValue;
    }
}
