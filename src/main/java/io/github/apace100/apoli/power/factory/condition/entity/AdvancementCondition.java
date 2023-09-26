package io.github.apace100.apoli.power.factory.condition.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.mixin.ClientAdvancementManagerAccessor;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;

public class AdvancementCondition {

    public static boolean condition(SerializableData.Instance data, Entity entity) {

        if (!(entity instanceof PlayerEntity playerEntity)) {
            return false;
        }

        MinecraftServer server = playerEntity.getServer();
        Identifier advancementId = data.get("advancement");

        if (server != null) {

            AdvancementEntry advancementEntry = server.getAdvancementLoader().get(advancementId);
            if (advancementEntry == null) {
                Apoli.LOGGER.warn("Advancement \"{}\" did not exist, but was referenced in an \"advancement\" entity condition!", advancementId);
                return false;
            }

            return ((ServerPlayerEntity) playerEntity)
                .getAdvancementTracker()
                .getProgress(advancementEntry)
                .isDone();

        }

        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) {
            return false;
        }

        ClientAdvancementManager manager = networkHandler.getAdvancementHandler();
        AdvancementEntry advancementEntry = manager.get(advancementId);

        if (advancementEntry == null) {
            //  We don't want to print an error here if the advancement does not exist,
            //  because o the client-side, the advancement could just not have been received from the server
            return false;
        }

        Map<Advancement, AdvancementProgress> progressMap = ((ClientAdvancementManagerAccessor) manager).getAdvancementProgresses();
        Advancement advancement = advancementEntry.value();

        if (progressMap.containsKey(advancement)) {
            return progressMap.get(advancement).isDone();
        }

        return false;

    }

    public static ConditionFactory<Entity> getFactory() {
        return new ConditionFactory<>(
            Apoli.identifier("advancement"),
            new SerializableData()
                .add("advancement", SerializableDataTypes.IDENTIFIER),
            AdvancementCondition::condition
        );
    }

}
