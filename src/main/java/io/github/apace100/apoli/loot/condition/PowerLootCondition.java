package io.github.apace100.apoli.loot.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.PowerReference;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record PowerLootCondition(LootContext.EntityTarget target, PowerReference power, Optional<Identifier> sourceId) implements LootCondition {

    public static final MapCodec<PowerLootCondition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        LootContext.EntityTarget.CODEC.optionalFieldOf("entity", LootContext.EntityTarget.THIS).forGetter(PowerLootCondition::target),
        ApoliDataTypes.POWER_REFERENCE.codec().fieldOf("power").forGetter(PowerLootCondition::power),
        Identifier.CODEC.optionalFieldOf("source").forGetter(PowerLootCondition::sourceId)
    ).apply(instance, PowerLootCondition::new));

    @Override
    public LootConditionType getType() {
        return ApoliLootConditionTypes.POWER;
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.get(target().getParameter());
        return PowerHolderComponent.KEY.maybeGet(entity)
            .map(this::hasPower)
            .orElse(false);
    }

    private boolean hasPower(PowerHolderComponent component) {
        return sourceId()
            .map(id -> component.hasPower(power(), id))
            .orElseGet(() -> component.hasPower(power()));
    }

}
