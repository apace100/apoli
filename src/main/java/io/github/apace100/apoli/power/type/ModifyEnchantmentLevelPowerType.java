package io.github.apace100.apoli.power.type;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.RootCommandNode;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.command.argument.PowerHolderArgumentType;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.mixin.LivingEntityAccessor;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.WorkableEmptyStack;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

//  FIXME:  Some enchantments, notably, those that apply attribute modifiers when equipped, do not work properly with
//          this power type
public class ModifyEnchantmentLevelPowerType extends ValueModifyingPowerType {

    private static final Cache<UUID, Cache<ItemStack, ItemEnchantmentsComponent>> ENCHANTMENTS_CACHE = CacheBuilder.newBuilder()
        .weakKeys()
        .build();

    public static final TypedDataObjectFactory<ModifyEnchantmentLevelPowerType> DATA_FACTORY = createConditionedModifyingRequiredDataFactory(
        new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT)
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty()),
        (data, modifiers, condition) -> new ModifyEnchantmentLevelPowerType(
            data.get("enchantment"),
            data.get("item_condition"),
            modifiers,
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("enchantment", powerType.enchantmentKey)
            .set("item_condition", powerType.itemCondition)
    );

    private final Cache<ItemStack, State> stateCache = CacheBuilder.newBuilder()
        .weakKeys()
        .build();

    private final RegistryKey<Enchantment> enchantmentKey;
    private final Optional<ItemCondition> itemCondition;

    public ModifyEnchantmentLevelPowerType(RegistryKey<Enchantment> enchantmentKey, Optional<ItemCondition> itemCondition, List<Modifier> modifiers, Optional<EntityCondition> condition) {
        super(modifiers, condition);
        this.enchantmentKey = enchantmentKey;
        this.itemCondition = itemCondition;
        this.setTicking();
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.MODIFY_ENCHANTMENT_LEVEL;
    }

    @Override
    public void onRemoved() {

        LivingEntity holder = getHolder();
        UUID uuid = holder.getUuid();

        for (var equipmentSlot : EquipmentSlot.values()) {

            ItemStack equippedStack = holder.getEquippedStack(equipmentSlot);

            if (WorkableEmptyStack.isOf(equippedStack)) {
                holder.equipStack(equipmentSlot, ItemStack.EMPTY);
            }

        }

        WorkableEmptyStack.remove(uuid);
        ENCHANTMENTS_CACHE.invalidate(uuid);

        stateCache.invalidateAll();

    }

    @Override
    public void serverTick() {

        LivingEntity holder = getHolder();

        for (var equipmentSlot : EquipmentSlot.values()) {

            StackReference stackReference = LivingEntityAccessor.callGetStackReference(holder, equipmentSlot);
            ItemStack stack = stackReference.get();

            if (stackReference != StackReference.EMPTY && stack.isEmpty() && !WorkableEmptyStack.isOf(stack)) {
                stackReference.set(WorkableEmptyStack.getOrCreate(holder));
            }

        }

    }

    public boolean doesApply(ItemStack stack) {
        return this.isActive()
            && this.doesItemConditionApply(stack);
    }

    public boolean doesItemConditionApply(ItemStack stack) {
        return this.itemCondition
            .map(condition -> condition.test(getHolder().getWorld(), stack))
            .orElse(true);
    }

    public boolean hasAppliedToStack(ItemStack stack) {
        var cache = stateCache.getIfPresent(stack);
        return cache != null
            && cache.applies;
    }

    public static ItemEnchantmentsComponent getEnchantments(ItemStack stack, boolean modified) {
        return getEnchantmentsOrElse(stack, stack.getEnchantments(), modified);
    }

    @ApiStatus.Internal
    public static ItemEnchantmentsComponent getEnchantmentsOrElse(ItemStack stack, ItemEnchantmentsComponent defaultEnchantments, boolean modified) {

        if (!modified || !(stack instanceof EntityLinkedItemStack linkedStack) || linkedStack.apoli$getEntity() == null) {
            return defaultEnchantments;
        }

        UUID uuid = linkedStack.apoli$getEntity().getUuid();
        var enchantmentsCache = ENCHANTMENTS_CACHE.getIfPresent(uuid);

        if (enchantmentsCache != null) {
            return enchantmentsCache.asMap().getOrDefault(stack, defaultEnchantments);
        }

        else {
            return defaultEnchantments;
        }

    }

    @ApiStatus.Internal
    public static ItemEnchantmentsComponent updateAndGetEnchantments(ItemStack stack, ItemEnchantmentsComponent original) {

        if (stack instanceof EntityLinkedItemStack linkedStack && linkedStack.apoli$getEntity() instanceof LivingEntity livingEntity) {
            recalculateCache(livingEntity, stack);
        }

        return getEnchantmentsOrElse(stack, original, true);

    }

    /**
     *  Move the enchantments cache of an {@linkplain ItemStack item stack} to another, usually its copy.
     *  @param fromStack the {@linkplain ItemStack item stack} whose enchantments cache will be taken from
     *  @param toStack the {@linkplain ItemStack item stack} to move the enchantments cache to
     *  @return {@code toStack} with the updated enchantments cache
     */
    @ApiStatus.Internal
    public static ItemStack moveCache(ItemStack fromStack, ItemStack toStack) {

        if (!(fromStack instanceof EntityLinkedItemStack linkedStack) || linkedStack.apoli$getEntity() == null) {
            return toStack;
        }

        Entity entity = linkedStack.apoli$getEntity();
        var enchantmentsMapCache = ENCHANTMENTS_CACHE.getIfPresent(entity.getUuid());

        if (!PowerHolderComponent.getPowerTypes(entity, ModifyEnchantmentLevelPowerType.class, true).isEmpty()) {

            if (toStack.isEmpty()) {
                toStack = WorkableEmptyStack.getOrCreate(entity);
            }

            else {
                ((EntityLinkedItemStack) toStack).apoli$setEntity(entity);
            }

        }

        if (enchantmentsMapCache != null) {

            var enchantmentsCache = enchantmentsMapCache.asMap().remove(fromStack);
            var powerComponents = PowerHolderComponent.getNullable(entity);

            if (enchantmentsCache != null) {
                enchantmentsMapCache.put(toStack, enchantmentsCache);
            }

            if (powerComponents != null) {
                powerComponents.getPowerTypes(ModifyEnchantmentLevelPowerType.class, true).forEach(powerType -> powerType.stateCache.invalidate(fromStack));
            }

        }

        return toStack;

    }

    public static void recalculateCache(LivingEntity entity, ItemStack stack) {

        UUID uuid = entity.getUuid();
        boolean update = false;

        //  Iterate through each power type without checking if it's active...
        for (var powerType : PowerHolderComponent.getPowerTypes(entity, ModifyEnchantmentLevelPowerType.class, true)) {

            //  ...to check if the power type no longer applies to the item stack, or if its modifiers have changed
            var newCache = new State(powerType.doesApply(stack), (int) Math.round(ModifierUtil.applyModifiers(entity, powerType.getModifiers(), 0)));
            var oldCache = powerType.stateCache.getIfPresent(stack);

            //  If the power type doesn't have a cache of its previous state, or its previous and current state no longer match...
            if (oldCache != null && oldCache.equals(newCache)) {
                continue;
            }

            //  ...update the state cache and state that the enchantments cache has to be updated as well
            powerType.stateCache.put(stack, newCache);
            update = true;

        }

        //  If the enchantments cache should be updated...
        if (!update) {
            return;
        }

        Registry<Enchantment> enchantmentRegistry = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        ItemEnchantmentsComponent.Builder enchantmentsBuilder = new ItemEnchantmentsComponent.Builder(stack.getEnchantments());

        IntSet processedEnchantmentIds = new IntOpenHashSet();
        update = false;

        //  ...iterate through every power type without checking if it's active again (to eliminate unnecessary checks)
        for (var powerType : PowerHolderComponent.getPowerTypes(entity, ModifyEnchantmentLevelPowerType.class, true)) {

            RegistryEntry.Reference<Enchantment> enchantmentReference = enchantmentRegistry.getEntry(powerType.enchantmentKey).orElseThrow();
            int enchantmentId = enchantmentRegistry.getRawId(enchantmentReference.value());

            //  If the specified enchantment in the power type hasn't been processed yet...
            if (processedEnchantmentIds.contains(enchantmentId)) {
                continue;
            }

            //  ...modify the level of the enchantment by checking if the power type's previously updated state applies
            //  to the stack and if it matches the specified enchantment in the power type...
            int level = enchantmentsBuilder.getLevel(enchantmentReference);
            int modifiedLevel = (int) Math.round(PowerHolderComponent.modify(entity, ModifyEnchantmentLevelPowerType.class, level, innerPowerType -> innerPowerType.hasAppliedToStack(stack) && innerPowerType.enchantmentKey.equals(powerType.enchantmentKey), innerPowerType -> {}, true));

            enchantmentsBuilder.set(enchantmentReference, modifiedLevel);
            processedEnchantmentIds.add(enchantmentId);

            //  ...and state that the enchantments cache has been updated
            update = true;

        }

        if (update) {

	        try {
		        ENCHANTMENTS_CACHE
		            .get(uuid, () -> CacheBuilder.newBuilder().weakKeys().build())
		            .put(stack, enchantmentsBuilder.build());
	        }

            catch (ExecutionException e) {
                //  This shouldn't happen as there isn't any exceptions thrown when loading new cache values
                Apoli.LOGGER.warn("Failed to update enchantments cache!", e);
	        }

        }

    }

    public static final class DebugCommand {

	    public static void register(RootCommandNode<ServerCommandSource> rootNode) {

            var melNode = literal(PowerTypes.MODIFY_ENCHANTMENT_LEVEL.id().getPath())
                .requires(source -> source.hasPermissionLevel(2))
                .build();

            melNode.addChild(CacheNode.builder().build());
            rootNode.addChild(melNode);

	    }

        public static class CacheNode {

            public static LiteralArgumentBuilder<ServerCommandSource> builder() {
                return literal("cache")
                    .then(argument("target", PowerHolderArgumentType.holder())
                        .executes(CacheNode::execute));
            }

            public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

                UUID uuid = PowerHolderArgumentType.getHolder(context, "target").getUuid();
                var enchantmentsMapCache = ENCHANTMENTS_CACHE.getIfPresent(uuid);

                if (enchantmentsMapCache == null) {
                    throw new SimpleCommandExceptionType(() -> "Entity with UUID \"" + uuid + "\" didn't have a cache of enchantments!").create();
                }

                MutableText enchantmentsText = Text.empty();
                var enchantmentsEntrySet = enchantmentsMapCache.asMap().entrySet();

                for (var entry : enchantmentsEntrySet) {

                    var stack = entry.getKey();
                    var enchantments = entry.getValue();

                    enchantmentsText
                        .append("\n")
                        .append(Text.literal(stack.toString() + " (identity hash: " + stack.hashCode() + ")"));

                    for (var enchantmentWithLevel : enchantments.getEnchantmentEntries()) {

                        var enchantment = enchantmentWithLevel.getKey();
                        var level = enchantmentWithLevel.getIntValue();

                        enchantmentsText
                            .append("\n")
                            .append(" - (").append(enchantment.value().description()).append("): ")
                            .append(Text.of(Integer.toString(level)));

                    }

                }

                context.getSource().sendFeedback(() -> enchantmentsText, false);
                return (int) enchantmentsMapCache.size();

            }

        }

    }

    public record State(boolean applies, int level) {

    }

}
