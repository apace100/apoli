package io.github.apace100.apoli.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.access.EndRespawningEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnItemUsePower;
import io.github.apace100.apoli.power.KeepInventoryPower;
import io.github.apace100.apoli.power.ModifyPlayerSpawnPower;
import io.github.apace100.apoli.power.PreventSleepPower;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ScreenHandlerListener, EndRespawningEntity {

    @Shadow
    private RegistryKey<World> spawnPointDimension;

    @Shadow
    private BlockPos spawnPointPosition;

    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    @Shadow
    public boolean notInAnyWorld;

    @Shadow private boolean spawnForced;

    @Shadow public abstract void setSpawnPoint(RegistryKey<World> dimension, @Nullable BlockPos pos, float angle, boolean forced, boolean sendMessage);

    @Shadow public abstract void sendMessage(Text message);

    @Shadow public abstract boolean shouldDamagePlayer(PlayerEntity player);

    @Inject(method = "trySleep", at = @At(value = "INVOKE",target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V"), cancellable = true)
    public void preventSleep(BlockPos pos, CallbackInfoReturnable<Either<SleepFailureReason, Unit>> info) {

        LinkedList<PreventSleepPower> preventSleepPowers = PowerHolderComponent.getPowers(this, PreventSleepPower.class)
            .stream()
            .filter(p -> p.doesPrevent(this.getWorld(), pos))
            .collect(Collectors.toCollection(LinkedList::new));

        if (preventSleepPowers.isEmpty()) {
            return;
        }

        preventSleepPowers.sort(Collections.reverseOrder(PreventSleepPower::compareTo));
        PreventSleepPower preventSleepPower = preventSleepPowers
            .getFirst();

        if (preventSleepPowers.stream().allMatch(PreventSleepPower::doesAllowSpawnPoint)) {
            this.setSpawnPoint(this.getWorld().getRegistryKey(), pos, this.getYaw(), false, true);
        }

        info.setReturnValue(Either.left(SleepFailureReason.OTHER_PROBLEM));
        this.sendMessage(preventSleepPower.getMessage(), true);

    }

    @Inject(at = @At("HEAD"), method = "getSpawnPointDimension", cancellable = true)
    private void modifySpawnPointDimension(CallbackInfoReturnable<RegistryKey<World>> info) {
        if (!this.origins_isEndRespawning && (spawnPointPosition == null || hasObstructedSpawn()) && PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).size() > 0) {
            ModifyPlayerSpawnPower power = PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).get(0);
            info.setReturnValue(power.dimension);
        }
    }

    @Inject(at = @At("HEAD"), method = "getSpawnPointPosition", cancellable = true)
    private void modifyPlayerSpawnPosition(CallbackInfoReturnable<BlockPos> info) {
        if(!this.origins_isEndRespawning && PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).size() > 0) {
            if(spawnPointPosition == null) {
                info.setReturnValue(findPlayerSpawn());
            } else if(hasObstructedSpawn()) {
                networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0F));
                info.setReturnValue(findPlayerSpawn());
            }
        }
    }


    @Inject(at = @At("HEAD"), method = "isSpawnForced", cancellable = true)
    private void modifySpawnPointSet(CallbackInfoReturnable<Boolean> info) {
        if(!this.origins_isEndRespawning && (spawnPointPosition == null || hasObstructedSpawn()) && PowerHolderComponent.hasPower(this, ModifyPlayerSpawnPower.class)) {
            info.setReturnValue(true);
        }
    }

    @Inject(method = "copyFrom", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/network/ServerPlayerEntity;enchantmentTableSeed:I"))
    private void copyInventoryWhenKeeping(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if(PowerHolderComponent.hasPower(oldPlayer, KeepInventoryPower.class)) {
            this.getInventory().clone(oldPlayer.getInventory());
        }
    }

    private boolean hasObstructedSpawn() {
        ServerWorld world = server.getWorld(spawnPointDimension);
        if(spawnPointPosition != null && world != null) {
            Optional optional = PlayerEntity.findRespawnPosition(world, spawnPointPosition, 0F, spawnForced, true);
            return !optional.isPresent();
        }
        return false;
    }

    private BlockPos findPlayerSpawn() {
        ModifyPlayerSpawnPower power = PowerHolderComponent.getPowers(this, ModifyPlayerSpawnPower.class).get(0);
        Pair<ServerWorld, BlockPos> spawn = power.getSpawn(true);
        if(spawn != null) {
            return spawn.getRight();
        }
        return null;
    }

    @Unique
    private ItemStack apoli$stackBeforeDrop;

    @Inject(method = "dropSelectedItem", at = @At("HEAD"))
    private void cacheItemStackBeforeDropping(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        apoli$stackBeforeDrop = this.getInventory().getMainHandStack().copy();
    }

    @Inject(method = "dropSelectedItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;getSlotIndex(Lnet/minecraft/inventory/Inventory;I)Ljava/util/OptionalInt;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkItemUsageStopping(boolean entireStack, CallbackInfoReturnable<Boolean> cir, PlayerInventory playerInventory, ItemStack itemStack) {
        if(this.isUsingItem() && !ItemStack.areItemsEqual(apoli$stackBeforeDrop, this.getInventory().getMainHandStack())) {
            ActionOnItemUsePower.executeActions(this, itemStack, apoli$stackBeforeDrop,
                    ActionOnItemUsePower.TriggerType.STOP, ActionOnItemUsePower.PriorityPhase.ALL);
        }
    }

    @Unique
    private boolean origins_isEndRespawning;

    @Override
    public void setEndRespawning(boolean endSpawn) {
        this.origins_isEndRespawning = endSpawn;
    }

    @Override
    public boolean isEndRespawning() {
        return this.origins_isEndRespawning;
    }

    @Override
    public boolean hasRealRespawnPoint() {
        return spawnPointPosition != null && !hasObstructedSpawn();
    }
}
