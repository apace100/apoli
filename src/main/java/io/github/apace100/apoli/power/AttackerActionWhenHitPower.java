package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class AttackerActionWhenHitPower extends CooldownPower {

    private final Predicate<Pair<DamageSource, Float>> damageCondition;
    private final Consumer<Entity> entityAction;

    public AttackerActionWhenHitPower(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Predicate<Pair<DamageSource, Float>> damageCondition, Consumer<Entity> entityAction) {
        super(type, entity, cooldownDuration, hudRender);
        this.damageCondition = damageCondition;
        this.entityAction = entityAction;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return source.getAttacker() != null
            && this.canUse()
            && (damageCondition == null || damageCondition.test(new Pair<>(source, amount)));
    }

    public void whenHit(Entity attacker) {
        this.entityAction.accept(attacker);
        this.use();
    }

    @Deprecated(forRemoval = true)
    public void whenHit(DamageSource damageSource, float damageAmount) {
        if(damageSource.getAttacker() != null && damageSource.getAttacker() != entity) {
            if(damageCondition == null || damageCondition.test(new Pair<>(damageSource, damageAmount))) {
                if(canUse()) {
                    this.entityAction.accept(damageSource.getAttacker());
                    use();
                }
            }
        }
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
            Apoli.identifier("attacker_action_when_hit"),
            new SerializableData()
                .add("entity_action", ApoliDataTypes.ENTITY_ACTION)
                .add("damage_condition", ApoliDataTypes.DAMAGE_CONDITION, null)
                .add("cooldown", SerializableDataTypes.INT, 1)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER),
            data -> (powerType, entity) -> new AttackerActionWhenHitPower(
                powerType,
                entity,
                data.get("cooldown"),
                data.get("hud_render"),
                data.get("damage_condition"),
                data.get("entity_action")
            )
        ).allowCondition();
    }

}
