package io.github.apace100.apoli.action.type.bientity;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;

public class DamageActionType {

    public static void action(Entity actor, Entity target, RegistryKey<DamageType> damageTypeKey, @Nullable Float amount, Collection<Modifier> modifiers) {

        if (actor == null || target == null) {
            return;
        }

        if (!modifiers.isEmpty() && target instanceof LivingEntity livingTarget) {
            amount = (float) ModifierUtil.applyModifiers(actor, modifiers, livingTarget.getMaxHealth());
        }

        if (amount != null) {
            target.damage(actor.getDamageSources().create(damageTypeKey, actor), amount);
        }

    }

    public static ActionTypeFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("damage"),
            new SerializableData()
                .add("damage_type", SerializableDataTypes.DAMAGE_TYPE)
                .add("amount", SerializableDataTypes.FLOAT, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .validate(data -> {

                    if (!data.isPresent("amount") && !MiscUtil.anyPresent(data, "modifier", "modifiers")) {
                        return DataResult.error(() -> "Any of 'amount', 'modifier', or 'modifiers' fields must be defined!");
                    }

                    else {
                        return DataResult.success(data);
                    }

                }),
            (data, actorAndTarget) -> {

                Collection<Modifier> modifiers = new LinkedList<>();

                data.ifPresent("modifier", modifiers::add);
                data.ifPresent("modifiers", modifiers::addAll);

                action(actorAndTarget.getLeft(), actorAndTarget.getRight(),
                    data.get("damage_type"),
                    data.get("amount"),
                    modifiers
                );

            }
        );
    }

}
