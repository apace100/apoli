package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class SelfActionWhenHitPowerType extends CooldownPowerType {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Consumer<Entity> entityAction;

    public SelfActionWhenHitPowerType(Power power, LivingEntity entity, Consumer<Entity> entityAction, Predicate<Pair<DamageSource, Float>> damageCondition, HudRender hudRender, int cooldownDuration) {
        super(power, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.entityAction = entityAction;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return this.canUse()
            && (damageCondition == null || damageCondition.test(new Pair<>(source, amount)));
    }

    public void whenHit() {
        this.use();
        this.entityAction.accept(entity);
    }

    public static PowerTypeFactory<?> createFactory(Identifier id) {
        return new PowerTypeFactory<>(id,
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER)
                .add("cooldown", SerializableDataTypes.INT, 1),
            data -> (power, entity) -> new SelfActionWhenHitPowerType(power, entity,
                data.get("entity_action"),
                data.get("damage_condition"),
                data.get("hud_render"),
                data.get("cooldown")
            )
        ).allowCondition();
    }

}
