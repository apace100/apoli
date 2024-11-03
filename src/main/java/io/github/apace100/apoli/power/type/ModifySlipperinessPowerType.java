package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifySlipperinessPowerType extends ValueModifyingPowerType {

    public static final TypedDataObjectFactory<ModifySlipperinessPowerType> DATA_FACTORY = createConditionedModifyingDataFactory(
        new SerializableData()
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, modifiers, condition) -> new ModifySlipperinessPowerType(
            data.get("block_condition"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("block_condition", powerType.blockCondition)
    );

    private final Optional<BlockCondition> blockCondition;

    public ModifySlipperinessPowerType(Optional<BlockCondition> blockCondition, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
        this.blockCondition = blockCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_SLIPPERINESS;
    }

    public boolean doesApply(WorldView worldView, BlockPos pos) {
        return worldView instanceof World world && blockCondition
            .map(condition -> condition.test(world, pos))
            .orElse(true);
    }

}
