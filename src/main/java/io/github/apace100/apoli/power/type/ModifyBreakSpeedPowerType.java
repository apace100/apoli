package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyBreakSpeedPowerType extends ValueModifyingPowerType {

    public static final TypedDataObjectFactory<ModifyBreakSpeedPowerType> DATA_FACTORY = createConditionedModifyingDataFactory(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("hardness_modifier", Modifier.DATA_TYPE, null)
            .addFunctionedDefault("hardness_modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrEmpty(data.get("hardness_modifier"))),
        (data, modifiers, condition) -> new ModifyBreakSpeedPowerType(
            data.get("block_condition"),
            data.get("hardness_modifiers"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("block_condition", powerType.blockCondition)
            .set("hardness_modifiers", powerType.hardnessModifiers)
    );

    private final Optional<BlockCondition> blockCondition;
    private final List<Modifier> hardnessModifiers;

    public ModifyBreakSpeedPowerType(Optional<BlockCondition> blockCondition, List<Modifier> hardnessModifiers, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
        this.blockCondition = blockCondition;
        this.hardnessModifiers = hardnessModifiers;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_BREAK_SPEED;
    }

    public List<Modifier> getHardnessModifiers() {
        return new ObjectArrayList<>(hardnessModifiers);
    }

    public boolean doesApply(BlockPos pos) {
        return blockCondition
            .map(condition -> condition.test(getHolder().getWorld(), pos))
            .orElse(true);
    }

}
