package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MutableItemStack;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.FilterableWeightedList;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class ItemActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ActionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("actions", ApoliDataTypes.ITEM_ACTIONS),
            (data, worldAndStack) -> ((List<ActionFactory<Pair<World, ItemStack>>.Instance>)data.get("actions")).forEach((e) -> e.accept(worldAndStack))));
        register(new ActionFactory<>(Apoli.identifier("chance"), new SerializableData()
            .add("action", ApoliDataTypes.ITEM_ACTION)
            .add("chance", SerializableDataTypes.FLOAT),
            (data, worldAndStack) -> {
                if(new Random().nextFloat() < data.getFloat("chance")) {
                    ((ActionFactory<Pair<World, ItemStack>>.Instance)data.get("action")).accept(worldAndStack);
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("if_else"), new SerializableData()
            .add("condition", ApoliDataTypes.ITEM_CONDITION)
            .add("if_action", ApoliDataTypes.ITEM_ACTION)
            .add("else_action", ApoliDataTypes.ITEM_ACTION, null),
            (data, worldAndStack) -> {
                if(((ConditionFactory<ItemStack>.Instance)data.get("condition")).test(worldAndStack.getRight())) {
                    ((ActionFactory<Pair<World, ItemStack>>.Instance)data.get("if_action")).accept(worldAndStack);
                } else {
                    if(data.isPresent("else_action")) {
                        ((ActionFactory<Pair<World, ItemStack>>.Instance)data.get("else_action")).accept(worldAndStack);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("choice"), new SerializableData()
            .add("actions", SerializableDataType.weightedList(ApoliDataTypes.ITEM_ACTION)),
            (data, worldAndStack) -> {
                FilterableWeightedList<ActionFactory<Pair<World, ItemStack>>.Instance> actionList = data.get("actions");
                ActionFactory<Pair<World, ItemStack>>.Instance action = actionList.pickRandom(new Random());
                action.accept(worldAndStack);
            }));

        register(new ActionFactory<>(Apoli.identifier("consume"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1),
            (data, worldAndStack) -> {
                worldAndStack.getRight().decrement(data.getInt("amount"));
            }));
        register(new ActionFactory<>(Apoli.identifier("modify"), new SerializableData()
            .add("modifier", SerializableDataTypes.IDENTIFIER),
            (data, worldAndStack) -> {
                MinecraftServer server = worldAndStack.getLeft().getServer();
                if(server != null) {
                    Identifier id = data.getId("modifier");
                    LootFunctionManager lootFunctionManager = server.getItemModifierManager();
                    LootFunction lootFunction = lootFunctionManager.get(id);
                    if (lootFunction == null) {
                        Apoli.LOGGER.info("Unknown item modifier used in `modify` action: " + id);
                        return;
                    }
                    ServerWorld serverWorld = server.getOverworld();
                    ItemStack stack = worldAndStack.getRight();
                    LootContext.Builder builder = (new LootContext.Builder(serverWorld)).parameter(LootContextParameters.ORIGIN, new Vec3d(0, 0, 0));
                    ItemStack newStack = lootFunction.apply(stack, builder.build(LootContextTypes.COMMAND));
                    ((MutableItemStack)stack).setFrom(newStack);
                }
            }));
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
    }

    private static void register(ActionFactory<Pair<World, ItemStack>> actionFactory) {
        Registry.register(ApoliRegistries.ITEM_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
