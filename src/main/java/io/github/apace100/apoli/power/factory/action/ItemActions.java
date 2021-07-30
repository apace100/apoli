package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ItemActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ActionFactory<>(Apoli.identifier("consume"), new SerializableData()
            .add("amount", SerializableDataTypes.INT, 1),
            (data, worldAndStack) -> {
                worldAndStack.getRight().decrement(data.getInt("amount"));
            }));
        register(new ActionFactory<>(Apoli.identifier("modify"), new SerializableData()
            .add("modifier", SerializableDataTypes.IDENTIFIER),
            (data, worldAndStack) -> {
                if(!worldAndStack.getLeft().isClient) {
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
                        LootContext.Builder builder = (new LootContext.Builder(serverWorld)).parameter(LootContextParameters.ORIGIN, new Vec3d(0, 0, 0));
                        lootFunction.apply(worldAndStack.getRight(), builder.build(LootContextTypes.COMMAND));
                    }
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
