package io.github.apace100.apoli.power.factory.action;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.ApoliServer;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.ResourceOperation;
import io.github.apace100.apoli.util.Scheduler;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.FilterableWeightedList;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.PotionUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityActions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ActionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("actions", ApoliDataTypes.ENTITY_ACTIONS),
            (data, entity) -> ((List<ActionFactory<Entity>.Instance>)data.get("actions")).forEach((e) -> e.accept(entity))));
        register(new ActionFactory<>(Apoli.identifier("chance"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION)
            .add("chance", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(new Random().nextFloat() < data.getFloat("chance")) {
                    ((ActionFactory<Entity>.Instance)data.get("action")).accept(entity);
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("if_else"), new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION)
            .add("if_action", ApoliDataTypes.ENTITY_ACTION)
            .add("else_action", ApoliDataTypes.ENTITY_ACTION, null),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    if(((ConditionFactory<LivingEntity>.Instance)data.get("condition")).test((LivingEntity)entity)) {
                        ((ActionFactory<Entity>.Instance)data.get("if_action")).accept(entity);
                    } else {
                        if(data.isPresent("else_action")) {
                            ((ActionFactory<Entity>.Instance)data.get("else_action")).accept(entity);
                        }
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("choice"), new SerializableData()
            .add("actions", SerializableDataType.weightedList(ApoliDataTypes.ENTITY_ACTION)),
            (data, entity) -> {
                FilterableWeightedList<ActionFactory<Entity>.Instance> actionList = (FilterableWeightedList<ActionFactory<Entity>.Instance>)data.get("actions");
                ActionFactory<Entity>.Instance action = actionList.pickRandom(new Random());
                action.accept(entity);
            }));
        register(new ActionFactory<>(Apoli.identifier("if_else_list"), new SerializableData()
            .add("actions", SerializableDataType.list(SerializableDataType.compound(ClassUtil.castClass(Pair.class), new SerializableData()
                .add("action", ApoliDataTypes.ENTITY_ACTION)
                .add("condition", ApoliDataTypes.ENTITY_CONDITION),
                inst -> new Pair<>((ConditionFactory<LivingEntity>.Instance)inst.get("condition"), (ActionFactory<Entity>.Instance)inst.get("action")),
                (data, pair) -> {
                    SerializableData.Instance inst = data.new Instance();
                    inst.set("condition", pair.getLeft());
                    inst.set("action", pair.getRight());
                    return inst;
                }))),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    List<Pair<ConditionFactory<Entity>.Instance, ActionFactory<Entity>.Instance>> actions =
                        (List<Pair<ConditionFactory<Entity>.Instance, ActionFactory<Entity>.Instance>>)data.get("actions");
                    for (Pair<ConditionFactory<Entity>.Instance, ActionFactory<Entity>.Instance> action: actions) {
                        if(action.getLeft().test(entity)) {
                            action.getRight().accept(entity);
                            break;
                        }
                    }
                }
            }));
        Scheduler scheduler = new Scheduler();
        register(new ActionFactory<>(Apoli.identifier("delay"), new SerializableData()
            .add("ticks", SerializableDataTypes.INT)
            .add("action", ApoliDataTypes.ENTITY_ACTION),
            (data, entity) -> {
                ActionFactory<Entity>.Instance action = (ActionFactory<Entity>.Instance)data.get("action");
                scheduler.queue(s -> action.accept(entity), data.getInt("ticks"));
            }));
        register(new ActionFactory<>(Apoli.identifier("nothing"), new SerializableData(),
            (data, entity) -> {}));


        register(new ActionFactory<>(Apoli.identifier("damage"), new SerializableData()
            .add("amount", SerializableDataTypes.FLOAT)
            .add("source", SerializableDataTypes.DAMAGE_SOURCE),
            (data, entity) -> entity.damage((DamageSource)data.get("source"), data.getFloat("amount"))));
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
                    entity.world.playSound(null, (entity).getX(), (entity).getY(), (entity).getZ(), (SoundEvent)data.get("sound"),
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
                if(entity instanceof LivingEntity && !entity.world.isClient) {
                    LivingEntity le = (LivingEntity) entity;
                    if(data.isPresent("effect")) {
                        StatusEffectInstance effect = (StatusEffectInstance)data.get("effect");
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
                if(entity instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity) entity;
                    if(data.isPresent("effect")) {
                        le.removeStatusEffect((StatusEffect)data.get("effect"));
                    } else {
                        le.clearStatusEffects();
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("set_on_fire"), new SerializableData()
            .add("duration", SerializableDataTypes.INT),
            (data, entity) -> {
                entity.setOnFireFor(data.getInt("duration"));
            }));
        register(new ActionFactory<>(Apoli.identifier("add_velocity"), new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .add("space", ApoliDataTypes.SPACE, Space.WORLD)
            .add("set", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> {
                Space space = (Space)data.get("space");
                Vec3f vec = new Vec3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                Vec3d vel;
                Vec3d velH;
                TriConsumer<Float, Float, Float> method = entity::addVelocity;
                if(data.getBoolean("set")) {
                    method = entity::setVelocity;
                }
                switch(space) {
                    case WORLD:
                        method.accept(data.getFloat("x"), data.getFloat("y"), data.getFloat("z"));
                        break;
                    case LOCAL:
                        Space.rotateVectorToBase(entity.getRotationVector(), vec);
                        method.accept(vec.getX(), vec.getY(), vec.getZ());
                        break;
                    case LOCAL_HORIZONTAL:
                        vel = entity.getRotationVector();
                        velH = new Vec3d(vel.x, 0, vel.z);
                        if(velH.lengthSquared() > 0.00005) {
                            velH = velH.normalize();
                            Space.rotateVectorToBase(velH, vec);
                            method.accept(vec.getX(), vec.getY(), vec.getZ());
                        }
                        break;
                    case VELOCITY:
                        Space.rotateVectorToBase(entity.getVelocity(), vec);
                        method.accept(vec.getX(), vec.getY(), vec.getZ());
                        break;
                    case VELOCITY_NORMALIZED:
                        Space.rotateVectorToBase(entity.getVelocity().normalize(), vec);
                        method.accept(vec.getX(), vec.getY(), vec.getZ());
                        break;
                    case VELOCITY_HORIZONTAL:
                        vel = entity.getVelocity();
                        velH = new Vec3d(vel.x, 0, vel.z);
                        Space.rotateVectorToBase(velH, vec);
                        method.accept(vec.getX(), vec.getY(), vec.getZ());
                        break;
                    case VELOCITY_HORIZONTAL_NORMALIZED:
                        vel = entity.getVelocity();
                        velH = new Vec3d(vel.x, 0, vel.z);
                        if(velH.lengthSquared() > 0.00005) {
                            velH = velH.normalize();
                            Space.rotateVectorToBase(velH, vec);
                            method.accept(vec.getX(), vec.getY(), vec.getZ());
                        }
                        break;
                }
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
                        mergedTag.copyFrom((NbtCompound)data.get("tag"));
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
                if(entity instanceof LivingEntity) {
                    LivingEntity le = (LivingEntity) entity;
                    le.setAir(Math.min(le.getAir() + data.getInt("value"), le.getMaxAir()));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("block_action_at"), new SerializableData()
            .add("block_action", ApoliDataTypes.BLOCK_ACTION),
            (data, entity) -> {
                    ((ActionFactory<Triple<World, BlockPos, Direction>>.Instance)data.get("block_action")).accept(
                        Triple.of(entity.world, entity.getBlockPos(), Direction.UP));
            }));
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
                    effects.add((StatusEffectInstance)data.get("effect"));
                }
                if(data.isPresent("effects")) {
                    effects.addAll((List<StatusEffectInstance>)data.get("effects"));
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
                    server.getCommandManager().execute(source, data.getString("command"));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("change_resource"), new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE)
            .add("change", SerializableDataTypes.INT)
            .add("operation", ApoliDataTypes.RESOURCE_OPERATION, ResourceOperation.ADD),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    Power p = component.getPower((PowerType<?>)data.get("resource"));
                    ResourceOperation operation = (ResourceOperation) data.get("operation");
                    int change = data.getInt("change");
                    if(p instanceof VariableIntPower) {
                        VariableIntPower vip = (VariableIntPower)p;
                        if (operation == ResourceOperation.ADD) {
                            int newValue = vip.getValue() + change;
                            vip.setValue(newValue);
                        } else if (operation == ResourceOperation.SET) {
                            vip.setValue(change);
                        }
                        PowerHolderComponent.sync(entity);
                    } else if(p instanceof CooldownPower) {
                        CooldownPower cp = (CooldownPower)p;
                        if (operation == ResourceOperation.ADD) {
                            cp.modify(change);
                        } else if (operation == ResourceOperation.SET) {
                            cp.setCooldown(change);
                        }
                        PowerHolderComponent.sync(entity);
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
            (data, entity) -> {
                entity.fallDistance = data.getFloat("fall_distance");
            }));
        register(new ActionFactory<>(Apoli.identifier("give"), new SerializableData()
            .add("stack", SerializableDataTypes.ITEM_STACK)
            .add("item_action", ApoliDataTypes.ITEM_ACTION, null)
            .add("preferred_slot", SerializableDataTypes.EQUIPMENT_SLOT, null),
            (data, entity) -> {
                if(!entity.world.isClient()) {
                    ItemStack stack = (ItemStack)data.get("stack");
                    if(stack.isEmpty()) {
                        return;
                    }
                    stack = stack.copy();
                    if(data.isPresent("item_action")) {
                        ActionFactory<Pair<World, ItemStack>>.Instance action = (ActionFactory<Pair<World, ItemStack>>.Instance)data.get("item_action");
                        action.accept(new Pair<>(entity.world, stack));
                    }
                    if(data.isPresent("preferred_slot") && entity instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity)entity;
                        EquipmentSlot slot = (EquipmentSlot) data.get("preferred_slot");
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
                    ItemStack stack = ((LivingEntity)entity).getEquippedStack((EquipmentSlot)data.get("equipment_slot"));
                    ActionFactory<Pair<World, ItemStack>>.Instance action = (ActionFactory<Pair<World, ItemStack>>.Instance)data.get("action");
                    action.accept(new Pair<>(entity.world, stack));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("trigger_cooldown"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    Power p = component.getPower((PowerType<?>)data.get("power"));
                    if(p instanceof CooldownPower) {
                        CooldownPower cp = (CooldownPower)p;
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
            (data, entity) -> {
                entity.emitGameEvent((GameEvent)data.get("event"));
            }));
        register(new ActionFactory<>(Apoli.identifier("set_resource"), new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE)
            .add("value", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof LivingEntity) {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    Power p = component.getPower((PowerType<?>)data.get("resource"));
                    int value = data.getInt("value");
                    if(p instanceof VariableIntPower) {
                        VariableIntPower vip = (VariableIntPower)p;
                        vip.setValue(value);
                        PowerHolderComponent.sync(entity);
                    } else if(p instanceof CooldownPower) {
                        CooldownPower cp = (CooldownPower)p;
                        cp.setCooldown(value);
                        PowerHolderComponent.sync(entity);
                    }
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("grant_power"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE)
            .add("source", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> {
                PowerHolderComponent.KEY.maybeGet(entity).ifPresent(component -> {
                    component.addPower((PowerType<?>)data.get("power"), data.getId("source"));
                });
            }));
        register(new ActionFactory<>(Apoli.identifier("revoke_power"), new SerializableData()
            .add("power", ApoliDataTypes.POWER_TYPE)
            .add("source", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> {
                PowerHolderComponent.KEY.maybeGet(entity).ifPresent(component -> {
                    component.removePower((PowerType<?>)data.get("power"), data.getId("source"));
                });
            }));
        register(new ActionFactory<>(Apoli.identifier("explode"), new SerializableData()
            .add("power", SerializableDataTypes.FLOAT)
            .add("destruction_type", SerializableDataType.enumValue(Explosion.DestructionType.class), Explosion.DestructionType.BREAK)
            .add("damage_self", SerializableDataTypes.BOOLEAN, true)
            .add("indestructible", ApoliDataTypes.BLOCK_CONDITION, null)
            .add("destructible", ApoliDataTypes.BLOCK_CONDITION, null)
            .add("create_fire", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> {
                if(entity.world.isClient) {
                    return;
                }
                if(data.isPresent("indestructible")) {
                    Predicate<CachedBlockPosition> blockCondition = (Predicate<CachedBlockPosition>)data.get("indestructible");
                    ExplosionBehavior eb = new ExplosionBehavior() {
                        @Override
                        public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
                            Optional<Float> def = super.getBlastResistance(explosion, world, pos, blockState, fluidState);
                            Optional<Float> ovr = blockCondition.test(
                                new CachedBlockPosition(entity.world, pos, true)) ?
                                Optional.of(Blocks.WATER.getBlastResistance()) : Optional.empty();
                            return ovr.isPresent() ? def.isPresent() ? def.get() > ovr.get() ? def : ovr : ovr : def;
                        }
                    };
                    entity.world.createExplosion(data.getBoolean("damage_self") ? null : entity,
                        entity instanceof LivingEntity ?
                            DamageSource.explosion((LivingEntity)entity) :
                            DamageSource.explosion((LivingEntity) null),
                        eb, entity.getX(), entity.getY(), entity.getZ(),
                        data.getFloat("power"), data.getBoolean("create_fire"),
                        (Explosion.DestructionType) data.get("destruction_type"));
                } else {
                    entity.world.createExplosion(data.getBoolean("damage_self") ? null : entity,
                        entity.getX(), entity.getY(), entity.getZ(),
                        data.getFloat("power"), data.getBoolean("create_fire"),
                        (Explosion.DestructionType) data.get("destruction_type"));
                }
            }));
        register(new ActionFactory<>(Apoli.identifier("dismount"), new SerializableData(),
            (data, entity) -> entity.stopRiding()));
        register(new ActionFactory<>(Apoli.identifier("passenger_action"), new SerializableData()
            .add("action", ApoliDataTypes.ENTITY_ACTION, null)
            .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null)
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("recursive", SerializableDataTypes.BOOLEAN, false),
            (data, entity) -> {
                Consumer<Entity> entityAction = (Consumer<Entity>) data.get("action");
                Consumer<Pair<Entity, Entity>> bientityAction = (Consumer<Pair<Entity, Entity>>) data.get("bientity_action");
                Predicate<Pair<Entity, Entity>> cond = (Predicate<Pair<Entity, Entity>>) data.get("bientity_condition");
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
                Consumer<Entity> entityAction = (Consumer<Entity>) data.get("action");
                Consumer<Pair<Entity, Entity>> bientityAction = (Consumer<Pair<Entity, Entity>>) data.get("bientity_action");
                Predicate<Pair<Entity, Entity>> cond = (Predicate<Pair<Entity, Entity>>) data.get("bientity_condition");
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
    }

    private static void register(ActionFactory<Entity> actionFactory) {
        Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
