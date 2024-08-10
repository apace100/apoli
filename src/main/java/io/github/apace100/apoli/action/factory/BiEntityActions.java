package io.github.apace100.apoli.action.factory;


import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.type.bientity.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class BiEntityActions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static void register() {

        MetaActions.register(ApoliDataTypes.BIENTITY_ACTION, ApoliDataTypes.BIENTITY_CONDITION, Function.identity(), BiEntityActions::register);

        register(InvertActionType.getFactory());
        register(ActorActionType.getFactory());
        register(TargetActionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("mount"), MountActionType::action));
        register(createSimpleFactory(Apoli.identifier("set_in_love"), SetInLoveActionType::action));
        register(createSimpleFactory(Apoli.identifier("tame"), TameActionType::action));
        register(AddVelocityActionType.getFactory());
        register(DamageActionType.getFactory());
        register(AddToEntitySetActionType.getFactory());
        register(RemoveFromEntitySetActionType.getFactory());
        register(createSimpleFactory(Apoli.identifier("leash"), LeashActionType::action));

    }

    public static ActionTypeFactory<Pair<Entity, Entity>> createSimpleFactory(Identifier id, BiConsumer<Entity, Entity> action) {
        return new ActionTypeFactory<>(id, new SerializableData(), (data, actorAndTarget) -> action.accept(actorAndTarget.getLeft(), actorAndTarget.getRight()));
    }

    public static <F extends ActionTypeFactory<Pair<Entity, Entity>>> F register(F actionFactory) {
        return Registry.register(ApoliRegistries.BIENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }

}
