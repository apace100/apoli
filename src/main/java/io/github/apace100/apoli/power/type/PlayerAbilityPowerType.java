package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class PlayerAbilityPowerType extends PowerType {

    private final PlayerAbility ability;
    private final AbilitySource source;

    private boolean shouldRefresh;

    public PlayerAbilityPowerType(Power power, LivingEntity entity, PlayerAbility playerAbility) {
        super(power, entity);
        this.ability = playerAbility;
        this.source = Pal.getAbilitySource(this.getPowerId());
    }

    @Override
    public boolean shouldTick() {
        return entity instanceof PlayerEntity;
    }

    @Override
    public boolean shouldTickWhenInactive() {
        return this.shouldTick();
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

        if (!(entity instanceof PlayerEntity player)) {
            return;
        }

        if (shouldRefresh) {
            ability.getTracker(player).refresh(true);
            shouldRefresh = false;
        }

        else {

            boolean active = this.isActive();
            boolean hasAbility = this.hasAbility();

            if (active && !hasAbility) {
                this.grantAbility();
            }

            else if (!active && hasAbility) {
                this.revokeAbility();
            }

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

    public static PowerTypeFactory<?> createFactory(Identifier id, PlayerAbility ability) {
        return new PowerTypeFactory<>(id,
            new SerializableData(),
            data -> (power, entity) -> new PlayerAbilityPowerType(power, entity, ability)
        ).allowCondition();
    }

}
