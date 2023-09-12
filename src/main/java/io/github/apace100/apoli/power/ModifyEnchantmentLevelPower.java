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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ModifyEnchantmentLevelPower extends ValueModifyingPower {
    private static final ConcurrentHashMap<Entity, ConcurrentHashMap<ItemStack, NbtList>> ENTITY_ITEM_ENCHANTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Entity, ConcurrentHashMap<ModifyEnchantmentLevelPower, Pair<Integer, Boolean>>> POWER_MODIFIER_CACHE = new ConcurrentHashMap<>();

    private final Enchantment enchantment;
    private final Predicate<ItemStack> itemCondition;

    public ModifyEnchantmentLevelPower(PowerType<?> type, LivingEntity entity, Enchantment enchantment, Predicate<ItemStack> itemCondition) {
        super(type, entity);
        this.enchantment = enchantment;
        this.itemCondition = itemCondition;
    }

    @Override
    public void onAdded() {
        ENTITY_ITEM_ENCHANTS.computeIfAbsent(entity, (_entity) -> new ConcurrentHashMap<>());
        ConcurrentHashMap<ModifyEnchantmentLevelPower, Pair<Integer, Boolean>> cache = POWER_MODIFIER_CACHE.computeIfAbsent(entity, (_entity) -> new ConcurrentHashMap<>());
        cache.compute(this, (p, _val) -> new Pair<>(0, false));
    }

    @Override
    public void onRemoved() {
        if (POWER_MODIFIER_CACHE.containsKey(entity)) {
            POWER_MODIFIER_CACHE.get(entity).remove(this);
        }
        if (PowerHolderComponent.getPowers(entity, ModifyEnchantmentLevelPower.class).size() == 0) {
            ENTITY_ITEM_ENCHANTS.remove(entity);
        }
    }

    public static boolean isInEnchantmentMap(LivingEntity entity) {
        return ENTITY_ITEM_ENCHANTS.containsKey(entity);
    }

    public boolean doesApply(Enchantment enchantment, ItemStack self) {
        return enchantment.equals(this.enchantment) && checkItemCondition(self);
    }

    public boolean checkItemCondition(ItemStack self) {
        return itemCondition == null || itemCondition.test(self);
    }

    private static Optional<Integer> findEnchantIndex(Identifier id, NbtList enchants) {
        for(int i = 0; i < enchants.size(); ++i) {
            String string = enchants.getCompound(i).getString("id");
            Identifier enchantId = Identifier.tryParse(string);
            if(enchantId != null && enchantId.equals(id)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private static NbtList generateEnchantments(NbtList enchants, ItemStack self) {
        Entity entity = ((EntityLinkedItemStack) self).apoli$getEntity();

        if(!(entity instanceof LivingEntity living)) return enchants;

        NbtList newEnchants = enchants.copy();

        List<ModifyEnchantmentLevelPower> powers = PowerHolderComponent.getPowers(entity, ModifyEnchantmentLevelPower.class);

        for (ModifyEnchantmentLevelPower power : powers) {
            Enchantment enchantment = power.enchantment;
            Identifier id = Registries.ENCHANTMENT.getId(enchantment);
            Optional<Integer> idx = findEnchantIndex(id, newEnchants);

            if (!power.doesApply(enchantment, self)) continue;

            if(idx.isPresent()) {
                NbtCompound existingEnchant = newEnchants.getCompound(idx.get());
                int lvl = existingEnchant.getInt("lvl");
                int newLvl = (int) ModifierUtil.applyModifiers(living, power.getModifiers(), lvl);
                existingEnchant.putInt("lvl", newLvl);
                newEnchants.set(idx.get(), existingEnchant);
            } else {
                NbtCompound newEnchant = new NbtCompound();
                newEnchant.putString("id", id.toString());
                newEnchant.putInt("lvl", (int) ModifierUtil.applyModifiers(living, power.getModifiers(), 0));
                newEnchants.add(newEnchant);
            }
        }

        return newEnchants;
    }

    public static NbtList getEnchantments(ItemStack self, NbtList originalTag) {
        Entity entity = ((EntityLinkedItemStack) self).apoli$getEntity();
        if (entity instanceof LivingEntity living && ENTITY_ITEM_ENCHANTS.containsKey(entity)) {
            ConcurrentHashMap<ItemStack, NbtList> itemEnchants = ENTITY_ITEM_ENCHANTS.get(entity);
            if (shouldReapplyEnchantments(living, self)) {
                itemEnchants.computeIfAbsent(self, (stack) -> originalTag);
                return itemEnchants.compute(self, (stack, tag) -> generateEnchantments(originalTag, self));
            }
            return itemEnchants.getOrDefault(self, originalTag);
        }
        return originalTag;
    }

    public static int getEquipmentLevel(Enchantment enchantment, LivingEntity living, boolean useModifications) {
        Iterable<ItemStack> iterable = enchantment.getEquipment(living).values();
        int i = 0;

        for(ItemStack itemStack : iterable) {
            int j = getLevel(living, enchantment, itemStack, useModifications);
            if (j > i) {
                i = j;
            }
        }

        return i;
    }

    public static Map<Enchantment, Integer> get(ItemStack self, boolean useModifications) {
        if (useModifications) {
            Entity entity = ((EntityLinkedItemStack) self).apoli$getEntity();
            if (entity instanceof LivingEntity living && ENTITY_ITEM_ENCHANTS.containsKey(living)) {
                ConcurrentHashMap<ItemStack, NbtList> itemEnchants = ENTITY_ITEM_ENCHANTS.get(entity);
                return EnchantmentHelper.fromNbt(itemEnchants.computeIfAbsent(self, ItemStack::getEnchantments));
            }
        }
        return EnchantmentHelper.get(self);
    }

    public static int getLevel(Enchantment enchantment, ItemStack self, boolean useModifications) {
        return getLevel(null, enchantment, self, useModifications);
    }

    public static int getLevel(LivingEntity entity, Enchantment enchantment, ItemStack self, boolean useModifications) {
        if (!useModifications) {
            Identifier id = Registries.ENCHANTMENT.getId(enchantment);
            Optional<Integer> idx = findEnchantIndex(id, self.getEnchantments());
            if(idx.isPresent()) {
                NbtCompound existingEnchant = self.getEnchantments().getCompound(idx.get());
                return EnchantmentHelper.getLevelFromNbt(existingEnchant);
            }
            return 0;
        }

        Entity nullsafeEntity = entity == null ? ((EntityLinkedItemStack) self).apoli$getEntity() : entity;
        if (nullsafeEntity instanceof LivingEntity living && ENTITY_ITEM_ENCHANTS.containsKey(living)) {
            ConcurrentHashMap<ItemStack, NbtList> itemEnchants = ENTITY_ITEM_ENCHANTS.get(living);
            Identifier id = Registries.ENCHANTMENT.getId(enchantment);
            NbtList newEnchants = itemEnchants.computeIfAbsent(self, ItemStack::getEnchantments);
            Optional<Integer> idx = findEnchantIndex(id, newEnchants);
            if(idx.isPresent()) {
                NbtCompound existingEnchant = newEnchants.getCompound(idx.get());
                return EnchantmentHelper.getLevelFromNbt(existingEnchant);
            }
            return 0;
        }
        return EnchantmentHelper.getLevel(enchantment, self);
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
        return new PowerFactory<>(Apoli.identifier("modify_enchantment_level"),
                new SerializableData()
                        .add("enchantment", SerializableDataTypes.ENCHANTMENT)
                        .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                        .add("modifier", Modifier.DATA_TYPE, null)
                        .add("modifiers", Modifier.LIST_TYPE, null),
                data ->
                        (type, player) -> {
            ModifyEnchantmentLevelPower power = new ModifyEnchantmentLevelPower(type, player, data.get("enchantment"), data.get("item_condition"));
            data.ifPresent("modifier", power::addModifier);
            data.<List<Modifier>>ifPresent("modifiers",
                    mods -> mods.forEach(power::addModifier)
            );
            return power;
        }).allowCondition();
    }

}
