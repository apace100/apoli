package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Deprecated
public class LaunchPowerType extends ActiveCooldownPowerType {

	public static final TypedDataObjectFactory<LaunchPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
		new SerializableData()
			.add("sound", SerializableDataTypes.SOUND_EVENT.optional(), Optional.empty())
			.add("speed", SerializableDataTypes.FLOAT)
			.add("hud_render", HudRender.DATA_TYPE, HudRender.DONT_RENDER)
			.add("cooldown", SerializableDataTypes.INT, 1)
			.add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Key()),
		(data, condition) -> new LaunchPowerType(
			data.get("sound"),
			data.get("speed"),
			data.get("hud_render"),
			data.get("cooldown"),
			data.get("key"),
			condition
		),
		(powerType, serializableData) -> serializableData.instance()
			.set("sound", powerType.sound)
			.set("speed", powerType.speed)
			.set("hud_render", powerType.getRenderSettings())
			.set("cooldown", powerType.getCooldown())
			.set("key", powerType.getKey())
	);

	private final Optional<SoundEvent> sound;
	private final float speed;

	public LaunchPowerType(Optional<SoundEvent> sound, float speed, HudRender hudRender, int cooldownDuration, Key key, Optional<EntityCondition> condition) {
		super(hudRender, cooldownDuration, key, condition);
		this.sound = sound;
		this.speed = speed;
	}

	@Override
	public @NotNull PowerConfiguration<?> getConfig() {
		return PowerTypes.LAUNCH;
	}

	@Override
	public void onUse() {

		LivingEntity holder = getHolder();
		if (!(holder.getWorld() instanceof ServerWorld serverWorld)) {
			return;
		}

		super.onUse();

		holder.addVelocity(0, speed, 0);
		holder.velocityModified = true;

		sound.ifPresent(soundEvent -> serverWorld.playSound(null, holder.getX(), holder.getY(), holder.getZ(), soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.4F / holder.getRandom().nextFloat()));

		for (int i = 0; i < 4; i++) {
			serverWorld.spawnParticles(ParticleTypes.CLOUD, holder.getX(), holder.getRandomBodyY(), holder.getZ(), 8, holder.getRandom().nextGaussian(), 0.0D, holder.getRandom().nextGaussian(), 0.5);
		}

	}

}
