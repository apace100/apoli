package io.github.apace100.apoli.power.type;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.InventoryUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

//  TODO: Fix this power type unreliably modifying attribute modifier enchantment effects -eggohito
public class ModifyEnchantmentLevelPowerType extends ValueModifyingPowerType {

    @ApiStatus.Internal
    public static final ConcurrentHashMap<UUID, WeakHashMap<ItemStack, ItemStack>> COPY_TO_ORIGINAL_STACK = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, WeakHashMap<ItemStack, ItemEnchantmentsComponent>> ITEM_ENCHANTMENTS = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<UUID, ItemStack> MODIFIED_EMPTY_STACKS = new ConcurrentHashMap<>();
    private static final WeakHashMap<Pair<UUID, ItemStack>, ConcurrentHashMap<ModifyEnchantmentLevelPowerType, Pair<Integer, Boolean>>> POWER_MODIFIER_CACHE = new WeakHashMap<>(256);

    private final RegistryKey<Enchantment> enchantmentKey;
    private final Predicate<net.minecraft.util.Pair<World, ItemStack>> itemCondition;

    public ModifyEnchantmentLevelPowerType(Power power, LivingEntity entity, RegistryKey<Enchantment> enchantmentKey, Predicate<net.minecraft.util.Pair<World, ItemStack>> itemCondition, Modifier modifier, List<Modifier> modifiers) {
        super(power, entity);

        this.enchantmentKey = enchantmentKey;
        this.itemCondition = itemCondition;

        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

        this.setTicking();

    }

    @Override
    public void onRemoved() {

        for (int slot : InventoryUtil.getAllSlots()) {

            StackReference stackReference = entity.getStackReference(slot);

            if (stackReference != StackReference.EMPTY && isWorkableEmptyStack(entity, stackReference)) {
                stackReference.set(ItemStack.EMPTY);
            }

        }

        COPY_TO_ORIGINAL_STACK.remove(entity.getUuid());
        ITEM_ENCHANTMENTS.remove(entity.getUuid());

        MODIFIED_EMPTY_STACKS.remove(entity.getUuid());

    }

    @Override
    public void tick() {

        for (int slot : InventoryUtil.getAllSlots()) {

            StackReference stackReference = entity.getStackReference(slot);
            if (stackReference == StackReference.EMPTY) {
                continue;
            }

            ItemStack stack = stackReference.get();
            if (stack.isEmpty() && !isWorkableEmptyStack(entity, stackReference) && checkItemCondition(stack)) {
                stackReference.set(getOrCreateWorkableEmptyStack(entity));
            }

        }

    }

    public static ItemEnchantmentsComponent getEnchantments(ItemStack stack, ItemEnchantmentsComponent original, boolean modified) {

        Entity entity = ((EntityLinkedItemStack)stack).apoli$getEntity();
        if (entity == null || !modified) {
            return original;
        }

        UUID uuid = entity.getUuid();
        ItemStack actualStack = COPY_TO_ORIGINAL_STACK.containsKey(uuid)
            ? COPY_TO_ORIGINAL_STACK.get(uuid).getOrDefault(stack, stack)
            : stack;

        if (ITEM_ENCHANTMENTS.containsKey(uuid) && ITEM_ENCHANTMENTS.get(uuid).containsKey(actualStack)) {
            return ITEM_ENCHANTMENTS.get(uuid).get(actualStack);
        }

        return original;

    }

    public static ItemEnchantmentsComponent getAndUpdateModifiedEnchantments(ItemStack stack, ItemEnchantmentsComponent original) {

        Entity entity = ((EntityLinkedItemStack)stack).apoli$getEntity();

        if (entity instanceof LivingEntity living) {
            calculateLevels(living, stack);
        }

        return getEnchantments(stack, original, true);

    }

