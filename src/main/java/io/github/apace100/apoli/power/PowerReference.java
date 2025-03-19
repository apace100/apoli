package io.github.apace100.apoli.power;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.util.PowerUtil;
import io.github.apace100.calio.util.Validatable;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public record PowerReference(Identifier id, Function<PowerType, DataResult<PowerType>> condition) implements Validatable {

	public static PowerReference of(Identifier id) {
		return new PowerReference(id, DataResult::success);
	}

	public static PowerReference resource(Identifier id) {
		return new PowerReference(id, PowerUtil::validateResource);
	}

	@Override
	public void validate() throws Exception {
		getResultPower()
			.map(Power::getType)
			.flatMap(condition())
			.getOrThrow();
	}

	public Optional<PowerType> getOptionalPowerType(Entity entity) {
		return getOptionalPower().flatMap(power -> PowerUtil.getOptionalPowerType(power, entity));
	}

	@Nullable
	public PowerType getNullablePowerType(Entity entity) {
		return getOptionalPowerType(entity).orElse(null);
	}

	public DataResult<Power> getResultPower() {
		return PowerManager.getResult(id());
	}

	public Optional<Power> getOptionalPower() {
		return PowerManager.getOptional(id());
	}

	@Nullable
	public Power getNullablePower() {
		return PowerManager.getNullable(id());
	}

	public Power getPower() {
		return PowerManager.get(id());
	}

	public boolean isActive(Entity entity) {
		return getOptionalPower()
			.map(power -> power.isActive(entity))
			.orElse(false);
	}

}
