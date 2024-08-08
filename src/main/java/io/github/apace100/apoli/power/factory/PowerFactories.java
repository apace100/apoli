package io.github.apace100.apoli.power.factory;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.IdentifierAlias;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.registry.Registry;

public class PowerFactories {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    public static final PowerFactory<?> SIMPLE = register(new PowerFactory<>(
        Apoli.identifier("simple"),
        new SerializableData(),
        data -> Power::new
    ).allowCondition());
    public static final PowerFactory<?> MULTIPLE = register(new PowerFactory<>(
        Apoli.identifier("multiple"),
        new SerializableData(),
        data -> Power::new
    ));

    @SuppressWarnings("unchecked")
    public static void register() {
        register(TogglePower::createFactory);
        register(AttributePower::createFactory);
        register(CooldownPower::createFactory);
        register(EffectImmunityPower::createFactory);
        register(ElytraFlightPower::createFactory);
        register(FireProjectilePower::createFactory);
        register(InventoryPower::createFactory);
        register(InvisibilityPower::createFactory);
        register(InvulnerablePower::createFactory);
        register(ActiveCooldownPower::createLaunchFactory);
        register(ModelColorPower::createFactory);
        register(ModifyBreakSpeedPower::createFactory);
        register(ModifyDamageDealtPower::createFactory);
        register(ModifyDamageTakenPower::createFactory);
        register(() -> ValueModifyingPower.createValueModifyingFactory(Apoli.identifier("modify_exhaustion"), ModifyExhaustionPower::new));
        register(ModifyHarvestPower::createFactory);
        register(ModifyJumpPower::createFactory);
        register(ModifyPlayerSpawnPower::createFactory);
        register(NightVisionPower::createFactory);
        register(ParticlePower::createFactory);
        register(PhasingPower::createFactory);
        register(PreventItemUsePower::createFactory);
        register(PreventSleepPower::createFactory);
        register(RestrictArmorPower::createFactory);
        register(ConditionedRestrictArmorPower::createFactory);
        register(StackingStatusEffectPower::createFactory);
        register(ModifySwimSpeedPower::createFactory);
        register(DamageOverTimePower::createFactory);
        register(() -> Power.createSimpleFactory(Apoli.identifier("swimming"), SwimmingPower::new));
        register(() -> Power.createSimpleFactory(Apoli.identifier("fire_immunity"), FireImmunityPower::new));
        register(ModifyLavaSpeedPower::createFactory);
        register(LavaVisionPower::createFactory);
        register(ConditionedAttributePower::createFactory);
        register(ActiveCooldownPower::createActiveSelfFactory);
        register(ActionOverTimePower::createFactory);
        register(() -> SelfActionWhenHitPower.createFactory(Apoli.identifier("self_action_when_hit")));
        register(AttackerActionWhenHitPower::createFactory);
        register(SelfActionOnHitPower::createFactory);
        register(TargetActionOnHitPower::createFactory);
        register(StartingEquipmentPower::createFactory);
        register(ActionOnCallbackPower::createFactory);
        register(WalkOnFluidPower::createFactory);
        register(ShaderPower::createFactory);
        register(() -> Power.createSimpleFactory(Apoli.identifier("shaking"), ShakingPower::new));
        register(() -> Power.createSimpleFactory(Apoli.identifier("disable_regen"), DisableRegenPower::new));
        register(ResourcePower::createFactory);
        register(ModifyFoodPower::createFactory);
        register(() -> ValueModifyingPower.createValueModifyingFactory(Apoli.identifier("modify_xp_gain"), ModifyExperiencePower::new));
        register(ActionOnBlockBreakPower::createFactory);
        register(ActionOnLandPower::createFactory);
        register(PreventEntityRenderPower::createFactory);
        register(EntityGlowPower::createFactory);
        register(SelfGlowPower::createFactory);
        register(ClimbingPower::createFactory);
        register(PreventBlockSelectionPower::createFactory);
        register(SelfActionOnKillPower::createFactory);
        register(RecipePower::createFactory);
        register(() -> Power.createSimpleFactory(Apoli.identifier("ignore_water"), IgnoreWaterPower::new));
        register(ModifyProjectileDamagePower::createFactory);
        register(ActionOnWakeUp::createFactory);
        register(PreventBlockUsePower::createFactory);
        register(PreventDeathPower::createFactory);
        register(ActionOnItemUsePower::createFactory);
        register(ModifyFallingPower::createFactory);
        register(() -> PlayerAbilityPower.createAbilityFactory(
            Apoli.identifier("creative_flight"), VanillaAbilities.ALLOW_FLYING));
        register(ActionOnEntityUsePower::createFactory);
        register(ActionOnBeingUsedPower::createFactory);
        register(PreventEntityUsePower::createFactory);
        register(PreventBeingUsedPower::createFactory);
        register(ToggleNightVisionPower::createFactory);
        register(BurnPower::createFactory);
        register(ExhaustOverTimePower::createFactory);
        register(PreventGameEventPower::createFactory);
        register(ModifyCraftingPower::createFactory);
        register(() -> Power.createSimpleFactory(Apoli.identifier("freeze"), FreezePower::new));
        register(ModifyBlockRenderPower::createFactory);
        register(ModifyFluidRenderPower::createFactory);
        register(ModifyCameraSubmersionTypePower::createFactory);
        register(OverrideHudTexturePower::createFactory);
        register(ItemOnItemPower::createFactory);
        register(OverlayPower::createFactory);
        register(TooltipPower::createFactory);
        register(ActionOnHitPower::createFactory);
        register(ActionWhenHitPower::createFactory);
        register(() -> SelfActionWhenHitPower.createFactory(Apoli.identifier("action_when_damage_taken")));
        register(() -> ValueModifyingPower.createValueModifyingFactory(Apoli.identifier("modify_air_speed"), ModifyAirSpeedPower::new));
        register(AttributeModifyTransferPower::createFactory);
        register(PreventFeatureRenderPower::createFactory);
        register(ModifySlipperinessPower::createFactory);
        register(PreventEntityCollisionPower::createFactory);
        register(ActionOnBlockUsePower::createFactory);
        register(PreventElytraFlightPower::createFactory);
        register(KeepInventoryPower::createFactory);
        register(ModifyStatusEffectDurationPower::createFactory);
        register(ModifyStatusEffectAmplifierPower::createFactory);
        register(ModifyAttributePower::createFactory);
        register(() -> Power.createSimpleFactory(Apoli.identifier("prevent_sprinting"), PreventSprintingPower::new));
        register(() -> ValueModifyingPower.createValueModifyingFactory(Apoli.identifier("modify_healing"), ModifyHealingPower::new));
        register(() -> ValueModifyingPower.createValueModifyingFactory(Apoli.identifier("modify_insomnia_ticks"), ModifyInsomniaTicksPower::new));
        register(ModifyGrindstonePower::createFactory);
        register(ReplaceLootTablePower::createFactory);
        register(ModifyVelocityPower::createFactory);
        register(() -> Power.createSimpleFactory(Apoli.identifier("grounded"), GroundedPower::new));
        register(ModifyEnchantmentLevelPower::createFactory);
        register(ActionOnDeathPower::createFactory);
        register(ActionOnItemPickupPower::createFactory);
        register(PreventItemPickupPower::createFactory);
        register(EdibleItemPower::createFactory);
        register(GameEventListenerPower::createFactory);
        register(ActionOnBlockPlacePower::createFactory);
        register(PreventBlockPlacePower::createFactory);
        register(EntitySetPower::createFactory);
        register(ModifyFovPower::createFactory);
        register(PreventEntitySelectionPower::createFactory);
        register(SprintingPower::createFactory);
        register(PosePower::createFactory);
    }

    private static PowerFactory<?> register(PowerFactory<?> powerFactory) {
        return Registry.register(ApoliRegistries.POWER_FACTORY, powerFactory.getSerializerId(), powerFactory);
    }

    private static PowerFactory<?> register(PowerFactorySupplier<?> factorySupplier) {
        return register(factorySupplier.createFactory());
    }

}
