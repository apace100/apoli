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
		getResultReference()
			.map(Power::getPowerType)
			.flatMap(condition())
			.getOrThrow();
	}

	@Nullable
	public PowerType getPowerTypeFrom(Entity entity) {
		return getOptionalReference()
			.flatMap(power -> Optional.ofNullable(power.getPowerTypeFrom(entity)))
			.orElse(null);
	}

	public DataResult<Power> getResultReference() {
		return PowerManager.getResult(id());
	}

	public Optional<Power> getOptionalReference() {
		return PowerManager.getOptional(id());
	}

	public Power getStrictReference() {
		return PowerManager.get(id());
	}

	@Nullable
	public Power getReference() {
		return PowerManager.getNullable(id());
	}

	public boolean isActive(Entity entity) {
		return getOptionalReference()
			.map(power -> power.isActive(entity))
			.orElse(false);
	}

}
