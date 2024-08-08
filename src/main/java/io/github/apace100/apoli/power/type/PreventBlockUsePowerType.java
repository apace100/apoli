package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.BlockUsagePhase;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventBlockUsePowerType extends ActiveInteractionPowerType {

    private final Predicate<CachedBlockPosition> blockCondition;

    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    private final EnumSet<Direction> directions;
    private final EnumSet<BlockUsagePhase> usePhases;

    public PreventBlockUsePowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction, Consumer<Pair<World, StackReference>> resultItemAction, Consumer<Pair<World, StackReference>> heldItemAction, Predicate<CachedBlockPosition> blockCondition, Predicate<Pair<World, ItemStack>> itemCondition, ItemStack resultStack, EnumSet<Direction> directions, EnumSet<Hand> hands, EnumSet<BlockUsagePhase> usePhases, int priority) {
        super(power, entity, hands, ActionResult.FAIL, itemCondition, heldItemAction, resultStack, resultItemAction, priority);
        this.blockCondition = blockCondition;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.directions = directions;
        this.usePhases = usePhases;
    }

    public void executeActions(BlockHitResult hitResult, Hand hand) {

        if (blockAction != null) {
            this.blockAction.accept(Triple.of(entity.getWorld(), hitResult.getBlockPos(), hitResult.getSide()));
        }

        if (entityAction != null) {
            this.entityAction.accept(entity);
        }

        if (entity instanceof PlayerEntity player) {
            this.performActorItemStuff(this, player, hand);
        }

    }

    public boolean doesPrevent(BlockUsagePhase usePhase, BlockHitResult hitResult, ItemStack heldStack, Hand hand) {
        return usePhases.contains(usePhase)
            && directions.contains(hitResult.getSide())
            && super.shouldExecute(hand, heldStack)
            && (blockCondition == null || blockCondition.test(new CachedBlockPosition(entity.getWorld(), hitResult.getBlockPos(), true)));
    }

    public static boolean doesPrevent(Entity holder, BlockUsagePhase usePhase, BlockHitResult hitResult, ItemStack heldStack, Hand hand) {

        CallInstance<ActiveInteractionPowerType> aipci = new CallInstance<>();
        aipci.add(holder, PreventBlockUsePowerType.class, p -> p.doesPrevent(usePhase, hitResult, heldStack, hand));

        for (int i = aipci.getMaxPriority(); i >= aipci.getMinPriority(); i--) {
            aipci.forEach(i, p -> ((PreventBlockUsePowerType) p).executeActions(hitResult, hand));
        }

        return !aipci.isEmpty();

    }

    public static PowerTypeFactory<PreventBlockUsePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("prevent_block_use"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("result_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("held_item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("directions", SerializableDataTypes.DIRECTION_SET, EnumSet.allOf(Direction.class))
                .add("hands", SerializableDataTypes.HAND_SET, EnumSet.allOf(Hand.class))
                .add("usage_phases", ApoliDataTypes.BLOCK_USAGE_PHASE_SET, EnumSet.allOf(BlockUsagePhase.class))
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new PreventBlockUsePowerType(power, entity,
                data.get("entity_action"),
                data.get("block_action"),
                data.get("result_item_action"),
                data.get("held_item_action"),
                data.get("block_condition"),
                data.get("item_condition"),
                data.get("result_stack"),
                data.get("directions"),
                data.get("hands"),
                data.get("usage_phases"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
