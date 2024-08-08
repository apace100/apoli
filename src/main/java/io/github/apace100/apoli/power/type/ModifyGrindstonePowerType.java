package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModifyGrindstonePowerType extends PowerType {

    private final Predicate<Pair<World, ItemStack>> topItemCondition;
    private final Predicate<Pair<World, ItemStack>> bottomItemCondition;
    private final Predicate<Pair<World, ItemStack>> outputItemCondition;
    private final Predicate<CachedBlockPosition> blockCondition;

    private final ItemStack newResultStack;
    private final Consumer<Pair<World, StackReference>> resultItemAction;
    private final Consumer<Pair<World, StackReference>> lateItemAction;
    private final Consumer<Entity> entityAction;
    private final Consumer<Triple<World, BlockPos, Direction>> blockAction;

    private final ResultType resultType;

    private final Modifier experienceModifier;

    public ModifyGrindstonePowerType(Power power, LivingEntity entity, Predicate<Pair<World, ItemStack>> topItemCondition, Predicate<Pair<World, ItemStack>> bottomItemCondition, Predicate<Pair<World, ItemStack>> outputItemCondition, Predicate<CachedBlockPosition> blockCondition, ItemStack newResultStack, Consumer<Pair<World, StackReference>> resultItemAction, Consumer<Pair<World, StackReference>> lateItemAction, Consumer<Entity> entityAction, Consumer<Triple<World, BlockPos, Direction>> blockAction, ResultType resultType, Modifier experienceModifier) {
        super(power, entity);
        this.topItemCondition = topItemCondition;
        this.bottomItemCondition = bottomItemCondition;
        this.outputItemCondition = outputItemCondition;
        this.blockCondition = blockCondition;
        this.newResultStack = newResultStack;
        this.resultItemAction = resultItemAction;
        this.lateItemAction = lateItemAction;
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.resultType = resultType;
        this.experienceModifier = experienceModifier;
    }

    public boolean allowsInTop(ItemStack stack) {
        return topItemCondition == null || topItemCondition.test(new Pair<>(entity.getWorld(), stack));
    }

    public boolean allowsInBottom(ItemStack stack) {
        return bottomItemCondition == null || bottomItemCondition.test(new Pair<>(entity.getWorld(), stack));
    }

    public void applyAfterGrindingItemAction(StackReference outputStackRef) {

        if (lateItemAction != null) {
            lateItemAction.accept(new Pair<>(entity.getWorld(), outputStackRef));
        }

    }

    public boolean doesApply(ItemStack topStack, ItemStack buttomStack, ItemStack originalOutput, @Nullable BlockPos grindstonePos) {
        World world = entity.getWorld();
        return (topItemCondition == null || topItemCondition.test(new Pair<>(world, topStack)))
            && (bottomItemCondition == null || bottomItemCondition.test(new Pair<>(world, buttomStack)))
            && (outputItemCondition == null || outputItemCondition.test(new Pair<>(world, originalOutput)))
            && (blockCondition == null || (grindstonePos != null && blockCondition.test(new CachedBlockPosition(world, grindstonePos, true))));
    }

    public Modifier getExperienceModifier() {
        return experienceModifier;
    }

    public void setOutput(ItemStack inputTop, ItemStack inputBottom, StackReference currentOutput) {

        switch (resultType) {
            case SPECIFIED ->
                currentOutput.set(newResultStack.copy());
            case FROM_BOTTOM ->
                currentOutput.set(inputBottom.copy());
            case FROM_TOP ->
                currentOutput.set(inputTop.copy());
        }

        if (resultItemAction != null) {
            resultItemAction.accept(new Pair<>(entity.getWorld(), currentOutput));
        }

    }

    public void executeActions(@Nullable BlockPos pos) {

        if (entityAction != null) {
            entityAction.accept(entity);
        }

        if (pos != null && blockAction != null) {
            blockAction.accept(Triple.of(entity.getWorld(), pos, Direction.UP));
        }

    }

    public static boolean allowsInTopSlot(GrindstoneScreenHandler screenHandler, ItemStack stack) {
        return screenHandler instanceof PowerModifiedGrindstone powerModifiedGrindstone
            && PowerHolderComponent.hasPowerType(powerModifiedGrindstone.apoli$getPlayer(), ModifyGrindstonePowerType.class, p -> p.allowsInTop(stack));
    }

    public static boolean allowsInBottomSlot(GrindstoneScreenHandler screenHandler, ItemStack stack) {
        return screenHandler instanceof PowerModifiedGrindstone powerModifiedGrindstone
            && PowerHolderComponent.hasPowerType(powerModifiedGrindstone.apoli$getPlayer(), ModifyGrindstonePowerType.class, p -> p.allowsInBottom(stack));
    }

    public static PowerTypeFactory<ModifyGrindstonePowerType> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_grindstone"),
            new SerializableData()
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("block_action", ApoliDataTypes.BLOCK_ACTION, null)
                .add("top_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("bottom_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("output_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
                .add("item_action_after_grinding", ApoliDataTypes.ITEM_ACTION, null)
                .add("result_stack", SerializableDataTypes.ITEM_STACK, null)
                .add("result_type", SerializableDataType.enumValue(ResultType.class), ResultType.UNCHANGED)
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null)
                .add("xp_modifier", Modifier.DATA_TYPE, null),
            data -> (power, entity) -> new ModifyGrindstonePowerType(power, entity,
                data.get("top_condition"),
                data.get("bottom_condition"),
                data.get("output_condition"),
                data.get("block_condition"),
                data.get("result_stack"),
                data.get("item_action"),
                data.get("item_action_after_grinding"),
                data.get("entity_action"),
                data.get("block_action"),
                data.get("result_type"),
                data.get("xp_modifier")
            )
        ).allowCondition();
    }

    private enum ResultType {
        UNCHANGED, SPECIFIED, FROM_TOP, FROM_BOTTOM
    }
}
