package io.github.apace100.apoli.power.factory.action;


import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.bientity.*;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;

public class BiEntityActions {

    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.BIENTITY_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.BIENTITY_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.BIENTITY_ACTION, ApoliDataTypes.BIENTITY_CONDITION));
        register(ChoiceAction.getFactory(ApoliDataTypes.BIENTITY_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.BIENTITY_ACTION, ApoliDataTypes.BIENTITY_CONDITION));
        register(DelayAction.getFactory(ApoliDataTypes.BIENTITY_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.BIENTITY_ACTION, entities -> !entities.getLeft().getWorld().isClient));

        register(InvertAction.getFactory());
        register(ActorAction.getFactory());
        register(TargetAction.getFactory());
        register(MountAction.getFactory());
        register(SetInLoveAction.getFactory());
        register(TameAction.getFactory());
        register(AddVelocityAction.getFactory());
        register(DamageAction.getFactory());
        register(AddToSetAction.getFactory());
        register(RemoveFromSetAction.getFactory());
    }

    private static void register(ActionFactory<Pair<Entity, Entity>> actionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
