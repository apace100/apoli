package io.github.apace100.apoli.util;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.SubPower;
import io.github.apace100.apoli.power.type.CooldownPowerType;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.power.type.VariableIntPowerType;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class PowerUtil {

	public static DataResult<PowerType> validateResource(@Nullable PowerType powerType) {
		return switch (powerType) {
			case VariableIntPowerType varInt ->
				DataResult.success(varInt);
			case CooldownPowerType cooldown ->
				DataResult.success(cooldown);
			case null ->
				DataResult.error(() -> "Power type cannot be null!");
			default -> {

				Power power = powerType.getPower();

				Identifier powerTypeId = power.getFactoryInstance().getSerializerId();
				StringBuilder powerString = new StringBuilder();

				if (power instanceof SubPower subPower) {
					powerString
						.append("Sub-power \"").append(subPower.getSubName()).append("\"")
						.append(" of power \"").append(subPower.getSuperPowerId()).append("\"");
				}

				else {
					powerString.append("Power \"").append(power.getId()).append("\"");
				}

				DataResult<PowerType> result = DataResult.error(() -> powerString
					.append(" is using power type \"").append(powerTypeId).append("\"")
					.append(", which is not considered a resource!")
					.toString());

				yield result.setPartial(powerType);

			}
		};
	}

	public static int getResourceValue(PowerType powerType) {
		return switch (powerType) {
			case VariableIntPowerType varInt ->
				varInt.getValue();
			case CooldownPowerType cooldown ->
				cooldown.getRemainingTicks();
			case null, default ->
				0;
		};
	}

	public static boolean modifyResourceValue(PowerType powerType, Collection<Modifier> modifiers) {

		int oldValue = getResourceValue(powerType);
		int newValue = 0;

		switch (powerType) {
			case VariableIntPowerType varInt -> {
				varInt.setValue((int) ModifierUtil.applyModifiers(powerType.getHolder(), modifiers, oldValue));
				newValue = varInt.getValue();
			}
			case CooldownPowerType cooldown -> {

				int modified = Math.max((int) ModifierUtil.applyModifiers(powerType.getHolder(), modifiers, oldValue), 0);
				cooldown.modify(modified - oldValue);

				newValue = cooldown.getRemainingTicks();

			}
			case null, default -> {

			}
		}

		return oldValue != newValue;

	}

	public static boolean changeResourceValue(PowerType powerType, int value) {

		int oldValue = getResourceValue(powerType);
		int newValue = 0;

		switch (powerType) {
			case VariableIntPowerType varInt -> {
				varInt.setValue(oldValue + value);
				newValue = varInt.getValue();
			}
			case CooldownPowerType cooldown -> {
				cooldown.modify(value);
				newValue = cooldown.getRemainingTicks();
			}
			case null, default -> {

			}
		}

		return oldValue != newValue;

	}

	public static boolean setResourceValue(PowerType powerType, int value) {

		int oldValue = getResourceValue(powerType);
		int newValue = 0;

		switch (powerType) {
			case VariableIntPowerType varInt -> {
				varInt.setValue(value);
				newValue = varInt.getValue();
			}
			case CooldownPowerType cooldown -> {
				cooldown.setCooldown(value);
				newValue = cooldown.getRemainingTicks();
			}
			case null, default -> {

			}
		}

		return oldValue != newValue;

	}

}
