package io.github.apace100.apoli.action.type.entity;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;

import java.util.List;
import java.util.Optional;

public class DamageEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<DamageEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("damage_type", SerializableDataTypes.DAMAGE_TYPE)
            .add("amount", SerializableDataTypes.FLOAT.optional(), Optional.empty())
            .add("modifier", Modifier.DATA_TYPE.optional(), Optional.empty())
            .add("modifiers", Modifier.LIST_TYPE.optional(), Optional.empty())
            .validate(data -> {

                Optional<Float> amount = data.get("amount");

                Optional<Modifier> modifier = data.get("modifier");
                Optional<List<Modifier>> modifiers = data.get("modifiers");

                if (amount.isPresent() || modifier.isPresent() || modifiers.isPresent()) {
                    return DataResult.success(data);
                }

                else {
                    return DataResult.error(() -> "Any of 'amount', 'modifier', and 'modifiers' fields must be defined!");
                }

            }),
        data -> new DamageEntityActionType(
            data.get("damage_type"),
            data.get("amount"),
            data.get("modifier"),
            data.get("modifiers")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("damage_type", actionType.damageType)
            .set("amount", actionType.amount)
            .set("modifier", actionType.modifier)
            .set("modifiers", actionType.modifiers)
    );

    private final RegistryKey<DamageType> damageType;
    private final Optional<Float> amount;

    private final Optional<Modifier> modifier;
    private final Optional<List<Modifier>> modifiers;

    private final List<Modifier> allModifiers;

    public DamageEntityActionType(RegistryKey<DamageType> damageType, Optional<Float> amount, Optional<Modifier> modifier, Optional<List<Modifier>> modifiers) {

        this.damageType = damageType;
        this.amount = amount;

        this.modifier = modifier;
        this.modifiers = modifiers;

        this.allModifiers = new ObjectArrayList<>();

        this.modifier.ifPresent(this.allModifiers::add);
        this.modifiers.ifPresent(this.allModifiers::addAll);

    }

    @Override
    protected void execute(Entity entity) {
        this.amount
            .or(() -> getModifiedAmount(entity))
            .ifPresent(amount -> entity.damage(entity.getDamageSources().create(damageType), amount));
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.DAMAGE;
    }

    private Optional<Float> getModifiedAmount(Entity entity) {
        return !allModifiers.isEmpty() && entity instanceof LivingEntity livingEntity
            ? Optional.of((float) ModifierUtil.applyModifiers(entity, allModifiers, livingEntity.getMaxHealth()))
            : Optional.empty();
    }

}
