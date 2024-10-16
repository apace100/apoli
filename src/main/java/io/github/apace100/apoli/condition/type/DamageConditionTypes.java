package io.github.apace100.apoli.condition.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.type.damage.*;
import io.github.apace100.apoli.condition.type.damage.meta.AllOfDamageConditionType;
import io.github.apace100.apoli.condition.type.damage.meta.AnyOfDamageConditionType;
import io.github.apace100.apoli.condition.type.damage.meta.ConstantDamageConditionType;
import io.github.apace100.apoli.condition.type.damage.meta.RandomChanceDamageConditionType;
import io.github.apace100.apoli.condition.type.meta.AllOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.AnyOfMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.ConstantMetaConditionType;
import io.github.apace100.apoli.condition.type.meta.RandomChanceMetaConditionType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.registry.Registry;

public class DamageConditionTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();
    public static final SerializableDataType<ConditionConfiguration<DamageConditionType>> DATA_TYPE = SerializableDataType.registry(ApoliRegistries.DAMAGE_CONDITION_TYPE, Apoli.MODID, ALIASES, (configurations, id) -> "Damage condition type \"" + id + "\" is undefined!");

    public static final ConditionConfiguration<AllOfDamageConditionType> ALL_OF = register(AllOfMetaConditionType.createConfiguration(DamageCondition.DATA_TYPE, AllOfDamageConditionType::new));
    public static final ConditionConfiguration<AnyOfDamageConditionType> ANY_OF = register(AnyOfMetaConditionType.createConfiguration(DamageCondition.DATA_TYPE, AnyOfDamageConditionType::new));
    public static final ConditionConfiguration<ConstantDamageConditionType> CONSTANT = register(ConstantMetaConditionType.createConfiguration(ConstantDamageConditionType::new));
    public static final ConditionConfiguration<RandomChanceDamageConditionType> RANDOM_CHANCE = register(RandomChanceMetaConditionType.createConfiguration(RandomChanceDamageConditionType::new));

    public static final ConditionConfiguration<AmountDamageConditionType> AMOUNT  = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("amount"), AmountDamageConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<AttackerDamageConditionType> ATTACKER = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("attacker"), AttackerDamageConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<BypassesArmorDamageConditionType> BYPASSES_ARMOR = register(ConditionConfiguration.simple(Apoli.identifier("bypasses_armor"), BypassesArmorDamageConditionType::new));
    public static final ConditionConfiguration<ExplosiveDamageConditionType> EXPLOSIVE = register(ConditionConfiguration.simple(Apoli.identifier("explosive"), ExplosiveDamageConditionType::new));
    public static final ConditionConfiguration<FireDamageConditionType> FIRE = register(ConditionConfiguration.simple(Apoli.identifier("fire"), FireDamageConditionType::new));
    public static final ConditionConfiguration<FromFallingDamageConditionType> FROM_FALLING = register(ConditionConfiguration.simple(Apoli.identifier("from_falling"), FromFallingDamageConditionType::new));
    public static final ConditionConfiguration<InTagDamageConditionType> IN_TAG = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("in_tag"), InTagDamageConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<NameDamageConditionType> NAME = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("name"), NameDamageConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<OutOfWorldDamageConditionType> OUT_OF_WORLD = register(ConditionConfiguration.simple(Apoli.identifier("out_of_world"), OutOfWorldDamageConditionType::new));
    public static final ConditionConfiguration<ProjectileDamageConditionType> PROJECTILE = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("projectile"), ProjectileDamageConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<TypeDamageConditionType> TYPE = register(ConditionConfiguration.fromDataFactory(Apoli.identifier("type"), TypeDamageConditionType.DATA_FACTORY));
    public static final ConditionConfiguration<UnblockableDamageConditionType> UNBLOCKABLE = register(ConditionConfiguration.simple(Apoli.identifier("unblockable"), UnblockableDamageConditionType::new));

    public static void register() {

    }

    @SuppressWarnings("unchecked")
	public static <CT extends DamageConditionType> ConditionConfiguration<CT> register(ConditionConfiguration<CT> configuration) {

        ConditionConfiguration<DamageConditionType> casted = (ConditionConfiguration<DamageConditionType>) configuration;
        Registry.register(ApoliRegistries.DAMAGE_CONDITION_TYPE, casted.id(), casted);

        return configuration;

    }

}
