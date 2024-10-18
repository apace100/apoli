package io.github.apace100.apoli.condition.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.EntityConditionType;
import io.github.apace100.apoli.condition.type.EntityConditionTypes;
import io.github.apace100.apoli.mixin.ClientAdvancementManagerAccessor;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;

public class AdvancementEntityConditionType extends EntityConditionType {

    public static final DataObjectFactory<AdvancementEntityConditionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("advancement", SerializableDataTypes.IDENTIFIER),
        data -> new AdvancementEntityConditionType(
            data.get("advancement")
        ),
        (conditionType, serializableData) -> serializableData.instance()
            .set("advancement", conditionType.advancement)
    );

    private final Identifier advancement;

    public AdvancementEntityConditionType(Identifier advancement) {
        this.advancement = advancement;
    }

    @Override
    public boolean test(Entity entity) {

        if (!(entity instanceof PlayerEntity player)) {
            return false;
        }

        MinecraftServer server = player.getServer();
        if (server != null) {

            AdvancementEntry advancementEntry = server.getAdvancementLoader().get(advancement);
            if (advancementEntry == null) {
                //  TODO: Throw an exception and pass it to the factory instance to be caught instead -eggohito
                Apoli.LOGGER.warn("Advancement \"{}\" did not exist, but was referenced in an \"advancement\" entity condition!", advancement);
                return false;
            }

            else {
                return ((ServerPlayerEntity) player).getAdvancementTracker()
                    .getProgress(advancementEntry)
                    .isDone();
            }

        }

        else if (player instanceof ClientPlayerEntity clientPlayer && clientPlayer.networkHandler != null) {

            ClientAdvancementManager advancementManager = clientPlayer.networkHandler.getAdvancementHandler();
            AdvancementEntry advancement = advancementManager.get(this.advancement);

            if (advancement == null) {
                //  We don't want to print an error here if the advancement does not exist,
                //  because on the client-side, the advancement could just not have been received from the server
                return false;
            }

            Map<AdvancementEntry, AdvancementProgress> progresses = ((ClientAdvancementManagerAccessor) advancementManager).getAdvancementProgresses();
            AdvancementProgress progress = progresses.get(advancement);

            return progress != null
                && progress.isDone();

        }

        else {
            return false;
        }

    }

    @Override
    public ConditionConfiguration<?> configuration() {
        return EntityConditionTypes.ADVANCEMENT;
    }

}
