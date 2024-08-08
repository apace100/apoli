package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.function.Consumer;

public class ActiveCooldownPowerType extends CooldownPowerType implements Active {

    private final Consumer<Entity> entityAction;
    private final Key key;

    public ActiveCooldownPowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, HudRender hudRender, int cooldownDuration, Key key) {
        super(power, entity, cooldownDuration, hudRender);
        this.entityAction = entityAction;
        this.key = key;
    }

    @Override
    public void onUse() {

        if (canUse()) {
            this.entityAction.accept(this.entity);
            use();
        }

    }

    @Override
    public Key getKey() {
        return key;
    }

    public static PowerTypeFactory<ActiveCooldownPowerType> getActiveSelfFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("active_self"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION)
                .add("hud_render", ApoliDataTypes.HUD_RENDER)
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Key()),
            data -> (power, entity) -> new ActiveCooldownPowerType(power, entity,
                data.get("entity_action"),
                data.get("hud_render"),
                data.get("cooldown"),
                data.get("key")
            )
        ).allowCondition();
    }

    public static PowerTypeFactory<ActiveCooldownPowerType> getLaunchFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("launch"),
            new SerializableData()
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Key())
                .add("sound", SerializableDataTypes.SOUND_EVENT, null)
                .add("speed", SerializableDataTypes.FLOAT),
            data -> (power, entity) -> {

                Consumer<Entity> entityAction = e -> {

                    if (!(entity.getWorld() instanceof ServerWorld serverWorld)) {
                        return;
                    }

                    entity.addVelocity(0, data.getFloat("speed"), 0);
                    entity.velocityModified = true;

                    if (data.isPresent("sound")) {
                        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(), (SoundEvent) data.get("sound"), SoundCategory.NEUTRAL, 0.5F, 0.4F / (entity.getRandom().nextFloat()));
                    }

                    for (int i = 0; i < 4; i++) {
                        serverWorld.spawnParticles(ParticleTypes.CLOUD, entity.getX(), entity.getRandomBodyY(), entity.getZ(), 8, entity.getRandom().nextGaussian(), 0.0D, entity.getRandom().nextGaussian(), 0.5);
                    }

                };

                return new ActiveCooldownPowerType(power, entity,
                    entityAction,
                    data.get("hud_render"),
                    data.get("cooldown"),
                    data.get("key")
                );

            }
        ).allowCondition();
    }

}
