package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.function.Predicate;

public class ModifyBreakSpeedPower extends ValueModifyingPower {

    private final Predicate<CachedBlockPosition> blockCondition;

    public ModifyBreakSpeedPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition, Modifier modifier, List<Modifier> modifiers) {
        super(type, entity);
        this.blockCondition = blockCondition;
        if (modifier != null) {
            this.addModifier(modifier);
        }
        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }
    }

    public boolean doesApply(BlockPos pos) {
        return blockCondition == null || blockCondition.test(new CachedBlockPosition(entity.getWorld(), pos, true));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("modify_break_speed"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (powerType, livingEntity) -> new ModifyBreakSpeedPower(
                powerType,
                livingEntity,
                data.get("block_condition"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
