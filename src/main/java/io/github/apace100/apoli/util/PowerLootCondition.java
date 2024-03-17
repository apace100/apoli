package io.github.apace100.apoli.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.util.Identifier;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PowerLootCondition implements LootCondition {

    public static final Codec<PowerLootCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.fieldOf("power").forGetter(PowerLootCondition::getPowerId),
        Identifier.CODEC.optionalFieldOf("source").forGetter(PowerLootCondition::getPowerSourceId)
    ).apply(instance, PowerLootCondition::new));
    public static final LootConditionType TYPE = new LootConditionType(CODEC);

    private final Identifier powerId;
    private final Optional<Identifier> powerSourceId;

    private PowerLootCondition(Identifier powerId, Optional<Identifier> powerSourceId) {
        this.powerId = powerId;
        this.powerSourceId = powerSourceId;
    }

    @Override
    public LootConditionType getType() {
        return TYPE;
    }

    @Override
    public boolean test(LootContext lootContext) {

        Entity entity = lootContext.get(LootContextParameters.THIS_ENTITY);
        PowerHolderComponent component = PowerHolderComponent.KEY.maybeGet(entity)
            .orElse(null);

        if (component == null) {
            return false;
        }

        PowerType<?> powerType = PowerTypeRegistry.getNullable(powerId);
        return powerType != null && powerSourceId
            .map(id -> component.hasPower(powerType, id))
            .orElse(component.hasPower(powerType));

    }

    public Identifier getPowerId() {
        return powerId;
    }

    public Optional<Identifier> getPowerSourceId() {
        return powerSourceId;
    }

}
