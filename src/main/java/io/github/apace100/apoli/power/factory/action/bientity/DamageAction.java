package io.github.apace100.apoli.power.factory.action.bientity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.util.Pair;

public class DamageAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> entities) {
        float amount = data.get("amount");
        DamageSource providedSource = data.get("source");
        DamageSource source = new EntityDamageSource(providedSource.getName(), entities.getLeft());
        if(providedSource.isExplosive()) {
            source.setExplosive();
        }
        if(providedSource.isProjectile()) {
            source.setProjectile();
        }
        if(providedSource.isFromFalling()) {
            source.setFromFalling();
        }
        if(providedSource.isMagic()) {
            source.setUsesMagic();
        }
        if(providedSource.isNeutral()) {
            source.setNeutral();
        }
        entities.getRight().damage(source, amount);
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("damage"),
            new SerializableData()
                .add("amount", SerializableDataTypes.FLOAT)
                .add("source", SerializableDataTypes.DAMAGE_SOURCE),
            DamageAction::action
        );
    }
}
