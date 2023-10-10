package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.EntityLinkedItemStack;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ModifyEnchantmentLevelPower extends ValueModifyingPower {

    private static final ConcurrentHashMap<Entity, ConcurrentHashMap<ItemStack, NbtList>> ENTITY_ITEM_ENCHANTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Entity, ConcurrentHashMap<ModifyEnchantmentLevelPower, Pair<Integer, Boolean>>> POWER_MODIFIER_CACHE = new ConcurrentHashMap<>();

    private final Enchantment enchantment;
    private final Predicate<Pair<World, ItemStack>> itemCondition;

    public ModifyEnchantmentLevelPower(PowerType<?> type, LivingEntity entity, Enchantment enchantment, Predicate<Pair<World, ItemStack>> itemCondition, Modifier modifier, List<Modifier> modifiers) {
        super(type, entity);

        this.enchantment = enchantment;
        this.itemCondition = itemCondition;

        if (modifier != null) {
            this.addModifier(modifier);
        }

        if (modifiers != null) {
            modifiers.forEach(this::addModifier);
        }

    }

    @Override
    public void onAdded(boolean onSync) {

        ENTITY_ITEM_ENCHANTS.computeIfAbsent(entity, (_entity) -> new ConcurrentHashMap<>());
        ConcurrentHashMap<ModifyEnchantmentLevelPower, Pair<Integer, Boolean>> cache = POWER_MODIFIER_CACHE.computeIfAbsent(entity, (_entity) -> new ConcurrentHashMap<>());

        cache.compute(this, (p, _val) -> new Pair<>(0, false));

    }

    @Override
    public void onRemoved(boolean onSync) {

        if (POWER_MODIFIER_CACHE.containsKey(entity)) {
            POWER_MODIFIER_CACHE.get(entity).remove(this);
        }

        if (PowerHolderComponent.getPowers(entity, ModifyEnchantmentLevelPower.class).isEmpty()) {
            ENTITY_ITEM_ENCHANTS.remove(entity);
        }

    }

    public static boolean isInEnchantmentMap(LivingEntity entity) {
        return ENTITY_ITEM_ENCHANTS.containsKey(entity);
    }

    public static boolean isInEnchantmentMap(Entity entity) {
        return entity instanceof LivingEntity livingEntity && isInEnchantmentMap(livingEntity);
    }

    public boolean doesApply(Enchantment enchantment, ItemStack self) {
        return enchantment.equals(this.enchantment) && checkItemCondition(self);
    }

    public boolean checkItemCondition(ItemStack self) {
        return itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), self));
    }

    private static Optional<Integer> findEnchantIndex(Identifier id, NbtList enchantmentsNbt) {

        for (int index = 0; index < enchantmentsNbt.size(); ++index) {

            NbtCompound enchantmentNbt = enchantmentsNbt.getCompound(index);
            Identifier enchantmentId = Identifier.tryParse(enchantmentNbt.getString("id"));

            if ((enchantmentId != null && id != null) && enchantmentId.equals(id)) {
                return Optional.of(index);
            }

        }

        return Optional.empty();

    }

    private static NbtList generateEnchantments(NbtList enchants, ItemStack self) {

        Entity stackHolder = ((EntityLinkedItemStack) self).apoli$getEntity();
        if (!(stackHolder instanceof LivingEntity livingStackHolder)) {
            return enchants;
        }

        NbtList newEnchantmentsNbt = enchants.copy();
        for (ModifyEnchantmentLevelPower power : PowerHolderComponent.getPowers(livingStackHolder, ModifyEnchantmentLevelPower.class)) {

            Enchantment enchantment = power.enchantment;
            Identifier enchantmentId = Registries.ENCHANTMENT.getId(enchantment);

            if (enchantmentId == null || !power.doesApply(enchantment, self)) {
                continue;
            }

            Optional<Integer> enchantmentIndex = findEnchantIndex(enchantmentId, newEnchantmentsNbt);
            if (enchantmentIndex.isPresent()) {

                NbtCompound existingEnchantmentNbt = newEnchantmentsNbt.getCompound(enchantmentIndex.get());

                int enchantmentLvl = existingEnchantmentNbt.getInt("lvl");
                int newEnchantmentLvl = (int) ModifierUtil.applyModifiers(livingStackHolder, power.getModifiers(), enchantmentLvl);

                existingEnchantmentNbt.putInt("lvl", newEnchantmentLvl);
                newEnchantmentsNbt.set(enchantmentIndex.get(), existingEnchantmentNbt);

            } else {

                NbtCompound newEnchantmentNbt = new NbtCompound();

                newEnchantmentNbt.putString("id", enchantmentId.toString());
                newEnchantmentNbt.putInt("lvl", (int) ModifierUtil.applyModifiers(livingStackHolder, power.getModifiers(), 0));

                newEnchantmentsNbt.add(newEnchantmentNbt);

            }

        }

        return newEnchantmentsNbt;

    }

    public static NbtList getEnchantments(ItemStack stack, NbtList originalTag) {

        Entity stackHolder = ((EntityLinkedItemStack) stack).apoli$getEntity();
        if (!(stackHolder instanceof LivingEntity livingStackHolder) || !ENTITY_ITEM_ENCHANTS.containsKey(stackHolder)) {
            return originalTag;
        }

        ConcurrentHashMap<ItemStack, NbtList> itemEnchants = ENTITY_ITEM_ENCHANTS.get(stackHolder);
        if (shouldReapplyEnchantments(livingStackHolder, stack)) {
            itemEnchants.computeIfAbsent(stack, _stack -> originalTag);
            return itemEnchants.compute(stack, (_stack, nbtElements) -> generateEnchantments(originalTag, stack));
        }

        return itemEnchants.getOrDefault(stack, originalTag);

    }

    public static int getEquipmentLevel(Enchantment enchantment, LivingEntity livingEntity, boolean useModifications) {

        int equippedEnchantmentLevel = 0;

        for (ItemStack stack : enchantment.getEquipment(livingEntity).values()) {

            int enchantmentLevel = getLevel(livingEntity, enchantment, stack, useModifications);

            if (enchantmentLevel > equippedEnchantmentLevel) {
                equippedEnchantmentLevel = enchantmentLevel;
            }

        }

        return equippedEnchantmentLevel;

    }

    public static Map<Enchantment, Integer> get(ItemStack stack, boolean useModifications) {

        Entity stackHolder = ((EntityLinkedItemStack) stack).apoli$getEntity();
        if (!useModifications || !(stackHolder instanceof LivingEntity livingStackHolder) || !ENTITY_ITEM_ENCHANTS.containsKey(livingStackHolder)) {
            return EnchantmentHelper.get(stack);
        }

        ConcurrentHashMap<ItemStack, NbtList> itemEnchants = ENTITY_ITEM_ENCHANTS.get(livingStackHolder);
        return EnchantmentHelper.fromNbt(itemEnchants.computeIfAbsent(stack, ItemStack::getEnchantments));

    }

    public static int getLevel(Enchantment enchantment, ItemStack self, boolean useModifications) {
        return getLevel(null, enchantment, self, useModifications);
    }

    public static int getLevel(LivingEntity livingEntity, Enchantment enchantment, ItemStack stack, boolean useModifications) {

        Identifier enchantmentId = Registries.ENCHANTMENT.getId(enchantment);
        Optional<Integer> enchantmentIndex = findEnchantIndex(enchantmentId, stack.getEnchantments());

        if (!useModifications) {

            return enchantmentIndex.map(index -> {

                NbtCompound existingEnchantmentNbt = stack.getEnchantments().getCompound(index);
                return EnchantmentHelper.getLevelFromNbt(existingEnchantmentNbt);

            }).orElse(0);

        }

        Entity nullSafeEntity = livingEntity == null ? ((EntityLinkedItemStack) stack).apoli$getEntity() : livingEntity;
        if (!(nullSafeEntity instanceof LivingEntity livingNullSafeEntity) || !ENTITY_ITEM_ENCHANTS.containsKey(livingNullSafeEntity)) {
            return EnchantmentHelper.getLevel(enchantment, stack);
        }

        ConcurrentHashMap<ItemStack, NbtList> itemEnchants = ENTITY_ITEM_ENCHANTS.get(livingNullSafeEntity);
        NbtList newEnchantmentsNbt = itemEnchants.computeIfAbsent(stack, ItemStack::getEnchantments);

        return enchantmentIndex.map(index -> {

            NbtCompound existingEnchantmentNbt = newEnchantmentsNbt.getCompound(index);
            return EnchantmentHelper.getLevelFromNbt(existingEnchantmentNbt);

        }).orElse(0);

    }

    private static boolean updateIfDifferent(ConcurrentHashMap<ModifyEnchantmentLevelPower, Pair<Integer, Boolean>> map, ModifyEnchantmentLevelPower power, int modifierValue, boolean conditionValue) {

        map.computeIfAbsent(power, (p) -> new Pair<>(0, false));
        boolean value = false;

        if (map.get(power).getLeft() != modifierValue) {
            map.get(power).setLeft(modifierValue);
            value = true;
        }

        if (map.get(power).getRight() != conditionValue) {
            map.get(power).setRight(conditionValue);
            value = true;
        }

        return value;

    }

    private static boolean shouldReapplyEnchantments(LivingEntity living, ItemStack self) {

        List<ModifyEnchantmentLevelPower> powers = PowerHolderComponent.KEY.get(living).getPowers(ModifyEnchantmentLevelPower.class, true);
        ConcurrentHashMap<ItemStack, NbtList> enchants = ENTITY_ITEM_ENCHANTS.get(living);

        ConcurrentHashMap<ModifyEnchantmentLevelPower, Pair<Integer, Boolean>> cache = POWER_MODIFIER_CACHE.computeIfAbsent(living, (_entity) -> new ConcurrentHashMap<>());
        return !enchants.containsKey(self) || powers.stream().anyMatch(power -> updateIfDifferent(cache, power, (int) ModifierUtil.applyModifiers(living, power.getModifiers(), 0), power.isActive() && power.checkItemCondition(self)));

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
