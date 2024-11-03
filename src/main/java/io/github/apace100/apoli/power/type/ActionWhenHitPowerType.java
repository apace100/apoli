package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.BiEntityAction;
import io.github.apace100.apoli.condition.BiEntityCondition;
import io.github.apace100.apoli.condition.DamageCondition;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ActionWhenHitPowerType extends CooldownPowerType {

    public static final TypedDataObjectFactory<ActionWhenHitPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("bientity_action", BiEntityAction.DATA_TYPE.optional(), Optional.empty())
            .add("bientity_condition", BiEntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("damage_condition", DamageCondition.DATA_TYPE.optional(), Optional.empty())
            .add("hud_render", HudRender.DATA_TYPE, HudRender.DONT_RENDER)
            .add("cooldown", SerializableDataTypes.INT, 1),
        (data, condition) -> new ActionWhenHitPowerType(
            data.get("bientity_action"),
            data.get("bientity_condition"),
            data.get("damage_condition"),
            data.get("hud_render"),
            data.get("cooldown"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("bientity_action", powerType.biEntityAction)
            .set("bientity_condition", powerType.biEntityCondition)
            .set("damage_condition", powerType.damageCondition)
            .set("hud_render", powerType.getRenderSettings())
            .set("cooldown", powerType.getCooldown())
    );

    private final Optional<BiEntityAction> biEntityAction;

    private final Optional<BiEntityCondition> biEntityCondition;
    private final Optional<DamageCondition> damageCondition;

    public ActionWhenHitPowerType(Optional<BiEntityAction> biEntityAction, Optional<BiEntityCondition> biEntityCondition, Optional<DamageCondition> damageCondition, HudRender hudRender, int cooldownDuration, Optional<EntityCondition> condition) {
        super(cooldownDuration, hudRender, condition);
        this.damageCondition = damageCondition;
        this.biEntityAction = biEntityAction;
        this.biEntityCondition = biEntityCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.ACTION_WHEN_HIT;
    }

    public boolean doesApply(@Nullable Entity attacker, DamageSource source, float amount) {
        return attacker != null
            && this.canUse()
            && damageCondition.map(condition -> condition.test(source, amount)).orElse(true)
            && biEntityCondition.map(condition -> condition.test(attacker, getHolder())).orElse(true);
    }

    public void whenHit(Entity attacker) {
        this.use();
        biEntityAction.ifPresent(action -> action.execute(attacker, getHolder()));
    }

}
