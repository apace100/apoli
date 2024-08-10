package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.util.List;
import java.util.function.Predicate;

public class ModifySlipperinessPowerType extends ValueModifyingPowerType {

    private final Predicate<CachedBlockPosition> blockCondition;

    public ModifySlipperinessPowerType(Power power, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition, Modifier modifier, List<Modifier> modifiers) {
        super(power, entity);
        this.blockCondition = blockCondition;

        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

    }

    public boolean doesApply(WorldView world, BlockPos pos) {
        return blockCondition == null || blockCondition.test(new CachedBlockPosition(world, pos, true));
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_slipperiness"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> new ModifySlipperinessPowerType(power, entity,
                data.get("block_condition"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }
    
}
