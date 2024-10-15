package io.github.apace100.apoli.action.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.action.type.block.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class BlockActionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ActionConfiguration<BlockActionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.BLOCK_ACTION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Block action type \"" + id + "\" is undefined!");

    public static void register() {

        MetaActionTypes.register(ApoliDataTypes.BLOCK_ACTION, ApoliDataTypes.BLOCK_CONDITION, block -> new CachedBlockPosition(block.getLeft(), block.getMiddle(), true), BlockActionTypes::register);

        register(OffsetActionType.getFactory());
        register(SetBlockActionType.getFactory());
        register(AddBlockActionType.getFactory());
        register(ExecuteCommandActionType.getFactory());
        register(BoneMealActionType.getFactory());
        register(ModifyBlockStateActionType.getFactory());
        register(ExplodeActionType.getFactory());
        register(AreaOfEffectActionType.getFactory());
        register(SpawnEntityActionType.getFactory());
    }

    public static <F extends ActionTypeFactory<Triple<World, BlockPos, Direction>>> F register(F actionFactory) {
        return Registry.register(ApoliRegistries.BLOCK_ACTION, actionFactory.getSerializerId(), actionFactory);
    }

}
