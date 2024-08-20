package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class ModifyBreakSpeedPowerType extends ValueModifyingPowerType {

    private final Predicate<CachedBlockPosition> blockCondition;
    private final List<Modifier> hardnessModifiers;

    public ModifyBreakSpeedPowerType(Power power, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition, Modifier deltaModifier, List<Modifier> deltaModifiers, Modifier hardnessModifier, List<Modifier> hardnessModifiers) {
        super(power, entity);

        if (deltaModifier != null) {
            this.addModifier(deltaModifier);
        }

        if (deltaModifiers != null) {
            deltaModifiers.forEach(this::addModifier);
        }

        this.blockCondition = blockCondition;
        this.hardnessModifiers = new LinkedList<>();

        if (hardnessModifier != null) {
            this.hardnessModifiers.add(hardnessModifier);
        }

        if (hardnessModifiers != null) {
            this.hardnessModifiers.addAll(hardnessModifiers);
        }

    }

    public List<Modifier> getHardnessModifiers() {
        return hardnessModifiers;
    }

    public boolean doesApply(BlockPos pos) {
        return blockCondition == null || blockCondition.test(new CachedBlockPosition(entity.getWorld(), pos, true));
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_break_speed"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .addFunctionedDefault("delta_modifier", Modifier.DATA_TYPE, data -> data.get("modifier"))
                .addFunctionedDefault("delta_modifiers", Modifier.LIST_TYPE, data -> data.get("modifiers"))
                .add("hardness_modifier", Modifier.DATA_TYPE, null)
                .add("hardness_modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> new ModifyBreakSpeedPowerType(power, entity,
                data.get("block_condition"),
                data.get("delta_modifier"),
                data.get("delta_modifiers"),
                data.get("hardness_modifier"),
                data.get("hardness_modifiers")
            )
        ).allowCondition();
    }

}
