package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.block.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;

public class BlockActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.BLOCK_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.BLOCK_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.BLOCK_ACTION, ApoliDataTypes.BLOCK_CONDITION,
            t -> new CachedBlockPosition(t.getLeft(), t.getMiddle(), true)));
        register(ChoiceAction.getFactory(ApoliDataTypes.BLOCK_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.BLOCK_ACTION, ApoliDataTypes.BLOCK_CONDITION,
            t -> new CachedBlockPosition(t.getLeft(), t.getMiddle(), true)));
        register(DelayAction.getFactory(ApoliDataTypes.BLOCK_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.BLOCK_ACTION, block -> !block.getLeft().isClient));

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
                BlockState actualState = data.get("block");
                //actualState = Block.postProcessState(actualState, block.getLeft(), block.getMiddle());
                block.getLeft().setBlockState(block.getMiddle(), actualState);
            }));
        register(new ActionFactory<>(Apoli.identifier("add_block"), new SerializableData()
            .add("block", SerializableDataTypes.BLOCK_STATE),
            (data, block) -> {
                BlockState actualState = data.get("block");
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
                        Text.translatable(blockName),
                        server,
                        null);
                    server.getCommandManager().executeWithPrefix(source, data.getString("command"));
                }
            }));
        register(BonemealAction.getFactory());
        register(ModifyBlockStateAction.getFactory());
        register(ExplodeAction.getFactory());
        register(AreaOfEffectAction.getFactory());
        register(SpawnEntityAction.getFactory());
    }

    private static void register(ActionFactory<Triple<World, BlockPos, Direction>> actionFactory) {
        Registry.register(ApoliRegistries.BLOCK_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
