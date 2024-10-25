package io.github.apace100.apoli.power.type;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import net.minecraft.entity.LivingEntity;

import java.util.List;

public interface AttributeModifying {

	List<AttributedEntityAttributeModifier> attributedModifiers();

	boolean shouldUpdateHealth();

	default void applyTempModifiers(LivingEntity entity) {

		if (entity.getWorld().isClient()) {
			return;
		}

		float previousMaxHealth = entity.getMaxHealth();
		float previousMaxHealthPercent = entity.getHealth() / previousMaxHealth;

		attributedModifiers().stream()
			.filter(mod -> entity.getAttributes().hasAttribute(mod.attribute()))
			.map(mod -> Pair.of(mod, entity.getAttributeInstance(mod.attribute())))
			.filter(pair -> pair.getSecond() != null && !pair.getSecond().hasModifier(pair.getFirst().modifier().id()))
			.forEach(pair -> pair.getSecond().addTemporaryModifier(pair.getFirst().modifier()));

		float currentMaxHealth = entity.getMaxHealth();

		if (shouldUpdateHealth() && currentMaxHealth != previousMaxHealth) {
			entity.setHealth(currentMaxHealth * previousMaxHealthPercent);
		}

	}

	default void removeTempModifiers(LivingEntity entity) {

		if (entity.getWorld().isClient()) {
			return;
		}

		float previousMaxHealth = entity.getMaxHealth();
		float previousMaxHealthPercent = entity.getHealth() / previousMaxHealth;

		attributedModifiers().stream()
			.filter(mod -> entity.getAttributes().hasAttribute(mod.attribute()))
			.map(mod -> Pair.of(mod, entity.getAttributeInstance(mod.attribute())))
			.filter(pair -> pair.getSecond() != null && pair.getSecond().hasModifier(pair.getFirst().modifier().id()))
			.forEach(pair -> pair.getSecond().removeModifier(pair.getFirst().modifier()));

		float currentMaxHealth = entity.getMaxHealth();

		if (shouldUpdateHealth() && currentMaxHealth != previousMaxHealth) {
			entity.setHealth(currentMaxHealth * previousMaxHealthPercent);
		}

	}

}
