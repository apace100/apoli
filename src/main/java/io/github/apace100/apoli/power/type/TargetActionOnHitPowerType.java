package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
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

import java.util.Optional;

@Deprecated
public class TargetActionOnHitPowerType extends CooldownPowerType {

    public static final TypedDataObjectFactory<TargetActionOnHitPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("entity_action", EntityAction.DATA_TYPE)
            .add("target_condition", EntityCondition.DATA_TYPE.optional(), Optional.empty())
            .add("damage_condition", DamageCondition.DATA_TYPE.optional(), Optional.empty())
            .add("hud_render", HudRender.DATA_TYPE, HudRender.DONT_RENDER)
            .add("cooldown", SerializableDataTypes.INT, 1),
        (data, condition) -> new TargetActionOnHitPowerType(
            data.get("entity_action"),
            data.get("target_condition"),
            data.get("damage_condition"),
            data.get("hud_render"),
            data.get("cooldown"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("entity_action", powerType.entityAction)
            .set("target_condition", powerType.targetCondition)
            .set("damage_condition", powerType.damageCondition)
            .set("hud_render", powerType.getRenderSettings())
            .set("cooldown", powerType.getCooldown())
    );

    private final EntityAction entityAction;

    private final Optional<EntityCondition> targetCondition;
    private final Optional<DamageCondition> damageCondition;

    public TargetActionOnHitPowerType(EntityAction entityAction, Optional<EntityCondition> targetCondition, Optional<DamageCondition> damageCondition, HudRender hudRender, int cooldownDuration, Optional<EntityCondition> condition) {
        super(cooldownDuration, hudRender, condition);
        this.entityAction = entityAction;
        this.targetCondition = targetCondition;
        this.damageCondition = damageCondition;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.TARGET_ACTION_ON_HIT;
    }

    public boolean doesApply(Entity target, DamageSource source, float amount) {
        return this.canUse()
            && damageCondition.map(condition -> condition.test(source, amount)).orElse(true)
            && targetCondition.map(condition -> condition.test(target)).orElse(true);
    }

    public void onHit(Entity target) {
        this.use();
        this.entityAction.execute(target);
    }

}
