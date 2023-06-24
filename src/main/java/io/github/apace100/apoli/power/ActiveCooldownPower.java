package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.function.Consumer;

public class ActiveCooldownPower extends CooldownPower implements Active {

    private final Consumer<Entity> activeFunction;

    public ActiveCooldownPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Consumer<Entity> activeFunction) {
        super(type, entity, cooldownDuration, hudRender);
        this.activeFunction = activeFunction;
    }

    @Override
    public void onUse() {
        if(canUse()) {
            this.activeFunction.accept(this.entity);
            use();
        }
    }

    private Key key;

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public void setKey(Key key) {
        this.key = key;
    }

    public static PowerFactory createActiveSelfFactory() {
        return new PowerFactory<>(Apoli.identifier("active_self"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION)
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data ->
                (type, player) -> {
                    ActiveCooldownPower power = new ActiveCooldownPower(type, player, data.getInt("cooldown"), (HudRender)data.get("hud_render"),
                        (ActionFactory<Entity>.Instance)data.get("entity_action"));
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                })
            .allowCondition();
    }

    public static PowerFactory createLaunchFactory() {
        return new PowerFactory<>(Apoli.identifier("launch"),
            new SerializableData()
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("speed", SerializableDataTypes.FLOAT)
                .add("sound", SerializableDataTypes.SOUND_EVENT, null)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Active.Key()),
            data -> {
                SoundEvent soundEvent = (SoundEvent)data.get("sound");
                return (type, player) -> {
                    ActiveCooldownPower power = new ActiveCooldownPower(type, player,
                        data.getInt("cooldown"),
                        (HudRender) data.get("hud_render"),
                        e -> {
                            if (!e.getWorld().isClient && e instanceof PlayerEntity) {
                                PlayerEntity p = (PlayerEntity) e;
                                p.addVelocity(0, data.getFloat("speed"), 0);
                                p.velocityModified = true;
                                if (soundEvent != null) {
                                    p.getWorld().playSound((PlayerEntity) null, p.getX(), p.getY(), p.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.4F / (p.getRandom().nextFloat() * 0.4F + 0.8F));
                                }
                                for (int i = 0; i < 4; ++i) {
                                    ((ServerWorld) p.getWorld()).spawnParticles(ParticleTypes.CLOUD, p.getX(), p.getRandomBodyY(), p.getZ(), 8, p.getRandom().nextGaussian(), 0.0D, p.getRandom().nextGaussian(), 0.5);
                                }
                            }
                        });
                    power.setKey((Active.Key)data.get("key"));
                    return power;
                };
            }).allowCondition();
    }
}
