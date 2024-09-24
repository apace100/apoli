package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.util.IdentifierAlias;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.registry.Registry;

import java.util.function.Supplier;

public class PowerTypes {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static final PowerTypeFactory<PowerType> SIMPLE = register(PowerType.createSimpleFactory(Apoli.identifier("simple"), PowerType::new));
    public static final PowerTypeFactory<PowerType> MULTIPLE = register(PowerType.createSimpleFactory(Apoli.identifier("multiple"), PowerType::new));

    public static final PowerTypeFactory<RecipePowerType> RECIPE = register(RecipePowerType::getFactory);

    public static void register() {
        register(TogglePowerType::getFactory);
        register(AttributePowerType::getFactory);
        register(CooldownPowerType::getFactory);
        register(EffectImmunityPowerType::getFactory);
        register(ElytraFlightPowerType::getFactory);
        register(FireProjectilePowerType::getFactory);
        register(InventoryPowerType::getFactory);
        register(InvisibilityPowerType::getFactory);
        register(InvulnerablePowerType::getFactory);
        register(ActiveCooldownPowerType.getLaunchFactory());
        register(ModelColorPowerType::getFactory);
        register(ModifyBreakSpeedPowerType::getFactory);
        register(ModifyDamageDealtPowerType::getFactory);
        register(ModifyDamageTakenPowerType::getFactory);
        register(ValueModifyingPowerType.createValueModifyingFactory(Apoli.identifier("modify_exhaustion"), ModifyExhaustionPowerType::new));
        register(ModifyHarvestPowerType::getFactory);
        register(ModifyJumpPowerType::getFactory);
        register(ModifyPlayerSpawnPowerType::getFactory);
        register(NightVisionPowerType::getFactory);
        register(ParticlePowerType::getFactory);
        register(PhasingPowerType::getFactory);
        register(PreventItemUsePowerType::getFactory);
        register(PreventSleepPowerType::getFactory);
        register(RestrictArmorPowerType::getFactory);
        register(ConditionedRestrictArmorPowerType::getFactory);
        register(StackingStatusEffectPowerType::getFactory);
        register(ModifySwimSpeedPowerType::getFactory);
        register(DamageOverTimePowerType::getFactory);
        register(PowerType.createSimpleFactory(Apoli.identifier("swimming"), SwimmingPowerType::new));
        register(PowerType.createSimpleFactory(Apoli.identifier("fire_immunity"), FireImmunityPowerType::new));
        register(ModifyLavaSpeedPowerType::getFactory);
        register(LavaVisionPowerType::getFactory);
        register(ConditionedAttributePowerType::getFactory);
        register(ActiveCooldownPowerType.getActiveSelfFactory());
        register(ActionOverTimePowerType::getFactory);
        register(SelfActionWhenHitPowerType.createFactory(Apoli.identifier("self_action_when_hit")));
        register(AttackerActionWhenHitPowerType::getFactory);
        register(SelfActionOnHitPowerType::getFactory);
        register(TargetActionOnHitPowerType::getFactory);
        register(StartingEquipmentPowerType::getFactory);
        register(ActionOnCallbackPowerType::getFactory);
        register(WalkOnFluidPowerType::getFactory);
        register(ShaderPowerType::getFactory);
        register(PowerType.createSimpleFactory(Apoli.identifier("shaking"), ShakingPowerType::new));
        register(PowerType.createSimpleFactory(Apoli.identifier("disable_regen"), DisableRegenPowerType::new));
        register(ResourcePowerType::getFactory);
        register(ModifyFoodPowerType::getFactory);
        register(ValueModifyingPowerType.createValueModifyingFactory(Apoli.identifier("modify_xp_gain"), ModifyExperiencePowerType::new));
        register(ActionOnBlockBreakPowerType::getFactory);
        register(ActionOnLandPowerType::getFactory);
        register(PreventEntityRenderPowerType::getFactory);
        register(EntityGlowPowerType::getFactory);
        register(SelfGlowPowerType::getFactory);
        register(ClimbingPowerType::getFactory);
        register(PreventBlockSelectionPowerType::getFactory);
        register(SelfActionOnKillPowerType::getFactory);
        register(PowerType.createSimpleFactory(Apoli.identifier("ignore_water"), IgnoreWaterPowerType::new));
        register(ModifyProjectileDamagePowerType::getFactory);
        register(ActionOnWakeUpPowerType::getFactory);
        register(PreventBlockUsePowerType::getFactory);
        register(PreventDeathPowerType::getFactory);
        register(ActionOnItemUsePowerType::getFactory);
        register(ModifyFallingPowerType::getFactory);
        register(PlayerAbilityPowerType.createFactory(Apoli.identifier("creative_flight"), VanillaAbilities.ALLOW_FLYING));
        register(ActionOnEntityUsePowerType::getFactory);
        register(ActionOnBeingUsedPowerType::getFactory);
        register(PreventEntityUsePowerType::getFactory);
        register(PreventBeingUsedPowerType::getFactory);
        register(ToggleNightVisionPowerType::getFactory);
        register(BurnPowerType::getFactory);
        register(ExhaustOverTimePowerType::getFactory);
        register(PreventGameEventPowerType::getFactory);
        register(ModifyCraftingPowerType::getFactory);
        register(PowerType.createSimpleFactory(Apoli.identifier("freeze"), FreezePowerType::new));
        register(ModifyBlockRenderPowerType::getFactory);
        register(ModifyFluidRenderPowerType::getFactory);
        register(ModifyCameraSubmersionTypePowerType::getFactory);
        register(OverrideHudTexturePowerType::getFactory);
        register(ItemOnItemPowerType::getFactory);
        register(OverlayPowerType::getFactory);
        register(TooltipPowerType::getFactory);
        register(ActionOnHitPowerType::getFactory);
        register(ActionWhenHitPowerType::getFactory);
        register(SelfActionWhenHitPowerType.createFactory(Apoli.identifier("action_when_damage_taken")));
        register(ValueModifyingPowerType.createValueModifyingFactory(Apoli.identifier("modify_air_speed"), ModifyAirSpeedPowerType::new));
        register(AttributeModifyTransferPowerType::getFactory);
        register(PreventFeatureRenderPowerType::getFactory);
        register(ModifySlipperinessPowerType::getFactory);
        register(PreventEntityCollisionPowerType::getFactory);
        register(ActionOnBlockUsePowerType::getFactory);
        register(PreventElytraFlightPowerType::getFactory);
        register(KeepInventoryPowerType::getFactory);
        register(ModifyStatusEffectDurationPowerType::getFactory);
        register(ModifyStatusEffectAmplifierPowerType::getFactory);
        register(ModifyAttributePowerType::getFactory);
        register(PowerType.createSimpleFactory(Apoli.identifier("prevent_sprinting"), PreventSprintingPowerType::new));
        register(ValueModifyingPowerType.createValueModifyingFactory(Apoli.identifier("modify_healing"), ModifyHealingPowerType::new));
        register(ValueModifyingPowerType.createValueModifyingFactory(Apoli.identifier("modify_insomnia_ticks"), ModifyInsomniaTicksPowerType::new));
        register(ModifyGrindstonePowerType::getFactory);
        register(ReplaceLootTablePowerType::getFactory);
        register(ModifyVelocityPowerType::getFactory);
        register(PowerType.createSimpleFactory(Apoli.identifier("grounded"), GroundedPowerType::new));
        register(ModifyEnchantmentLevelPowerType::getFactory);
        register(ActionOnDeathPowerType::getFactory);
        register(ActionOnItemPickupPowerType::getFactory);
        register(PreventItemPickupPowerType::getFactory);
        register(EdibleItemPowerType::getFactory);
        register(GameEventListenerPowerType::getFactory);
        register(ActionOnBlockPlacePowerType::getFactory);
        register(PreventBlockPlacePowerType::getFactory);
        register(EntitySetPowerType::getFactory);
        register(ModifyFovPowerType::getFactory);
        register(PreventEntitySelectionPowerType::getFactory);
        register(SprintingPowerType::getFactory);
        register(PosePowerType::getFactory);
        register(ModifyTypeTagPowerType::getFactory);
    }

    @SuppressWarnings("unchecked")
    public static <T extends PowerType> PowerTypeFactory<T> register(PowerTypeFactory<?> powerTypeFactory) {
        return (PowerTypeFactory<T>) Registry.register(ApoliRegistries.POWER_FACTORY, powerTypeFactory.getSerializerId(), powerTypeFactory);
    }

    public static <T extends PowerType> PowerTypeFactory<T> register(Supplier<PowerTypeFactory<?>> powerTypeFactory) {
        return register(powerTypeFactory.get());
    }

}
