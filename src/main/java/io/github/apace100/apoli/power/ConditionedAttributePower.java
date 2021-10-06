package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;

import java.util.LinkedList;
import java.util.List;

public class ConditionedAttributePower extends Power {

    private final List<AttributedEntityAttributeModifier> modifiers = new LinkedList<AttributedEntityAttributeModifier>();
    private final int tickRate;
    private final boolean updateHealth;

    public ConditionedAttributePower(PowerType<?> type, LivingEntity entity, int tickRate, boolean updateHealth) {
        super(type, entity);
        this.setTicking(true);
        this.tickRate = tickRate;
        this.updateHealth = updateHealth;
    }

    @Override
    public void tick() {
        if(entity.age % tickRate == 0) {
            if(this.isActive()) {
                addMods();
            } else {
                removeMods();
            }
        }
    }

    @Override
    public void onRemoved() {
        removeMods();
    }

    public ConditionedAttributePower addModifier(AttributedEntityAttributeModifier modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public void addMods() {
        float previousMaxHealth = entity.getMaxHealth();
        float previousHealthPercent = entity.getHealth() / previousMaxHealth;
        modifiers.forEach(mod -> {
            if(entity.getAttributes().hasAttribute(mod.getAttribute())) {
                EntityAttributeInstance instance = entity.getAttributeInstance(mod.getAttribute());
                if(instance != null) {
                    if(!instance.hasModifier(mod.getModifier())) {
                        instance.addTemporaryModifier(mod.getModifier());
                    }
                }
            }
        });
        float afterMaxHealth = entity.getMaxHealth();
        if(updateHealth && afterMaxHealth != previousMaxHealth) {
            entity.setHealth(afterMaxHealth * previousHealthPercent);
        }
    }

    public void removeMods() {
        float previousMaxHealth = entity.getMaxHealth();
        float previousHealthPercent = entity.getHealth() / previousMaxHealth;
        modifiers.forEach(mod -> {
            if (entity.getAttributes().hasAttribute(mod.getAttribute())) {
                EntityAttributeInstance instance = entity.getAttributeInstance(mod.getAttribute());
                if(instance != null) {
                    if(instance.hasModifier(mod.getModifier())) {
                        instance.removeModifier(mod.getModifier());
                    }
                }
            }
        });
        float afterMaxHealth = entity.getMaxHealth();
        if(updateHealth && afterMaxHealth != previousMaxHealth) {
            entity.setHealth(afterMaxHealth * previousHealthPercent);
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("conditioned_attribute"),
            new SerializableData()
                .add("modifier", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIER, null)
                .add("modifiers", ApoliDataTypes.ATTRIBUTED_ATTRIBUTE_MODIFIERS, null)
                .add("tick_rate", SerializableDataTypes.INT, 20)
                .add("update_health", SerializableDataTypes.BOOLEAN, true),
            data ->
                (type, player) -> {
                    ConditionedAttributePower ap = new ConditionedAttributePower(type, player, data.getInt("tick_rate"), data.getBoolean("update_health"));
                    if(data.isPresent("modifier")) {
                        ap.addModifier((AttributedEntityAttributeModifier)data.get("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        List<AttributedEntityAttributeModifier> modifierList = (List<AttributedEntityAttributeModifier>)data.get("modifiers");
                        modifierList.forEach(ap::addModifier);
                    }
                    return ap;
                }).allowCondition();
    }
}
