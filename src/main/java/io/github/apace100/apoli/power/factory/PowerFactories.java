package io.github.apace100.apoli.power.factory;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.registry.Registry;

public class PowerFactories {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new PowerFactory<>(Apoli.identifier("simple"), new SerializableData(), data -> Power::new).allowCondition());
        register(TogglePower::createFactory);
        register(AttributePower::createFactory);
        register(CooldownPower::createFactory);
        register(EffectImmunityPower::createFactory);
        register(ElytraFlightPower::createFactory);
        register(SetEntityGroupPower::createFactory);
        register(FireProjectilePower::createFactory);
        register(InventoryPower::createFactory);
        register(InvisibilityPower::createFactory);
        register(InvulnerablePower::createFactory);
        register(ActiveCooldownPower::createLaunchFactory);
        register(ModelColorPower::createFactory);
        register(ModifyBreakSpeedPower::createFactory);
        register(ModifyDamageDealtPower::createFactory);
        register(ModifyDamageTakenPower::createFactory);
        register(() -> ValueModifyingPower.createValueModifyingFactory(
            ModifyExhaustionPower::new, Apoli.identifier("modify_exhaustion")));
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
        register(() -> Power.createSimpleFactory(SwimmingPower::new, Apoli.identifier("swimming")));
        register(() -> Power.createSimpleFactory(FireImmunityPower::new, Apoli.identifier("fire_immunity")));
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
        register(() -> Power.createSimpleFactory(ShakingPower::new, Apoli.identifier("shaking")));
        register(() -> Power.createSimpleFactory(DisableRegenPower::new, Apoli.identifier("disable_regen")));
        register(ResourcePower::createFactory);
        register(ModifyFoodPower::createFactory);
        register(() -> ValueModifyingPower.createValueModifyingFactory(
            ModifyExperiencePower::new, Apoli.identifier("modify_xp_gain")));
        register(ActionOnBlockBreakPower::createFactory);
        register(ActionOnLandPower::createFactory);
        register(PreventEntityRenderPower::createFactory);
        register(EntityGlowPower::createFactory);
        register(SelfGlowPower::createFactory);
        register(ClimbingPower::createFactory);
        register(PreventBlockSelectionPower::createFactory);
        register(SelfActionOnKillPower::createFactory);
        register(RecipePower::createFactory);
        register(() -> Power.createSimpleFactory(IgnoreWaterPower::new, Apoli.identifier("ignore_water")));
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
        register(() -> Power.createSimpleFactory(FreezePower::new, Apoli.identifier("freeze")));
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
        register(() -> ValueModifyingPower.createValueModifyingFactory(
            ModifyAirSpeedPower::new, Apoli.identifier("modify_air_speed")));
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
        register(() -> Power.createSimpleFactory(PreventSprintingPower::new, Apoli.identifier("prevent_sprinting")));
        register(() -> ValueModifyingPower.createValueModifyingFactory(
            ModifyHealingPower::new, Apoli.identifier("modify_healing")));
        register(() -> ValueModifyingPower.createValueModifyingFactory(
            ModifyInsomniaTicksPower::new, Apoli.identifier("modify_insomnia_ticks")));
        register(ModifyGrindstonePower::createFactory);
        register(ReplaceLootTablePower::createFactory);
        register(ModifyVelocityPower::createFactory);
        register(() -> Power.createSimpleFactory(GroundedPower::new, Apoli.identifier("grounded")));
        register(ModifyEnchantmentLevelPower::createFactory);
        register(ActionOnDeathPower::createFactory);
        register(ActionOnItemPickupPower::createFactory);
        register(PreventItemPickupPower::createFactory);
        register(EdibleItemPower::createFactory);
        register(GameEventListenerPower::createFactory);
        register(ActionOnBlockPlacePower::createFactory);
        register(PreventBlockPlacePower::createFactory);
        register(EntitySetPower::createFactory);
    }

    private static void register(PowerFactory<?> powerFactory) {
        Registry.register(ApoliRegistries.POWER_FACTORY, powerFactory.getSerializerId(), powerFactory);
    }

    private static void register(PowerFactorySupplier<?> factorySupplier) {
        register(factorySupplier.createFactory());
    }
}
