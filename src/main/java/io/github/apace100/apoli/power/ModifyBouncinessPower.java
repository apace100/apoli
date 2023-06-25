package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ModifyBouncinessPower extends ValueModifyingPower {

    private final Predicate<CachedBlockPosition> blockCondition;
    private final boolean takeFallDamage;
    private final Stage stage;

    public ModifyBouncinessPower(PowerType<?> type, LivingEntity entity, Predicate<CachedBlockPosition> blockCondition, boolean takeFallDamage, Stage stage) {
        super(type, entity);
        this.blockCondition = blockCondition;
        this.takeFallDamage = takeFallDamage;
        this.stage = stage;
    }

    public static float modify(Entity entity, double baseBounciness, CachedBlockPosition cbp) {
        double preY = PowerHolderComponent.modify(entity, ModifyBouncinessPower.class, (float)baseBounciness, p -> p.doesApply(cbp) && p.stage == Stage.PRE);
        return PowerHolderComponent.modify(entity, ModifyBouncinessPower.class, (float)(entity.getVelocity().getY() * preY), p -> p.doesApply(cbp) && p.stage == Stage.POST);
    }

    public boolean doesApply(CachedBlockPosition cbp) {
        return blockCondition == null || blockCondition.test(cbp);
    }

    public static boolean shouldCancelFallDamage(Entity entity, CachedBlockPosition cbp) {
        return PowerHolderComponent.getPowers(entity, ModifyBouncinessPower.class).stream().anyMatch(p -> p.doesApply(cbp) && !p.takeFallDamage);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_bounciness"),
            new SerializableData()
                .add("modifier", Modifier.DATA_TYPE, null)
                .add("modifiers", Modifier.LIST_TYPE, null)
                .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null)
                .add("take_fall_damage", SerializableDataTypes.BOOLEAN, false)
                .add("stage", SerializableDataType.enumValue(Stage.class), Stage.PRE),
            data ->
                (type, player) -> {
                    ModifyBouncinessPower power = new ModifyBouncinessPower(type, player, data.get("block_condition"), data.getBoolean("take_fall_damage"), data.get("stage"));
                    data.ifPresent("modifier", power::addModifier);
                    data.<List<Modifier>>ifPresent("modifiers",
                        mods -> mods.forEach(power::addModifier)
                    );
                    return power;
                })
            .allowCondition();
    }

    public enum Stage {
        PRE,
        POST
    }

}
