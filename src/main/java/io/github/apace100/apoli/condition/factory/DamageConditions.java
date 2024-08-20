package io.github.apace100.apoli.condition.factory;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.type.damage.*;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Pair;

public class DamageConditions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static final ConditionTypeFactory<Pair<DamageSource, Float>> AMOUNT = register(AmountConditionType.getFactory());

    public static void register() {
        MetaConditions.register(ApoliDataTypes.DAMAGE_CONDITION, DamageConditions::register);
        register(NameConditionType.getFactory());
        register(ProjectileConditionType.getFactory());
        register(AttackerConditionType.getFactory());

        //region  FIXME: These are deprecated. Remove them in the future -eggohito
        register(InTagConditionType.createFactory(Apoli.identifier("fire"), DamageTypeTags.IS_FIRE));
        register(InTagConditionType.createFactory(Apoli.identifier("bypasses_armor"), DamageTypeTags.BYPASSES_ARMOR));
        register(InTagConditionType.createFactory(Apoli.identifier("explosive"), DamageTypeTags.IS_EXPLOSION));
        register(InTagConditionType.createFactory(Apoli.identifier("from_falling"), DamageTypeTags.IS_FALL));
        register(InTagConditionType.createFactory(Apoli.identifier("unblockable"), DamageTypeTags.BYPASSES_SHIELD));
        register(InTagConditionType.createFactory(Apoli.identifier("out_of_world"), DamageTypeTags.BYPASSES_INVULNERABILITY));
        //endregion

        register(InTagConditionType.getFactory());
        register(TypeConditionType.getFactory());

    }

    public static <F extends ConditionTypeFactory<Pair<DamageSource, Float>>> F register(F conditionFactory) {
        return Registry.register(ApoliRegistries.DAMAGE_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }

}
