package io.github.apace100.apoli.power.type;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.LinkedList;
import java.util.List;

public class AttributePowerType extends PowerType {

    protected final List<AttributedEntityAttributeModifier> modifiers = new LinkedList<>();
    protected final boolean updateHealth;

    public AttributePowerType(Power power, LivingEntity entity, boolean updateHealth) {
        super(power, entity);
        this.updateHealth = updateHealth;
    }

    public AttributePowerType(Power power, LivingEntity entity, boolean updateHealth, RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier) {
        this(power, entity, updateHealth);
        addModifier(attribute, modifier);
    }

    public AttributePowerType addModifier(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier) {
        AttributedEntityAttributeModifier mod = new AttributedEntityAttributeModifier(attribute, modifier);
        this.modifiers.add(mod);
        return this;
    }

    public AttributePowerType addModifier(AttributedEntityAttributeModifier modifier) {
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
            .filter(mod -> entity.getAttributes().hasAttribute(mod.attribute()))
            .map(mod -> Pair.of(mod, entity.getAttributeInstance(mod.attribute())))
            .filter(pair -> pair.getSecond() != null && !pair.getSecond().hasModifier(pair.getFirst().modifier().id()))
            .forEach(pair -> pair.getSecond().addTemporaryModifier(pair.getFirst().modifier()));

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
            .filter(mod -> entity.getAttributes().hasAttribute(mod.attribute()))
            .map(mod -> Pair.of(mod, entity.getAttributeInstance(mod.attribute())))
            .filter(pair -> pair.getSecond() != null && pair.getSecond().hasModifier(pair.getFirst().modifier().id()))
            .forEach(pair -> pair.getSecond().removeModifier(pair.getFirst().modifier().id()));

        float currentMaxHealth = entity.getMaxHealth();
        if (updateHealth && currentMaxHealth != previousMaxHealth) {
            entity.setHealth(currentMaxHealth * previousHealthPercent);
        }

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("attribute"),
            new SerializableData()
                .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                .add("update_health", SerializableDataTypes.BOOLEAN, true),
            data -> (power, entity) -> {

                AttributePowerType attributePower = new AttributePowerType(
                    power,
                    entity,
                    data.get("update_health")
                );

                data.<AttributedEntityAttributeModifier>ifPresent("modifier", attributePower::addModifier);
                data.<List<AttributedEntityAttributeModifier>>ifPresent("modifiers", mods -> mods.forEach(attributePower::addModifier));

                return attributePower;

            }
        );
    }

}
