package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.power.factory.action.entity.*;
import io.github.apace100.apoli.power.factory.action.meta.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.IdentifierAlias;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityActions {

    public static final IdentifierAlias ALIASES = new IdentifierAlias();

    @SuppressWarnings("unchecked")
    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.ENTITY_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.ENTITY_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.ENTITY_ACTION, ApoliDataTypes.ENTITY_CONDITION));
        register(ChoiceAction.getFactory(ApoliDataTypes.ENTITY_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.ENTITY_ACTION, ApoliDataTypes.ENTITY_CONDITION));
        register(DelayAction.getFactory(ApoliDataTypes.ENTITY_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.ENTITY_ACTION, entity -> !entity.getWorld().isClient));

        register(DamageAction.getFactory());
        register(new ActionFactory<>(Apoli.identifier("heal"), new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    ((LivingEntity)entity).heal(data.getFloat("amount"));
                }
            }));
        register(PlaySoundAction.getFactory());
        register(new ActionFactory<>(Apoli.identifier("exhaust"), new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity)
                    ((PlayerEntity)entity).getHungerManager().addExhaustion(data.getFloat("amount"));
            }));
        register(new ActionFactory<>(Apoli.identifier("apply_effect"), new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE, null)
            .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),
            (data, entity) -> {
                if(entity instanceof LivingEntity le && !entity.getWorld().isClient) {
                    if(data.isPresent("effect")) {
                        StatusEffectInstance effect = data.get("effect");
                        le.addStatusEffect(new StatusEffectInstance(effect));
                    }
                    if(data.isPresent("effects")) {
                        ((List<StatusEffectInstance>)data.get("effects")).forEach(e -> le.addStatusEffect(new StatusEffectInstance(e)));
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("clear_effect"), new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT_ENTRY, null),
            (data, entity) -> {
                if(entity instanceof LivingEntity le) {
                    if(data.isPresent("effect")) {
                        le.removeStatusEffect(data.get("effect"));
                    } else {
                        le.clearStatusEffects();
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("set_on_fire"), new SerializableData()
            .add("duration", SerializableDataTypes.INT),
            (data, entity) -> entity.setOnFireFor(data.getInt("duration"))));
        register(new ActionFactory<>(Apoli.identifier("add_velocity"), new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .add("space", ApoliDataTypes.SPACE, Space.WORLD)
            .add("client", SerializableDataTypes.BOOLEAN, true)
            .add("server", SerializableDataTypes.BOOLEAN, true)
            .add("set", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> {
                if (entity instanceof PlayerEntity
                    && (entity.getWorld().isClient ?
                    !data.getBoolean("client") : !data.getBoolean("server")))
                    return;
                Space space = data.get("space");
                Vector3f vec = new Vector3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                TriConsumer<Float, Float, Float> method = entity::addVelocity;
                if(data.getBoolean("set")) {
                    method = entity::setVelocity;
                }
                space.toGlobal(vec, entity);
                method.accept(vec.x, vec.y, vec.z);
                entity.velocityModified = true;
            }));
        register(SpawnEntityAction.getFactory());
        register(new ActionFactory<>(Apoli.identifier("gain_air"), new SerializableData()
            .add("value", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof LivingEntity le) {
                    le.setAir(Math.min(le.getAir() + data.getInt("value"), le.getMaxAir()));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("block_action_at"), new SerializableData()
            .add("block_action", ApoliDataTypes.BLOCK_ACTION),
            (data, entity) -> ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("block_action")).accept(
                Triple.of(entity.getWorld(), entity.getBlockPos(), Direction.UP))));
        register(new ActionFactory<>(Apoli.identifier("extinguish"), new SerializableData(),
            (data, entity) -> entity.extinguish()));
        register(new ActionFactory<>(Apoli.identifier("execute_command"), new SerializableData()
            .add("command", SerializableDataTypes.STRING),
            (data, entity) -> {
                MinecraftServer server = entity.getWorld().getServer();
                if(server != null) {
                    boolean validOutput = !(entity instanceof ServerPlayerEntity) || ((ServerPlayerEntity)entity).networkHandler != null;
                    ServerCommandSource source = new ServerCommandSource(
                        Apoli.config.executeCommand.showOutput && validOutput ? entity : CommandOutput.DUMMY,
                        entity.getPos(),
                        entity.getRotationClient(),
                        entity.getWorld() instanceof ServerWorld ? (ServerWorld)entity.getWorld() : null,
                        Apoli.config.executeCommand.permissionLevel,
                        entity.getName().getString(),
                        entity.getDisplayName(),
                        entity.getWorld().getServer(),
                        entity);
                    server.getCommandManager().executeWithPrefix(source, data.getString("command"));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("change_resource"), new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE)
            .add("change", SerializableDataTypes.INT)
            .add("operation", ApoliDataTypes.RESOURCE_OPERATION, ResourceOperation.ADD),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    PowerType powerType = data.get("resource");
                    Power p = component.getPower(powerType);
                    ResourceOperation operation = data.get("operation");
                    int change = data.getInt("change");
                    if(p instanceof VariableIntPower vip) {
                        if (operation == ResourceOperation.ADD) {
                            int newValue = vip.getValue() + change;
                            vip.setValue(newValue);
                        } else if (operation == ResourceOperation.SET) {
                            vip.setValue(change);
                        }
                        PowerHolderComponent.syncPower(entity, powerType);
                    } else if(p instanceof CooldownPower cp) {
                        if (operation == ResourceOperation.ADD) {
                            cp.modify(change);
                        } else if (operation == ResourceOperation.SET) {
                            cp.setCooldown(change);
                        }
                        PowerHolderComponent.syncPower(entity, powerType);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("feed"), new SerializableData()
            .add("food", SerializableDataTypes.INT)
            .add("saturation", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    ((PlayerEntity)entity).getHungerManager().add(data.getInt("food"), data.getFloat("saturation"));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("add_xp"), new SerializableData()
            .add("points", SerializableDataTypes.INT, 0)
            .add("levels", SerializableDataTypes.INT, 0),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    int points = data.getInt("points");
                    int levels = data.getInt("levels");
                    if(points > 0) {
                        ((PlayerEntity)entity).addExperience(points);
                    }
                    ((PlayerEntity)entity).addExperienceLevels(levels);
                }
            }));

        register(new ActionFactory<>(Apoli.identifier("set_fall_distance"), new SerializableData()
            .add("fall_distance", SerializableDataTypes.FLOAT),
            (data, entity) -> entity.fallDistance = data.getFloat("fall_distance")));
        register(GiveAction.getFactory());
        register(EquippedItemAction.getFactory());
        register(new ActionFactory<>(Apoli.identifier("trigger_cooldown"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    Power p = component.getPower((PowerType)data.get("power"));
                    if(p instanceof CooldownPower cp) {
                        cp.use();
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("toggle"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    Power p = component.getPower((PowerType)data.get("power"));
                    if(p instanceof TogglePower) {
                        ((TogglePower)p).onUse();
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("emit_game_event"), new SerializableData()
            .add("event", SerializableDataTypes.GAME_EVENT),
            (data, entity) -> entity.emitGameEvent(data.get("event"))));
        register(new ActionFactory<>(Apoli.identifier("set_resource"), new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE)
            .add("value", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    PowerType powerType = data.get("resource");
                    Power p = component.getPower(powerType);
                    int value = data.getInt("value");
                    if(p instanceof VariableIntPower vip) {
                        vip.setValue(value);
                        PowerHolderComponent.syncPower(entity, powerType);
                    } else if(p instanceof CooldownPower cp) {
                        cp.setCooldown(value);
                        PowerHolderComponent.syncPower(entity, powerType);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("grant_power"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE)
            .add("source", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> PowerHolderComponent.KEY.maybeGet(entity).ifPresent(component -> {
                component.addPower(data.get("power"), data.getId("source"));
                component.sync();
            })));
        register(new ActionFactory<>(Apoli.identifier("revoke_power"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE)
            .add("source", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> PowerHolderComponent.KEY.maybeGet(entity).ifPresent(component -> {
                component.removePower(data.get("power"), data.getId("source"));
                component.sync();
            })));
        register(RevokeAllPowersAction.getFactory());
        register(RemovePowerAction.getFactory());
        register(ExplodeAction.getFactory());
        register(new ActionFactory<>(Apoli.identifier("dismount"), new SerializableData(),
            (data, entity) -> entity.stopRiding()));
        register(new ActionFactory<>(Apoli.identifier("passenger_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION, null)
            .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("recursive", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> {
                Consumer<Entity> entityAction = data.get("action");
                Consumer<Pair<Entity, Entity>> bientityAction = data.get("bientity_action");
                Predicate<Pair<Entity, Entity>> cond = data.get("bientity_condition");
                if(!entity.hasPassengers() || (entityAction == null && bientityAction == null)) {
                    return;
                }
                Iterable<Entity> passengers = data.getBoolean("recursive") ? entity.getPassengersDeep() : entity.getPassengerList();
                for(Entity passenger : passengers) {
                    if(cond == null || cond.test(new Pair<>(passenger, entity))) {
                        if (entityAction != null) {
                            entityAction.accept(passenger);
                        }
                        if (bientityAction != null) {
                            bientityAction.accept(new Pair<>(passenger, entity));
                        }
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("riding_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION, null)
            .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("recursive", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> {
                Consumer<Entity> entityAction = data.get("action");
                Consumer<Pair<Entity, Entity>> bientityAction = data.get("bientity_action");
                Predicate<Pair<Entity, Entity>> cond = data.get("bientity_condition");
                if(!entity.hasVehicle() || (entityAction == null && bientityAction == null)) {
                    return;
                }
                if(data.getBoolean("recursive")) {
                    Entity vehicle = entity.getVehicle();
                    while(vehicle != null) {
                        if(cond == null || cond.test(new Pair<>(entity, vehicle))) {
                            if(entityAction != null) {
                                entityAction.accept(vehicle);
                            }
                            if(bientityAction != null) {
                                bientityAction.accept(new Pair<>(entity, vehicle));
                            }
                        }
                        vehicle = vehicle.getVehicle();
                    }
                } else {
                    Entity vehicle = entity.getVehicle();
                    if(cond == null || cond.test(new Pair<>(entity, vehicle))) {
                        if(entityAction != null) {
                            entityAction.accept(vehicle);
                        }
                        if(bientityAction != null) {
                            bientityAction.accept(new Pair<>(entity, vehicle));
                        }
                    }
                }
            }));
        register(AreaOfEffectAction.getFactory());
        register(CraftingTableAction.getFactory());
        register(EnderChestAction.getFactory());
        register(SwingHandAction.getFactory());
        register(RaycastAction.getFactory());
        register(SpawnParticlesAction.getFactory());
        register(ModifyInventoryAction.getFactory());
        register(ReplaceInventoryAction.getFactory());
        register(DropInventoryAction.getFactory());
        register(ModifyDeathTicksAction.getFactory());
        register(ModifyResourceAction.getFactory());
        register(ModifyStatAction.getFactory());
        register(FireProjectileAction.getFactory());
        register(SelectorAction.getFactory());
        register(GrantAdvancementAction.getFactory());
        register(RevokeAdvancementAction.getFactory());
        register(ActionOnEntitySetAction.getFactory());
        register(RandomTeleportAction.getFactory());
        register(ShowToastAction.getFactory());
        register(SpawnEffectCloudAction.getFactory());
    }

    private static void register(ActionFactory<Entity> actionFactory) {
        Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
