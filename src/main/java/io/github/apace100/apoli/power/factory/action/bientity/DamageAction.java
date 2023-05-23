package io.github.apace100.apoli.power.factory.action.bientity;

import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.DamageSourceDescription;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Pair;

public class DamageAction {

    public static void action(SerializableData.Instance data, Pair<Entity, Entity> entities) {
        float amount = data.get("amount");
        DamageSource source = MiscUtil.createDamageSource(
                entities.getLeft().getDamageSources(), data.get("source"), data.get("damage_type"), entities.getLeft());
        entities.getRight().damage(source, amount);
    }

    public static ActionFactory<Pair<Entity, Entity>> getFactory() {
        return new ActionFactory<>(Apoli.identifier("damage"),
            new SerializableData()
                .add("amount", SerializableDataTypes.FLOAT)
                .add("source", ApoliDataTypes.DAMAGE_SOURCE_DESCRIPTION, null)
                .add("damage_type", SerializableDataTypes.DAMAGE_TYPE, null),
            DamageAction::action
        );
    }
}
