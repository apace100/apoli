package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.List;
import java.util.function.Predicate;

public class ModifyBreakSpeedPower extends ValueModifyingPower {

    private final Predicate<CachedBlockPosition> predicate;

    public ModifyBreakSpeedPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> predicate) {
        super(type, entity);
        this.predicate = predicate;
    }

    public boolean doesApply(WorldView world, BlockPos pos) {
        if(predicate == null) {
            return true;
        }
        CachedBlockPosition cbp = new CachedBlockPosition(world, pos, true);
        return predicate.test(cbp);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_break_speed"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("modifier", SerializableDataTypes.ATTRIBUTE_MODIFIER, null)
                .add("modifiers", SerializableDataTypes.ATTRIBUTE_MODIFIERS, null),
            data ->
                (type, player) -> {
                    ModifyBreakSpeedPower power = new ModifyBreakSpeedPower(type, player, data.isPresent("block_condition") ? (ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition") : cbp -> true);
                    if(data.isPresent("modifier")) {
                        power.addModifier(data.getModifier("modifier"));
                    }
                    if(data.isPresent("modifiers")) {
                        ((List<EntityAttributeModifier>)data.get("modifiers")).forEach(power::addModifier);
                    }
                    return power;
                })
            .allowCondition();
    }
}
