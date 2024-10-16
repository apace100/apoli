package io.github.apace100.apoli.action.type.bientity;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.BiEntityActionType;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactory;
import io.github.apace100.calio.registry.SimpleDataObjectFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;

import java.util.List;
import java.util.Optional;

public class DamageBiEntityActionType extends BiEntityActionType {

    public static final DataObjectFactory<DamageBiEntityActionType> DATA_FACTORY = new SimpleDataObjectFactory<>(
        new SerializableData()
            .add("damage_type", SerializableDataTypes.DAMAGE_TYPE)
            .add("amount", SerializableDataTypes.FLOAT.optional(), Optional.empty())
            .add("modifier", Modifier.DATA_TYPE.optional(), Optional.empty())
            .addFunctionedDefault("modifiers", Modifier.LIST_TYPE, data -> data.<Optional<Modifier>>get("modifier").map(List::of).orElseGet(List::of))
            .validate(data -> {

                if (data.isPresent("amount") || MiscUtil.anyPresent(data, "modifier", "modifiers")) {
                    return DataResult.success(data);
                }

                else {
                    return DataResult.error(() -> "Any of 'amount', 'modifier' and 'modifiers' fields must be defined!");
                }

            }),
        data -> new DamageBiEntityActionType(
            data.get("damage_type"),
            data.get("amount"),
            data.get("modifiers")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("damage_type", actionType.damageType)
            .set("amount", actionType.amount)
            .set("modifiers", actionType.modifiers)
    );

    private final RegistryKey<DamageType> damageType;
    private final Optional<Float> amount;

    private final List<Modifier> modifiers;

    public DamageBiEntityActionType(RegistryKey<DamageType> damageType, Optional<Float> amount, List<Modifier> modifiers) {
        this.damageType = damageType;
        this.amount = amount;
        this.modifiers = modifiers;
    }

    @Override
	protected void execute(Entity actor, Entity target) {

        if (actor != null && target != null) {
            this.amount
                .or(() -> getModifiedAmount(actor, target))
                .ifPresent(amount -> target.damage(actor.getDamageSources().create(damageType, actor), amount));
        }

    }

    @Override
    public ActionConfiguration<?> configuration() {
        return BiEntityActionTypes.DAMAGE;
    }

    private Optional<Float> getModifiedAmount(Entity actor, Entity target) {
        return !modifiers.isEmpty() && target instanceof LivingEntity livingTarget
            ? Optional.of((float) ModifierUtil.applyModifiers(actor, modifiers, livingTarget.getMaxHealth()))
            : Optional.empty();
    }

}
