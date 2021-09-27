package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.function.Predicate;

public class BiEntityConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, pair) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIENTITY_CONDITIONS),
            (data, pair) -> ((List<ConditionFactory<Pair<Entity, Entity>>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(pair)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.BIENTITY_CONDITIONS),
            (data, pair) -> ((List<ConditionFactory<Pair<Entity, Entity>>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(pair)
            )));
        register(new ConditionFactory<>(Apoli.identifier("invert"), new SerializableData()
            .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Pair<Entity, Entity>> cond = ((ConditionFactory<Pair<Entity, Entity>>.Instance)data.get("condition"));
                return cond.test(new Pair<>(pair.getRight(), pair.getLeft()));
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("actor_condition"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = ((ConditionFactory<Entity>.Instance)data.get("condition"));
                return cond.test(pair.getLeft());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("target_condition"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = ((ConditionFactory<Entity>.Instance)data.get("condition"));
                return cond.test(pair.getRight());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("either"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = ((ConditionFactory<Entity>.Instance)data.get("condition"));
                return cond.test(pair.getLeft()) || cond.test(pair.getRight());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("both"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Entity> cond = ((ConditionFactory<Entity>.Instance)data.get("condition"));
                return cond.test(pair.getLeft()) && cond.test(pair.getRight());
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("undirected"), new SerializableData()
            .add("condition", ApoliDataTypes.BIENTITY_CONDITION),
            (data, pair) -> {
                Predicate<Pair<Entity, Entity>> cond = ((ConditionFactory<Pair<Entity, Entity>>.Instance)data.get("condition"));
                return cond.test(pair) || cond.test(new Pair<>(pair.getRight(), pair.getLeft()));
            }
            ));

        register(new ConditionFactory<>(Apoli.identifier("distance"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, pair) -> {
                double distanceSq = pair.getLeft().getPos().squaredDistanceTo(pair.getRight().getPos());
                double comp = data.getDouble("compare_to");
                comp *= comp;
                return ((Comparison)data.get("comparison")).compare(distanceSq, comp);
            }
            ));
        register(new ConditionFactory<>(Apoli.identifier("can_see"), new SerializableData(),
            (data, pair) -> pair.getLeft() instanceof LivingEntity && ((LivingEntity)pair.getLeft()).canSee(pair.getRight())
        ));
        register(new ConditionFactory<>(Apoli.identifier("owner"), new SerializableData(),
            (data, pair) -> {
                if(pair.getRight() instanceof Tameable) {
                    return pair.getLeft() == ((Tameable)pair.getRight()).getOwner();
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding"), new SerializableData(),
            (data, pair) -> pair.getLeft().getVehicle() == pair.getRight()
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding_root"), new SerializableData(),
            (data, pair) -> pair.getLeft().getRootVehicle() == pair.getRight()
        ));
        register(new ConditionFactory<>(Apoli.identifier("riding_recursive"), new SerializableData(),
            (data, pair) -> {
                if(pair.getLeft().getVehicle() == null) {
                    return false;
                }
                Entity vehicle = pair.getLeft().getVehicle();
                while(vehicle != pair.getRight() && vehicle != null) {
                    vehicle = vehicle.getVehicle();
                }
                return vehicle == pair.getRight();
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("attack_target"), new SerializableData(),
            (data, pair) -> {
                if(pair.getLeft() instanceof MobEntity) {
                    return ((MobEntity)pair.getLeft()).getTarget() == pair.getRight();
                }
                if(pair.getLeft() instanceof Angerable) {
                    return ((Angerable)pair.getLeft()).getTarget() == pair.getRight();
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("attacker"), new SerializableData(),
            (data, pair) -> {
                if(pair.getRight() instanceof LivingEntity living) {
                    return living.getAttacker() == pair.getLeft();
                }
                return false;
            }
        ));
    }

    private static void register(ConditionFactory<Pair<Entity, Entity>> conditionFactory) {
        Registry.register(ApoliRegistries.BIENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
