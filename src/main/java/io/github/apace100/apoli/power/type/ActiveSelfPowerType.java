package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.action.EntityAction;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ActiveSelfPowerType extends ActiveCooldownPowerType {

	public static final TypedDataObjectFactory<ActiveSelfPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
		new SerializableData()
			.add("entity_action", EntityAction.DATA_TYPE)
			.add("hud_render", HudRender.DATA_TYPE, HudRender.DONT_RENDER)
			.add("cooldown", SerializableDataTypes.INT, 1)
			.add("key", ApoliDataTypes.BACKWARDS_COMPATIBLE_KEY, new Key()),
		(data, condition) -> new ActiveSelfPowerType(
			data.get("entity_action"),
			data.get("hud_render"),
			data.get("cooldown"),
			data.get("key"),
			condition
		),
		(powerType, serializableData) -> serializableData.instance()
			.set("entity_action", powerType.entityAction)
			.set("hud_render", powerType.getRenderSettings())
			.set("cooldown", powerType.getCooldown())
			.set("key", powerType.getKey())
	);

	private final EntityAction entityAction;

	public ActiveSelfPowerType(EntityAction entityAction, HudRender hudRender, int cooldownDuration, Key key, Optional<EntityCondition> condition) {
		super(hudRender, cooldownDuration, key, condition);
		this.entityAction = entityAction;
	}

	@Override
	public @NotNull PowerConfiguration<?> getConfig() {
		return PowerTypes.ACTIVE_SELF;
	}

	@Override
	public void onUse() {
		super.onUse();
		entityAction.execute(getHolder());
	}

}
