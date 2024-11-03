package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.access.PowerModifiedGrindstone;
import io.github.apace100.apoli.action.BlockAction;
import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.action.ItemAction;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.BlockCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModifyGrindstonePowerType extends PowerType {

    public static final TypedDataObjectFactory<ModifyGrindstonePowerType> DATA_FACTORY = createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("block_action", BlockAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("item_action_after_grinding", ItemAction.DATA_TYPE.optional(), Optional.empty())
            .add("top_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("bottom_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("output_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("block_condition", BlockCondition.DATA_TYPE.optional(), Optional.empty())
            .add("result_stack", SerializableDataTypes.ITEM_STACK.optional(), Optional.empty())
            .add("xp_modifier", Modifier.DATA_TYPE.optional(), Optional.empty())
            .add("result_type", SerializableDataType.enumValue(ResultType.class), ResultType.UNCHANGED),
        (data, condition) -> new ModifyGrindstonePowerType(
            data.get("entity_action"),
            data.get("block_action"),
            data.get("item_action"),
            data.get("item_action_after_grinding"),
            data.get("top_condition"),
            data.get("bottom_condition"),
            data.get("output_condition"),
            data.get("block_condition"),
            data.get("result_stack"),
            data.get("xp_modifier"),
            data.get("result_type"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("block_action", powerType.blockAction)
            .set("item_action", powerType.itemAction)
            .set("item_action_after_grinding", powerType.itemActionAfterGrinding)
            .set("top_condition", powerType.topItemCondition)
            .set("bottom_condition", powerType.bottomItemCondition)
            .set("output_condition", powerType.outputItemCondition)
            .set("block_condition", powerType.blockCondition)
            .set("result_stack", powerType.resultStack)
            .set("xp_modifier", powerType.experienceModifier)
            .set("result_type", powerType.resultType)
    );

    private final Optional<EntityAction> entityAction;
    private final Optional<BlockAction> blockAction;

    private final Optional<ItemAction> itemAction;
    private final Optional<ItemAction> itemActionAfterGrinding;

    private final Optional<ItemCondition> topItemCondition;
    private final Optional<ItemCondition> bottomItemCondition;
    private final Optional<ItemCondition> outputItemCondition;
    private final Optional<BlockCondition> blockCondition;

    private final Optional<ItemStack> resultStack;

    private final Optional<Modifier> experienceModifier;
    private final ResultType resultType;

    public ModifyGrindstonePowerType(Optional<EntityAction> entityAction, Optional<BlockAction> blockAction, Optional<ItemAction> itemActionAfterGrinding, Optional<ItemCondition> topItemCondition, Optional<ItemCondition> bottomItemCondition, Optional<ItemCondition> outputItemCondition, Optional<BlockCondition> blockCondition, Optional<ItemStack> resultStack, Optional<ItemAction> itemAction, Optional<Modifier> experienceModifier, ResultType resultType, Optional<EntityCondition> condition) {
        super(condition);
        this.entityAction = entityAction;
        this.blockAction = blockAction;
        this.itemAction = itemAction;
        this.itemActionAfterGrinding = itemActionAfterGrinding;
        this.topItemCondition = topItemCondition;
        this.bottomItemCondition = bottomItemCondition;
        this.outputItemCondition = outputItemCondition;
        this.blockCondition = blockCondition;
        this.resultStack = resultStack;
        this.experienceModifier = experienceModifier;
        this.resultType = resultType;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_GRINDSTONE;
    }

    public boolean allowsInTop(ItemStack stack) {
        return topItemCondition
            .map(condition -> condition.test(getHolder().getWorld(), stack))
            .orElse(true);
    }

    public boolean allowsInBottom(ItemStack stack) {
        return bottomItemCondition
            .map(condition -> condition.test(getHolder().getWorld(), stack))
            .orElse(true);
    }

    public boolean doesApply(ItemStack topStack, ItemStack bottomStack, ItemStack originalOutput, @Nullable BlockPos grindstonePos) {
        World world = getHolder().getWorld();
        return allowsInTop(topStack)
            && allowsInBottom(bottomStack)
            && outputItemCondition.map(condition -> condition.test(world, originalOutput)).orElse(true)
            && blockCondition.map(condition -> grindstonePos != null && condition.test(world, grindstonePos)).orElse(true);
    }

    public Optional<Modifier> getExperienceModifier() {
        return experienceModifier;
    }

    public void setOutput(ItemStack inputTop, ItemStack inputBottom, StackReference currentOutputStackReference) {

        switch (resultType) {
            case SPECIFIED -> resultStack
                .map(ItemStack::copy)
                .ifPresent(currentOutputStackReference::set);
            case FROM_BOTTOM ->
                currentOutputStackReference.set(inputBottom.copy());
            case FROM_TOP ->
                currentOutputStackReference.set(inputTop.copy());
        }

        itemAction.ifPresent(action -> action.execute(getHolder().getWorld(), currentOutputStackReference));

    }

    public void executeActions(@Nullable BlockPos pos, StackReference outputStackRef) {
        executeActions(pos);
        applyAfterGrindingItemAction(outputStackRef);
    }

    public void executeActions(@Nullable BlockPos pos) {
        entityAction.ifPresent(action -> action.execute(getHolder()));
        blockAction.filter(action -> pos != null).ifPresent(action -> action.execute(getHolder().getWorld(), pos, Optional.empty()));
    }

    public void applyAfterGrindingItemAction(StackReference outputStackReference) {
        itemActionAfterGrinding.ifPresent(action -> action.execute(getHolder().getWorld(), outputStackReference));
    }

    public static boolean allowsInTopSlot(GrindstoneScreenHandler screenHandler, ItemStack stack) {
        return screenHandler instanceof PowerModifiedGrindstone powerModifiedGrindstone
            && PowerHolderComponent.hasPowerType(powerModifiedGrindstone.apoli$getPlayer(), ModifyGrindstonePowerType.class, p -> p.allowsInTop(stack));
    }

    public static boolean allowsInBottomSlot(GrindstoneScreenHandler screenHandler, ItemStack stack) {
        return screenHandler instanceof PowerModifiedGrindstone powerModifiedGrindstone
            && PowerHolderComponent.hasPowerType(powerModifiedGrindstone.apoli$getPlayer(), ModifyGrindstonePowerType.class, p -> p.allowsInBottom(stack));
    }

    public enum ResultType {
        UNCHANGED, SPECIFIED, FROM_TOP, FROM_BOTTOM
    }

}
