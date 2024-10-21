package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.AdvancementUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.command.AdvancementCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GrantAdvancementEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<GrantAdvancementEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("advancement", SerializableDataTypes.IDENTIFIER)
            .add("selection", ApoliDataTypes.ADVANCEMENT_SELECTION, AdvancementCommand.Selection.ONLY)
            .add("criterion", SerializableDataTypes.STRING.optional(), Optional.empty())
            .add("criteria", SerializableDataTypes.STRINGS.optional(), Optional.empty()),
        data -> new GrantAdvancementEntityActionType(
            data.get("advancement"),
            data.get("selection"),
            data.get("criterion"),
            data.get("criteria")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("advancement", actionType.advancementId)
            .set("selection", actionType.selection)
            .set("criterion", actionType.criterion)
            .set("criteria", actionType.criteria)
    );

    private final Identifier advancementId;
    private final AdvancementCommand.Selection selection;

    private final Optional<String> criterion;
    private final Optional<List<String>> criteria;

    private final Set<String> allCriteria;

    public GrantAdvancementEntityActionType(Identifier advancementId, AdvancementCommand.Selection selection, Optional<String> criterion, Optional<List<String>> criteria) {

        this.advancementId = advancementId;
        this.selection = selection;

        this.criterion = criterion;
        this.criteria = criteria;

        this.allCriteria = new ObjectOpenHashSet<>();

        this.criterion.ifPresent(this.allCriteria::add);
        this.criteria.ifPresent(this.allCriteria::addAll);

    }

    @Override
    protected void execute(Entity entity) {

        MinecraftServer server = entity.getServer();
        if (server == null || !(entity instanceof ServerPlayerEntity serverPlayerEntity)) {
            return;
        }

        ServerAdvancementLoader advancementLoader = server.getAdvancementLoader();
        if (selection == AdvancementCommand.Selection.EVERYTHING) {
            AdvancementUtil.processAdvancements(advancementLoader.getAdvancements(), AdvancementCommand.Operation.GRANT, serverPlayerEntity);
        }

        else if (advancementId != null) {

            AdvancementEntry advancementEntry = advancementLoader.get(advancementId);
            if (advancementEntry == null) {
                Apoli.LOGGER.warn("Unknown advancement (\"" + advancementId + "\") referenced in `grant_advancement` entity action type!");
            }

            else if (allCriteria.isEmpty()) {
                AdvancementUtil.processAdvancements(AdvancementUtil.selectEntries(server.getAdvancementLoader().getManager(), advancementEntry, selection), AdvancementCommand.Operation.GRANT, serverPlayerEntity);
            }

            else {
                AdvancementUtil.processCriteria(advancementEntry, allCriteria, AdvancementCommand.Operation.GRANT, serverPlayerEntity);
            }

        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.GRANT_ADVANCEMENT;
    }

}
