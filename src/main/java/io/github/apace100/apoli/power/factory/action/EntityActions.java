package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.entity.AreaOfEffectAction;
import io.github.apace100.apoli.power.factory.action.entity.CraftingTableAction;
import io.github.apace100.apoli.power.factory.action.entity.EnderChestAction;
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
import net.minecraft.entity.*;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;
//import net.minecraft.util.math.Vec3f;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(AndAction.getFactory(ApoliDataTypes.ENTITY_ACTIONS));
        register(ChanceAction.getFactory(ApoliDataTypes.ENTITY_ACTION));
        register(IfElseAction.getFactory(ApoliDataTypes.ENTITY_ACTION, ApoliDataTypes.ENTITY_CONDITION));
        register(ChoiceAction.getFactory(ApoliDataTypes.ENTITY_ACTION));
        register(IfElseListAction.getFactory(ApoliDataTypes.ENTITY_ACTION, ApoliDataTypes.ENTITY_CONDITION));
        register(DelayAction.getFactory(ApoliDataTypes.ENTITY_ACTION));
        register(NothingAction.getFactory());
        register(SideAction.getFactory(ApoliDataTypes.ENTITY_ACTION, entity -> !entity.world.isClient));

        register(new ActionFactory<>(Apoli.identifier("damage"), new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT)
            .add("source", SerializableDataTypes.DAMAGE_SOURCE),
            (data, entity) -> entity.damage(data.get("source"), data.getFloat("amount"))));
        register(new ActionFactory<>(Apoli.identifier("heal"), new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    ((LivingEntity)entity).heal(data.getFloat("amount"));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("play_sound"), new SerializableData()
                .add("sound", SerializableDataTypes.SOUND_EVENT)
                .add("volume", SerializableDataTypes.FLOAT, 1F)
                .add("pitch", SerializableDataTypes.FLOAT, 1F),
                (data, entity) -> {
                    SoundCategory category;
                    if(entity instanceof PlayerEntity) {
                        category = SoundCategory.PLAYERS;
                    } else
                    if(entity instanceof HostileEntity) {
                        category = SoundCategory.HOSTILE;
                    } else {
                        category = SoundCategory.NEUTRAL;
                    }
                    entity.world.playSound(null, (entity).getX(), (entity).getY(), (entity).getZ(), data.get("sound"),
                        category, data.getFloat("volume"), data.getFloat("pitch"));
                }));
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
                if(entity instanceof LivingEntity le && !entity.world.isClient) {
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
            .add("effect", SerializableDataTypes.STATUS_EFFECT, null),
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
                    && (entity.world.isClient ?
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
        register(new ActionFactory<>(Apoli.identifier("spawn_entity"), new SerializableData()
            .add("entity_type", SerializableDataTypes.ENTITY_TYPE)
            .add("tag", SerializableDataTypes.NBT, null)
            .add("entity_action", ApoliDataTypes.ENTITY_ACTION, null),
            (data, entity) -> {
                Entity e = ((EntityType<?>)data.get("entity_type")).create(entity.world);
                if(e != null) {
                    e.refreshPositionAndAngles(entity.getPos().x, entity.getPos().y, entity.getPos().z, entity.getYaw(), entity.getPitch());
                    if(data.isPresent("tag")) {
                        NbtCompound mergedTag = e.writeNbt(new NbtCompound());
                        mergedTag.copyFrom(data.get("tag"));
                        e.readNbt(mergedTag);
                    }

                    entity.world.spawnEntity(e);
                    if(data.isPresent("entity_action")) {
                        ((ActionFactory<Entity>.Instance)data.get("entity_action")).accept(e);
                    }
                }
            }));
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
                Triple.of(entity.world, entity.getBlockPos(), Direction.UP))));
        register(new ActionFactory<>(Apoli.identifier("spawn_effect_cloud"), new SerializableData()
            .add("radius", SerializableDataTypes.FLOAT, 3.0F)
            .add("radius_on_use", SerializableDataTypes.FLOAT, -0.5F)
            .add("wait_time", SerializableDataTypes.INT, 10)
            .add("effect", SerializableDataTypes.STATUS_EFFECT_INSTANCE, null)
            .add("effects", SerializableDataTypes.STATUS_EFFECT_INSTANCES, null),
            (data, entity) -> {
                AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(entity.world, entity.getX(), entity.getY(), entity.getZ());
                if (entity instanceof LivingEntity) {
                    areaEffectCloudEntity.setOwner((LivingEntity)entity);
                }
                areaEffectCloudEntity.setRadius(data.getFloat("radius"));
                areaEffectCloudEntity.setRadiusOnUse(data.getFloat("radius_on_use"));
                areaEffectCloudEntity.setWaitTime(data.getInt("wait_time"));
                areaEffectCloudEntity.setRadiusGrowth(-areaEffectCloudEntity.getRadius() / (float)areaEffectCloudEntity.getDuration());
                List<StatusEffectInstance> effects = new LinkedList<>();
                if(data.isPresent("effect")) {
                    effects.add(data.get("effect"));
                }
                if(data.isPresent("effects")) {
                    effects.addAll(data.get("effects"));
                }
                areaEffectCloudEntity.setColor(PotionUtil.getColor(effects));
                effects.forEach(areaEffectCloudEntity::addEffect);

                entity.world.spawnEntity(areaEffectCloudEntity);
            }));
        register(new ActionFactory<>(Apoli.identifier("extinguish"), new SerializableData(),
            (data, entity) -> entity.extinguish()));
        register(new ActionFactory<>(Apoli.identifier("execute_command"), new SerializableData()
            .add("command", SerializableDataTypes.STRING),
            (data, entity) -> {
                MinecraftServer server = entity.world.getServer();
                if(server != null) {
                    boolean validOutput = !(entity instanceof ServerPlayerEntity) || ((ServerPlayerEntity)entity).networkHandler != null;
                    ServerCommandSource source = new ServerCommandSource(
                        Apoli.config.executeCommand.showOutput && validOutput ? entity : CommandOutput.DUMMY,
                        entity.getPos(),
                        entity.getRotationClient(),
                        entity.world instanceof ServerWorld ? (ServerWorld)entity.world : null,
                        Apoli.config.executeCommand.permissionLevel,
                        entity.getName().getString(),
                        entity.getDisplayName(),
                        entity.world.getServer(),
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
                    PowerType<?> powerType = data.get("resource");
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
        register(new ActionFactory<>(Apoli.identifier("give"), new SerializableData()
            .add("stack", SerializableDataTypes.ITEM_STACK)
            .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
            .add("preferred_slot", SerializableDataTypes.EQUIPMENT_SLOT, null),
            (data, entity) -> {
                if(!entity.world.isClient()) {
                    ItemStack stack = data.get("stack");
                    if(stack.isEmpty()) {
                        return;
                    }
                    stack = stack.copy();
                    if(data.isPresent("item_action")) {
                        ActionFactory<Pair<World, ItemStack>>.Instance action = data.get("item_action");
                        action.accept(new Pair<>(entity.world, stack));
                    }
                    if(data.isPresent("preferred_slot") && entity instanceof LivingEntity living) {
                        EquipmentSlot slot = data.get("preferred_slot");
                        ItemStack stackInSlot = living.getEquippedStack(slot);
                        if(stackInSlot.isEmpty()) {
                            living.equipStack(slot, stack);
                            return;
                        } else
                        if(ItemStack.canCombine(stackInSlot, stack) && stackInSlot.getCount() < stackInSlot.getMaxCount()) {
                            int fit = Math.min(stackInSlot.getMaxCount() - stackInSlot.getCount(), stack.getCount());
                            stackInSlot.increment(fit);
                            stack.decrement(fit);
                            if(stack.isEmpty()) {
                                return;
                            }
                        }
                    }
                    if(entity instanceof PlayerEntity) {
                        ((PlayerEntity)entity).getInventory().offerOrDrop(stack);
                    } else {
                        entity.world.spawnEntity(new ItemEntity(entity.world, entity.getX(), entity.getY(), entity.getZ(), stack));
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("equipped_item_action"), new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT)
            .add("action", ApoliDataTypes.ITEM_ACTION),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    ItemStack stack = ((LivingEntity)entity).getEquippedStack(data.get("equipment_slot"));
                    ActionFactory<Pair<World, ItemStack>>.Instance action = data.get("action");
                    action.accept(new Pair<>(entity.world, stack));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("trigger_cooldown"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    Power p = component.getPower((PowerType<?>)data.get("power"));
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
                    Power p = component.getPower((PowerType<?>)data.get("power"));
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
                    PowerType<?> powerType = data.get("resource");
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
        register(SelectorAction.getFactory());
    }

    private static void register(ActionFactory<Entity> actionFactory) {
        Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
