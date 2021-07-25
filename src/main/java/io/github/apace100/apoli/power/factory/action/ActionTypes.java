package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class ActionTypes {

    public static ActionType<Entity> ENTITY = new ActionType<>("EntityAction", ApoliRegistries.ENTITY_ACTION);
    public static ActionType<Pair<World, ItemStack>> ITEM = new ActionType<>("ItemAction", ApoliRegistries.ITEM_ACTION);
    public static ActionType<Triple<World, BlockPos, Direction>> BLOCK = new ActionType<>("BlockAction", ApoliRegistries.BLOCK_ACTION);
    public static ActionType<Pair<Entity, Entity>> BIENTITY = new ActionType<>("BiEntityAction", ApoliRegistries.BIENTITY_ACTION);

}
