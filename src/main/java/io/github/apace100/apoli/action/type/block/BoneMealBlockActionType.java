package io.github.apace100.apoli.action.type.block;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BlockActionType;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import java.util.Optional;

public class BoneMealBlockActionType extends BlockActionType {

    public static final DataObjectFactory<BoneMealBlockActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("effects", SerializableDataTypes.BOOLEAN, true),
        data -> new BoneMealBlockActionType(
            data.get("effects")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("effects", actionType.showEffects)
    );

    private final boolean showEffects;

    public BoneMealBlockActionType(boolean showEffects) {
        this.showEffects = showEffects;
    }

    @Override
    public void execute(World world, BlockPos pos, Optional<Direction> direction) {

        if (BoneMealItem.useOnFertilizable(ItemStack.EMPTY, world, pos)) {

            if (showEffects && !world.isClient()) {
                world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 0);
            }

        }

        else if (direction.isPresent()) {

            Direction dir = direction.get();

            BlockState blockState = world.getBlockState(pos);
            BlockPos offsetPos = pos.offset(dir);

            boolean solidSide = blockState.isSideSolidFullSquare(world, pos, dir);
            if (solidSide && BoneMealItem.useOnGround(ItemStack.EMPTY, world, offsetPos, dir)) {

                if (showEffects && !world.isClient()) {
                    world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, offsetPos, 0);
                }

            }

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BlockActionTypes.BONE_MEAL;
    }

}
