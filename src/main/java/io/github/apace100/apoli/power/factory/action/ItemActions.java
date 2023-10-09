package io.github.apace100.apoli.power.factory.action;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.item.HolderAction;
import io.github.apace100.apoli.power.factory.action.item.ModifyAction;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ItemActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.ITEM_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION,
            worldItemStackPair -> worldItemStackPair));
        register(ChoiceAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.ITEM_ACTION, ApoliDataTypes.ITEM_CONDITION,
            worldItemStackPair -> worldItemStackPair));
        register(DelayAction.getFactory(ApoliDataTypes.ITEM_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.ITEM_ACTION, worldAndStack -> !worldAndStack.getLeft().isClient));

        register(new ActionFactory<>(Apoli.identifier("consume"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1),
            (data, worldAndStack) -> {
                worldAndStack.getRight().decrement(data.getInt("amount"));
            }));
        register(ModifyAction.getFactory());
        register(new ActionFactory<>(Apoli.identifier("damage"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1)
            .add("ignore_unbreaking", SerializableDataTypes.BOOLEAN, false),
            (data, worldAndStack) -> {
                if (worldAndStack.getRight().isDamageable()) {
                    int amount = data.getInt("amount");
                    int i;
                    if (amount > 0 && !data.getBoolean("ignore_unbreaking")) {
                        i = EnchantmentHelper.getLevel(Enchantments.UNBREAKING, worldAndStack.getRight());
                        int j = 0;

                        for(int k = 0; i > 0 && k < amount; ++k) {
                            if (UnbreakingEnchantment.shouldPreventDamage(worldAndStack.getRight(), i, worldAndStack.getLeft().random)) {
                                ++j;
                            }
                        }

                        amount -= j;
                        if (amount <= 0) {
                            return;
                        }
                    }

                    i = worldAndStack.getRight().getDamage() + amount;
                    worldAndStack.getRight().setDamage(i);
                    if(i >= worldAndStack.getRight().getMaxDamage()) {
                        worldAndStack.getRight().decrement(1);
                        worldAndStack.getRight().setDamage(0);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("merge_nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.STRING),
            (data, worldAndStack) -> {
                String nbtString = data.get("nbt");
                try {
                    NbtCompound nbt = new StringNbtReader(new StringReader(nbtString)).parseCompound();
                    worldAndStack.getRight().getOrCreateNbt().copyFrom(nbt);
                } catch (CommandSyntaxException e) {
                    Apoli.LOGGER.error("Failed `merge_nbt` item action due to malformed nbt string: \"" + nbtString + "\"");
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("remove_enchantment"), new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT, null)
            .add("enchantments", SerializableDataType.list(SerializableDataTypes.ENCHANTMENT), null)
            .add("levels", SerializableDataTypes.INT, null)
            .add("reset_repair_cost", SerializableDataTypes.BOOLEAN, false),
            (data, worldAndStack) -> {
                ItemStack stack = worldAndStack.getRight();
                if(!stack.hasNbt()) {
                    return;
                }
                List<Enchantment> enchs = new LinkedList<>();
                data.<Enchantment>ifPresent("enchantment", enchs::add);
                data.<List<Enchantment>>ifPresent("enchantments", enchs::addAll);
                int levels = -1;
                if(data.isPresent("levels")) {
                    levels = data.getInt("levels");
                }
                Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
                if(enchs.size() > 0) {
                    for(Enchantment ench : enchs) {
                        if(enchants.containsKey(ench)) {
                            int newLevel = levels == -1 ? 0 : enchants.get(ench) - data.getInt("levels");
                            if(newLevel <= 0) {
                                enchants.remove(ench);
                            } else {
                                enchants.put(ench, newLevel);
                            }
                        }
                    }
                } else {
                    Map<Enchantment, Integer> newEnchants = new LinkedHashMap<>();
                    for(Enchantment e : enchants.keySet()) {
                        int newLevel = levels == -1 ? 0 : enchants.get(e) - data.getInt("levels");
                        if(newLevel > 0) {
                            newEnchants.put(e, newLevel);
                        }
                    }
                    enchants = newEnchants;
                }
                EnchantmentHelper.set(enchants, stack);
                if(data.getBoolean("reset_repair_cost") && !stack.hasEnchantments()) {
                    stack.setRepairCost(0);
                }
            }));
        register(HolderAction.getFactory());
    }

    private static void register(ActionFactory<Pair<World, ItemStack>> actionFactory) {
        Registry.register(ApoliRegistries.ITEM_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
