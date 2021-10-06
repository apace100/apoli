package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerAbilityPower extends Power {

    private final PlayerAbility ability;

    public PlayerAbilityPower(PowerType<?> type, LivingEntity entity, PlayerAbility playerAbility) {
        super(type, entity);
        this.ability = playerAbility;
        if(entity instanceof PlayerEntity) {
            this.setTicking(true);
        }
    }

    @Override
    public void tick() {
        if(!entity.world.isClient) {
            boolean isActive = isActive();
            boolean hasAbility = hasAbility();
            if(isActive && !hasAbility) {
                grantAbility();
            } else if(!isActive && hasAbility) {
                revokeAbility();
            }
        }
    }

    @Override
    public void onGained() {
        if(!entity.world.isClient && isActive() && !hasAbility()) {
            grantAbility();
        }
    }

    @Override
    public void onLost() {
        if(!entity.world.isClient && hasAbility()) {
            revokeAbility();
        }
    }

    public boolean hasAbility() {
        return Apoli.POWER_SOURCE.grants((PlayerEntity)entity, ability);
    }

    public void grantAbility() {
        Apoli.POWER_SOURCE.grantTo((PlayerEntity)entity, ability);
    }

    public void revokeAbility() {
        Apoli.POWER_SOURCE.revokeFrom((PlayerEntity)entity, ability);
    }

    public static PowerFactory createAbilityFactory(Identifier identifier, PlayerAbility ability) {
        return new PowerFactory<>(identifier,
            new SerializableData(),
            data ->
                (type, player) -> new PlayerAbilityPower(type, player, ability))
            .allowCondition();
    }
}
