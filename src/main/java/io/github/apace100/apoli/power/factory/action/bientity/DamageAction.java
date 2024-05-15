package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.LinkedList;
import java.util.List;

public class DamageAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> entities) {

        Entity actor = entities.getLeft();
        Entity target = entities.getRight();

        if (actor == null || target == null) {
            return;
        }

        Float damageAmount = data.get("amount");
        List<Modifier> modifiers = new LinkedList<>();

        data.<Modifier>ifPresent("modifier", modifiers::add);
        data.<List<Modifier>>ifPresent("modifiers", modifiers::addAll);

        if (!modifiers.isEmpty() && target instanceof LivingEntity livingTarget) {
            damageAmount = (float) ModifierUtil.applyModifiers(actor, modifiers, livingTarget.getMaxHealth());
        }

        if (damageAmount == null) {
            return;
        }

        try {
            DamageSource damageSource = MiscUtil.createDamageSource(actor.getDamageSources(), data.get("source"), data.get("damage_type"), actor);
            target.damage(damageSource, damageAmount);
        }

        catch (Throwable t) {
            Apoli.LOGGER.error("Error trying to deal damage via the `damage` bi-entity action: " + t.getMessage());
        }

    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("damage"),
            new SerializableData()
                .add("amount", SerializableDataTypes.FLOAT, null)
                .add("source", ApoliDataTypes.DAMAGE_SOURCE_DESCRIPTION, null)
                .add("damage_type", SerializableDataTypes.DAMAGE_TYPE, null)
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null),
            DamageAction::action
        );
    }
}
