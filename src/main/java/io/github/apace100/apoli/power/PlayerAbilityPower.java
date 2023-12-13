package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class PlayerAbilityPower extends Power {

    private final PlayerAbility ability;
    private final AbilitySource source;

    private boolean shouldRefresh;

    public PlayerAbilityPower(PowerType<?> type, LivingEntity entity, PlayerAbility playerAbility) {
        super(type, entity);
        this.ability = playerAbility;
        this.source = Pal.getAbilitySource(type.getIdentifier());
        if (entity instanceof PlayerEntity) {
            this.setTicking(true);
        }
    }

    @Override
    public NbtElement toTag() {

        NbtCompound rootNbt = new NbtCompound();
        rootNbt.putBoolean("ShouldRefresh", shouldRefresh);

        return rootNbt;

    }

    @Override
    public void fromTag(NbtElement tag) {
        if (tag instanceof NbtCompound rootNbt) {
            this.shouldRefresh = rootNbt.getBoolean("ShouldRefresh");
        }
    }

    @Override
    public void tick() {

        if (entity.getWorld().isClient || !(entity instanceof PlayerEntity playerEntity)) {
            return;
        }

        if (shouldRefresh) {

            ability.getTracker(playerEntity).refresh(true);
            shouldRefresh = false;

            return;

        }

        boolean active = this.isActive();
        boolean hasAbility = this.hasAbility();

        if (active && !hasAbility) {
            grantAbility();
        } else if (!active && hasAbility) {
            revokeAbility();
        }

    }

    @Override
    public void onAdded() {
        if (!entity.getWorld().isClient && entity instanceof PlayerEntity player && Apoli.LEGACY_POWER_SOURCE.grants(player, ability)) {
            Apoli.LEGACY_POWER_SOURCE.revokeFrom(player, ability);
        }
    }

    @Override
    public void onRemoved() {
        //  Indicate that the ability should be refreshed upon the entity being removed from a world
        this.shouldRefresh = true;
    }

    @Override
    public void onGained() {
        if (!entity.getWorld().isClient && this.isActive()) {
            grantAbility();
        }
    }

    @Override
    public void onLost() {
        if (!entity.getWorld().isClient) {
            revokeAbility();
        }
    }

    public boolean hasAbility() {
        return entity instanceof PlayerEntity playerEntity && source.grants(playerEntity, ability);
    }

    public void grantAbility() {
        if (entity instanceof PlayerEntity playerEntity) {
            source.grantTo(playerEntity, ability);
        }
    }

    public void revokeAbility() {
        if (entity instanceof PlayerEntity playerEntity) {
            source.revokeFrom(playerEntity, ability);
        }
    }

    public static PowerFactory createAbilityFactory(Identifier identifier, PlayerAbility ability) {
        return new PowerFactory<>(
            identifier,
            new SerializableData(),
            data -> (type, player) -> new PlayerAbilityPower(
                type,
                player,
                ability
            )
        ).allowCondition();
    }

}
