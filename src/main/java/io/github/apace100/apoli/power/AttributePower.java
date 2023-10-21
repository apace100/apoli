package io.github.apace100.apoli.power;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;

import java.util.LinkedList;
import java.util.List;

public class AttributePower extends Power {

    protected final List<AttributedEntityAttributeModifier> modifiers = new LinkedList<>();
    protected final boolean updateHealth;

    public AttributePower(PowerType<?> type, LivingEntity entity, boolean updateHealth) {
        super(type, entity);
        this.updateHealth = updateHealth;
    }

    public AttributePower(PowerType<?> type, LivingEntity entity, boolean updateHealth, EntityAttribute attribute, EntityAttributeModifier modifier) {
        this(type, entity, updateHealth);
        addModifier(attribute, modifier);
    }

    public AttributePower addModifier(EntityAttribute attribute, EntityAttributeModifier modifier) {
        AttributedEntityAttributeModifier mod = new AttributedEntityAttributeModifier(attribute, modifier);
        this.modifiers.add(mod);
        return this;
    }

    public AttributePower addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    @Override
    public void onAdded() {
        this.applyTempMods();
    }

    @Override
    public void onRemoved() {
        this.removeTempMods();
    }

    protected void applyTempMods() {

        if (entity.getWorld().isClient) {
            return;
        }

        float previousMaxHealth = entity.getMaxHealth();
        float previousHealthPercent = entity.getHealth() / previousMaxHealth;

        modifiers.stream()
            .filter(mod -> entity.getAttributes().hasAttribute(mod.getAttribute()))
            .map(mod -> Pair.of(mod, entity.getAttributeInstance(mod.getAttribute())))
            .filter(pair -> pair.getSecond() != null && !pair.getSecond().hasModifier(pair.getFirst().getModifier()))
            .forEach(pair -> pair.getSecond().addTemporaryModifier(pair.getFirst().getModifier()));

        float currentMaxHealth = entity.getMaxHealth();
        if (updateHealth && currentMaxHealth != previousMaxHealth) {
            entity.setHealth(currentMaxHealth * previousHealthPercent);
        }

    }

    protected void removeTempMods() {

        if (entity.getWorld().isClient) {
            return;
        }

        float previousMaxHealth = entity.getMaxHealth();
        float previousHealthPercent = entity.getHealth() / previousMaxHealth;

        modifiers.stream()
            .filter(mod -> entity.getAttributes().hasAttribute(mod.getAttribute()))
            .map(mod -> Pair.of(mod, entity.getAttributeInstance(mod.getAttribute())))
            .filter(pair -> pair.getSecond() != null && pair.getSecond().hasModifier(pair.getFirst().getModifier()))
            .forEach(pair -> pair.getSecond().removeModifier(pair.getFirst().getModifier().getId()));

        float currentMaxHealth = entity.getMaxHealth();
        if (updateHealth && currentMaxHealth != previousMaxHealth) {
            entity.setHealth(currentMaxHealth * previousHealthPercent);
        }

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("attribute"),
            new SerializableData()
                .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                .add("update_health", SerializableDataTypes.BOOLEAN, true),
            data -> (powerType, livingEntity) -> {

                AttributePower attributePower = new AttributePower(
                    powerType,
                    livingEntity,
                    data.get("update_health")
                );

                data.<AttributedEntityAttributeModifier>ifPresent("modifier", attributePower::addModifier);
                data.<List<AttributedEntityAttributeModifier>>ifPresent("modifiers", mods -> mods.forEach(attributePower::addModifier));

                return attributePower;

            }
        );
    }

}
