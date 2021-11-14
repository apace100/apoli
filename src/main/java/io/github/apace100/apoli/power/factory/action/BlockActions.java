package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.block.ExplodeAction;
import io.github.apace100.apoli.power.factory.action.block.ModifyBlockStateAction;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.FilterableWeightedList;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Random;

public class BlockActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ActionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("actions", ApoliDataTypes.BLOCK_ACTIONS),
            (data, block) -> ((List<ActionFactory<Triple<World, BlockPos, Direction>>.Instance>)data.get("actions")).forEach((e) -> e.accept(block))));
        register(new ActionFactory<>(Apoli.identifier("chance"), new SerializableData()
            .add("action", ApoliDataTypes.BLOCK_ACTION)
            .add("chance", SerializableDataTypes.FLOAT),
            (data, block) -> {
                if(new Random().nextFloat() < data.getFloat("chance")) {
                    ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("action")).accept(block);
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("if_else"), new SerializableData()
            .add("condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("if_action", ApoliDataTypes.BLOCK_ACTION)
            .add("else_action", ApoliDataTypes.BLOCK_ACTION, null),
            (data, block) -> {
                if(((ConditionFactory<CachedBlockPosition>.Instance)data.get("condition")).test(new CachedBlockPosition(block.getLeft(), block.getMiddle(), true))) {
                    ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("if_action")).accept(block);
                } else {
                    if(data.isPresent("else_action")) {
                        ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("else_action")).accept(block);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("choice"), new SerializableData()
            .add("actions", SerializableDataType.weightedList(ApoliDataTypes.BLOCK_ACTION)),
            (data, block) -> {
                FilterableWeightedList<ActionFactory<Triple<World, BlockPos, Direction>>.Instance> actionList = (FilterableWeightedList<ActionFactory<Triple<World, BlockPos, Direction>>.Instance>)data.get("actions");
                ActionFactory<Triple<World, BlockPos, Direction>>.Instance action = actionList.pickRandom(new Random());
                action.accept(block);
            }));
        register(new ActionFactory<>(Apoli.identifier("offset"), new SerializableData()
            .add("action", ApoliDataTypes.BLOCK_ACTION)
            .add("x", SerializableDataTypes.INT, 0)
            .add("y", SerializableDataTypes.INT, 0)
            .add("z", SerializableDataTypes.INT, 0),
            (data, block) -> ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("action")).accept(Triple.of(
                block.getLeft(),
                block.getMiddle().add(data.getInt("x"), data.getInt("y"), data.getInt("z")),
                block.getRight())
            )));

        register(new ActionFactory<>(Apoli.identifier("set_block"), new SerializableData()
            .add("block", SerializableDataTypes.BLOCK_STATE),
            (data, block) -> {
                BlockState actualState = (BlockState)data.get("block");
                //actualState = Block.postProcessState(actualState, block.getLeft(), block.getMiddle());
                block.getLeft().setBlockState(block.getMiddle(), actualState);
            }));
        register(new ActionFactory<>(Apoli.identifier("add_block"), new SerializableData()
            .add("block", SerializableDataTypes.BLOCK_STATE),
            (data, block) -> {
                BlockState actualState = (BlockState)data.get("block");
                BlockPos pos = block.getMiddle().offset(block.getRight());
                //actualState = Block.postProcessState(actualState, block.getLeft(), pos);
                block.getLeft().setBlockState(pos, actualState);
            }));
        register(new ActionFactory<>(Apoli.identifier("execute_command"), new SerializableData()
            .add("command", SerializableDataTypes.STRING),
            (data, block) -> {
                MinecraftServer server = block.getLeft().getServer();
                if(server != null) {
                    String blockName = block.getLeft().getBlockState(block.getMiddle()).getBlock().getTranslationKey();
                    ServerCommandSource source = new ServerCommandSource(
                        Apoli.config.executeCommand.showOutput ? server : CommandOutput.DUMMY,
                        new Vec3d(block.getMiddle().getX() + 0.5, block.getMiddle().getY() + 0.5, block.getMiddle().getZ() + 0.5),
                        new Vec2f(0, 0),
                        (ServerWorld)block.getLeft(),
                        Apoli.config.executeCommand.permissionLevel,
                        blockName,
                        new TranslatableText(blockName),
                        server,
                        null);
                    server.getCommandManager().execute(source, data.getString("command"));
                }
            }));
        register(ModifyBlockStateAction.getFactory());
        register(ExplodeAction.getFactory());
    }

    private static void register(ActionFactory<Triple<World, BlockPos, Direction>> actionFactory) {
        Registry.register(ApoliRegistries.BLOCK_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
