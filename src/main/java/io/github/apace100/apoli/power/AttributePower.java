package io.github.apace100.apoli.power;

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

    private final List<AttributedEntityAttributeModifier> modifiers = new LinkedList<AttributedEntityAttributeModifier>();
    private final boolean updateHealth;

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
        if(!entity.getWorld().isClient) {
            float previousMaxHealth = entity.getMaxHealth();
            float previousHealthPercent = entity.getHealth() / previousMaxHealth;
            modifiers.forEach(mod -> {
                if(entity.getAttributes().hasAttribute(mod.getAttribute())) {
                    entity.getAttributeInstance(mod.getAttribute()).addTemporaryModifier(mod.getModifier());
                }
            });
            float afterMaxHealth = entity.getMaxHealth();
            if(updateHealth && afterMaxHealth != previousMaxHealth) {
                entity.setHealth(afterMaxHealth * previousHealthPercent);
            }
        }
    }

    @Override
    public void onRemoved() {
        if(!entity.getWorld().isClient) {
            float previousMaxHealth = entity.getMaxHealth();
            float previousHealthPercent = entity.getHealth() / previousMaxHealth;
            modifiers.forEach(mod -> {
                if (entity.getAttributes().hasAttribute(mod.getAttribute())) {
                    entity.getAttributeInstance(mod.getAttribute()).removeModifier(mod.getModifier());
                }
            });
            float afterMaxHealth = entity.getMaxHealth();
            if(updateHealth && afterMaxHealth != previousMaxHealth) {
                entity.setHealth(afterMaxHealth * previousHealthPercent);
            }
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("attribute"),
            new SerializableData()
                .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                .add("update_health", SerializableDataTypes.BOOLEAN, true),
            data ->
                (type, player) -> {
                    AttributePower ap = new AttributePower(type, player, data.getBoolean("update_health"));
                    if(data.isPresent("modifier")) {
                        ap.addModifier((AttributedEntityAttributeModifier)data.get("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        List<AttributedEntityAttributeModifier> modifierList = (List<AttributedEntityAttributeModifier>)data.get("modifiers");
                        modifierList.forEach(ap::addModifier);
                    }
                    return ap;
                });
    }
}