    private static void calculateLevels(LivingEntity entity, ItemStack stack) {

        for (ModifyEnchantmentLevelPowerType power : PowerHolderComponent.getPowerTypes(entity, ModifyEnchantmentLevelPowerType.class)) {

            Pair<UUID, ItemStack> uuidAndStack = Pair.of(entity.getUuid(), stack);
            int baseModifiedLevel = (int) ModifierUtil.applyModifiers(entity, power.getModifiers(), 0);

            if (POWER_MODIFIER_CACHE.containsKey(uuidAndStack) && !updateIfDifferent(POWER_MODIFIER_CACHE.get(uuidAndStack), power, stack, baseModifiedLevel, power.doesApply(power.enchantmentKey, stack))) {
                continue;
            }

            //  If all modify enchantment powers are not active...
            if (ITEM_ENCHANTMENTS.containsKey(entity.getUuid()) && POWER_MODIFIER_CACHE.containsKey(uuidAndStack) && POWER_MODIFIER_CACHE.get(uuidAndStack).entrySet().stream().filter(entry -> entry.getKey().enchantmentKey.equals(power.enchantmentKey)).noneMatch(entry -> entry.getValue().getSecond())) {
                //  Remove the power's enchantments component
                ITEM_ENCHANTMENTS.get(entity.getUuid()).remove(stack);
                break;
            }

            ItemEnchantmentsComponent.Builder enchantmentsBuilder = new ItemEnchantmentsComponent.Builder(stack.getEnchantments());
            Set<RegistryEntry<Enchantment>> processedEnchantments = new HashSet<>();

            //  Iterate on all powers, because we found a match, and must set the item enchantments accordingly
            for (ModifyEnchantmentLevelPowerType innerPower : PowerHolderComponent.getPowerTypes(entity, ModifyEnchantmentLevelPowerType.class)) {

                RegistryEntry<Enchantment> innerEnchantment = entity.getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .entryOf(innerPower.enchantmentKey);

                //  If this enchantment has already been processed, continue
                if (processedEnchantments.contains(innerEnchantment)) {
                    continue;
                }

                //  Set the enchantment level from all modify enchantment powers that have the enchantment
                int innerEnchantmentLevel = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getLevel(innerEnchantment);
                enchantmentsBuilder.set(innerEnchantment, (int) PowerHolderComponent.modify(entity, ModifyEnchantmentLevelPowerType.class, innerEnchantmentLevel, p -> innerPower.doesApply(innerPower.enchantmentKey, stack)));

                //  Mark the enchantment as processed
                processedEnchantments.add(innerEnchantment);

            }

            power.recalculateCache(entity, stack);
            ITEM_ENCHANTMENTS
                .computeIfAbsent(entity.getUuid(), uuid -> new WeakHashMap<>())
                .put(stack, enchantmentsBuilder.build());

            break;

        }

    }

    public static ItemStack getOrCreateWorkableEmptyStack(Entity entity) {

        if (!PowerHolderComponent.hasPowerType(entity, ModifyEnchantmentLevelPowerType.class)) {
            return ItemStack.EMPTY;
        }

        UUID uuid = entity.getUuid();
        if (MODIFIED_EMPTY_STACKS.containsKey(uuid)) {
            return MODIFIED_EMPTY_STACKS.get(uuid);
        }

        ItemStack workableEmptyStack = new ItemStack((Void) null);
        ((EntityLinkedItemStack) workableEmptyStack).apoli$setEntity(entity);

        return MODIFIED_EMPTY_STACKS.compute(uuid, (prevUuid, prevStack) -> workableEmptyStack);

    }

    public static void integrateCallback(Entity entity, World world) {
        MODIFIED_EMPTY_STACKS.remove(entity.getUuid());
    }

    public static boolean isWorkableEmptyStack(StackReference stackReference) {
        Entity stackHolder = ((EntityLinkedItemStack) stackReference.get()).apoli$getEntity();
        return stackHolder != null && isWorkableEmptyStack(stackHolder, stackReference);
    }

    public static boolean isWorkableEmptyStack(ItemStack stack) {
        return stack.isEmpty() && MODIFIED_EMPTY_STACKS.contains(stack);
    }

    public static boolean isWorkableEmptyStack(@NotNull Entity entity, StackReference stackReference) {
        return stackReference.get().isEmpty()
            && MODIFIED_EMPTY_STACKS.containsKey(entity.getUuid())
            && stackReference.get() == MODIFIED_EMPTY_STACKS.get(entity.getUuid());
    }

    public boolean doesApply(RegistryKey<Enchantment> enchantmentKey, ItemStack stack) {
        return this.isActive()
            && this.enchantmentKey.equals(enchantmentKey)
            && this.checkItemCondition(stack);
    }

    private static boolean updateIfDifferent(ConcurrentHashMap<ModifyEnchantmentLevelPowerType, Pair<Integer, Boolean>> map, ModifyEnchantmentLevelPowerType power, ItemStack stack, int modifierValue, boolean conditionValue) {

        map.computeIfAbsent(power, (p) -> new Pair<>(0, false));
        boolean value = false;

        if (map.get(power).getFirst() != modifierValue) {
            map.put(power, Pair.of(modifierValue, map.get(power).getSecond()));
            value = true;
        }

        if (map.get(power).getSecond() != conditionValue) {
            map.put(power, Pair.of(map.get(power).getFirst(), conditionValue));
            value = true;
        }

        return value;

    }

    public void recalculateCache(LivingEntity entity, ItemStack stack) {

        for (ModifyEnchantmentLevelPowerType power : PowerHolderComponent.getPowerTypes(entity, ModifyEnchantmentLevelPowerType.class)) {

            ConcurrentHashMap<ModifyEnchantmentLevelPowerType, Pair<Integer, Boolean>> cacheMap = new ConcurrentHashMap<>();
            cacheMap.put(power, new Pair<>((int) ModifierUtil.applyModifiers(entity, power.getModifiers(), 0), power.doesApply(power.enchantmentKey, stack)));

            POWER_MODIFIER_CACHE.put(Pair.of(entity.getUuid(), stack), cacheMap);

        }

    }

    public boolean checkItemCondition(ItemStack self) {
        return itemCondition == null || itemCondition.test(new net.minecraft.util.Pair<>(entity.getWorld(), self));
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_enchantment_level"),
            new SerializableData()
                .add("enchantment", SerializableDataTypes.ENCHANTMENT)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (power, entity) -> new ModifyEnchantmentLevelPowerType(power, entity,
                data.get("enchantment"),
                data.get("item_condition"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
