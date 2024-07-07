package io.github.apace100.apoli.power;

import com.mojang.datafixers.util.Pair;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ModifyEnchantmentLevelPower extends ValueModifyingPower {

    private static final ConcurrentHashMap<UUID, WeakHashMap<ItemStack, ItemEnchantmentsComponent>> ITEM_ENCHANTMENTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, ItemStack> MODIFIED_EMPTY_STACKS = new ConcurrentHashMap<>();
    private static final WeakHashMap<Pair<UUID, ItemStack>, ConcurrentHashMap<ModifyEnchantmentLevelPower, Pair<Integer, Boolean>>> POWER_MODIFIER_CACHE = new WeakHashMap<>(256);

    private final RegistryEntry<Enchantment> enchantment;
    private final Predicate<net.minecraft.util.Pair<World, ItemStack>> itemCondition;

    public ModifyEnchantmentLevelPower(PowerType<?> type, LivingEntity entity, RegistryKey<Enchantment> enchantment, Predicate<net.minecraft.util.Pair<World, ItemStack>> itemCondition, Modifier modifier, List<Modifier> modifiers) {
        super(type, entity);

        this.enchantment = entity.getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(enchantment);
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
    public void onRemoved(boolean onSync) {

        for (int slot : InventoryUtil.getAllSlots()) {

            StackReference stackReference = entity.getStackReference(slot);

            if (stackReference != StackReference.EMPTY && isWorkableEmptyStack(entity, stackReference)) {
                stackReference.set(ItemStack.EMPTY);
            }

        }

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

        if (modified && entity != null && ITEM_ENCHANTMENTS.containsKey(entity.getUuid()) && ITEM_ENCHANTMENTS.get(entity.getUuid()).containsKey(stack)) {
            return ITEM_ENCHANTMENTS.get(entity.getUuid()).get(stack);
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

        for (ModifyEnchantmentLevelPower power : PowerHolderComponent.getPowers(entity, ModifyEnchantmentLevelPower.class)) {
            int baseModifiedLevel = (int) ModifierUtil.applyModifiers(entity, power.getModifiers(), 0);

            if (!POWER_MODIFIER_CACHE.containsKey(Pair.of(entity.getUuid(), stack)) || updateIfDifferent(POWER_MODIFIER_CACHE.get(Pair.of(entity.getUuid(), stack)), power, stack, baseModifiedLevel, power.doesApply(power.enchantment, stack))) {

                // If all enchantment powers are not active...
                if (ITEM_ENCHANTMENTS.containsKey(entity.getUuid()) && POWER_MODIFIER_CACHE.containsKey(Pair.of(entity.getUuid(), stack)) && POWER_MODIFIER_CACHE.get(Pair.of(entity.getUuid(), stack)).entrySet().stream().filter(entry -> entry.getKey().enchantment.equals(power.enchantment)).noneMatch(entry -> entry.getValue().getSecond())) {
                    // Remove the Power Enchantments component.
                    ITEM_ENCHANTMENTS.get(entity.getUuid()).remove(stack);
                    break;
                }

                ItemEnchantmentsComponent.Builder enchantments = new ItemEnchantmentsComponent.Builder(stack.getEnchantments());

                Set<RegistryEntry<Enchantment>> processedEnchantments = new HashSet<>();

                // Iterate on all powers, because we have found a match, and must set the item enchantments accordingly.
                for (ModifyEnchantmentLevelPower innerPower : PowerHolderComponent.getPowers(entity, ModifyEnchantmentLevelPower.class)) {
                    // If this enchantment has already been processed, continue.
                    if (processedEnchantments.contains(innerPower.enchantment)) continue;

                    // Set the enchantment level from all MELPs that have this enchantment.
                    enchantments.set(innerPower.enchantment, (int) PowerHolderComponent.modify(entity, ModifyEnchantmentLevelPower.class, stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT).getLevel(innerPower.enchantment), p -> innerPower.doesApply(innerPower.enchantment, stack)));

                    //.Mark the enchantment as processed.
                    processedEnchantments.add(innerPower.enchantment);
                }

                power.recalculateCache(entity, stack);
                ITEM_ENCHANTMENTS.computeIfAbsent(entity.getUuid(), uuid -> new WeakHashMap<>()).put(stack, enchantments.build());
                break;

            }

        }
    }

    public static ItemStack getOrCreateWorkableEmptyStack(Entity entity) {

        if (!PowerHolderComponent.hasPower(entity, ModifyEnchantmentLevelPower.class)) {
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

    public boolean doesApply(RegistryEntry<Enchantment> enchantment, ItemStack self) {
        return isActive() && enchantment.equals(this.enchantment) && checkItemCondition(self);
    }

    private static boolean updateIfDifferent(ConcurrentHashMap<ModifyEnchantmentLevelPower, Pair<Integer, Boolean>> map, ModifyEnchantmentLevelPower power, ItemStack stack, int modifierValue, boolean conditionValue) {

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
        for (ModifyEnchantmentLevelPower power : PowerHolderComponent.getPowers(entity, ModifyEnchantmentLevelPower.class)) {

            ConcurrentHashMap<ModifyEnchantmentLevelPower, Pair<Integer, Boolean>> map = new ConcurrentHashMap<>();
            map.put(power, new Pair<>((int)ModifierUtil.applyModifiers(entity, power.getModifiers(), 0), power.doesApply(power.enchantment, stack)));

            POWER_MODIFIER_CACHE.put(Pair.of(entity.getUuid(), stack), map);
        }
    }

    public boolean checkItemCondition(ItemStack self) {
        return itemCondition == null || itemCondition.test(new net.minecraft.util.Pair<>(entity.getWorld(), self));
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("modify_enchantment_level"),
            new SerializableData()
                .add("enchantment", SerializableDataTypes.ENCHANTMENT)
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            data -> (powerType, livingEntity) -> new ModifyEnchantmentLevelPower(
                powerType,
                livingEntity,
                data.get("enchantment"),
                data.get("item_condition"),
                data.get("modifier"),
                data.get("modifiers")
            )
        ).allowCondition();
    }

}
