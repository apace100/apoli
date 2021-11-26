package io.github.apace100.apoli.power.factory.condition;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.access.MovingEntity;
import io.github.apace100.apoli.access.SubmergableEntity;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.apoli.power.*;
import io.github.apace100.apoli.power.factory.condition.entity.ElytraFlightPossibleCondition;
import io.github.apace100.apoli.power.factory.condition.entity.RaycastCondition;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.Shape;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
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

public class EntityConditions {

    @SuppressWarnings("unchecked")
    public static void register() {
        register(new ConditionFactory<>(Apoli.identifier("constant"), new SerializableData()
            .add("value", SerializableDataTypes.BOOLEAN),
            (data, entity) -> data.getBoolean("value")));
        register(new ConditionFactory<>(Apoli.identifier("and"), new SerializableData()
            .add("conditions", ApoliDataTypes.ENTITY_CONDITIONS),
            (data, entity) -> ((List<ConditionFactory<Entity>.Instance>)data.get("conditions")).stream().allMatch(
                condition -> condition.test(entity)
            )));
        register(new ConditionFactory<>(Apoli.identifier("or"), new SerializableData()
            .add("conditions", ApoliDataTypes.ENTITY_CONDITIONS),
            (data, entity) -> ((List<ConditionFactory<Entity>.Instance>)data.get("conditions")).stream().anyMatch(
                condition -> condition.test(entity)
            )));
        register(new ConditionFactory<>(Apoli.identifier("block_collision"), new SerializableData()
            .add("offset_x", SerializableDataTypes.FLOAT)
            .add("offset_y", SerializableDataTypes.FLOAT)
            .add("offset_z", SerializableDataTypes.FLOAT),
            (data, entity) -> entity.world.getBlockCollisions(entity,
                entity.getBoundingBox().offset(
                    data.getFloat("offset_x") * entity.getBoundingBox().getXLength(),
                    data.getFloat("offset_y") * entity.getBoundingBox().getYLength(),
                    data.getFloat("offset_z") * entity.getBoundingBox().getZLength())
            ).iterator().hasNext()));
        register(new ConditionFactory<>(Apoli.identifier("brightness"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity.getBrightnessAtEyes(), data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("daytime"), new SerializableData(), (data, entity) -> entity.world.getTimeOfDay() % 24000L < 13000L));
        register(new ConditionFactory<>(Apoli.identifier("time_of_day"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT), (data, entity) ->
            ((Comparison)data.get("comparison")).compare(entity.world.getTimeOfDay() % 24000L, data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("fall_flying"), new SerializableData(), (data, entity) -> entity instanceof LivingEntity && ((LivingEntity) entity).isFallFlying()));
        register(new ConditionFactory<>(Apoli.identifier("exposed_to_sun"), new SerializableData(), (data, entity) -> {
            if (entity.world.isDay() && !((EntityAccessor) entity).callIsBeingRainedOn()) {
                float f = entity.getBrightnessAtEyes();
                BlockPos blockPos = entity.getVehicle() instanceof BoatEntity ? (new BlockPos(entity.getX(), (double) Math.round(entity.getY()), entity.getZ())).up() : new BlockPos(entity.getX(), (double) Math.round(entity.getY()), entity.getZ());
                return f > 0.5F && entity.world.isSkyVisible(blockPos);
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("in_rain"), new SerializableData(), (data, entity) -> ((EntityAccessor) entity).callIsBeingRainedOn()));
        register(new ConditionFactory<>(Apoli.identifier("invisible"), new SerializableData(), (data, entity) -> entity.isInvisible()));
        register(new ConditionFactory<>(Apoli.identifier("on_fire"), new SerializableData(), (data, entity) -> entity.isOnFire()));
        register(new ConditionFactory<>(Apoli.identifier("exposed_to_sky"), new SerializableData(), (data, entity) -> {
            BlockPos blockPos = entity.getVehicle() instanceof BoatEntity ? (new BlockPos(entity.getX(), (double) Math.round(entity.getY()), entity.getZ())).up() : new BlockPos(entity.getX(), (double) Math.round(entity.getY()), entity.getZ());
            return entity.world.isSkyVisible(blockPos);
        }));
        register(new ConditionFactory<>(Apoli.identifier("sneaking"), new SerializableData(), (data, entity) -> entity.isSneaking()));
        register(new ConditionFactory<>(Apoli.identifier("sprinting"), new SerializableData(), (data, entity) -> entity.isSprinting()));
        register(new ConditionFactory<>(Apoli.identifier("power_active"), new SerializableData().add("power", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> ((PowerTypeReference<?>)data.get("power")).isActive(entity)));
        register(new ConditionFactory<>(Apoli.identifier("status_effect"), new SerializableData()
            .add("effect", SerializableDataTypes.STATUS_EFFECT)
            .add("min_amplifier", SerializableDataTypes.INT, 0)
            .add("max_amplifier", SerializableDataTypes.INT, Integer.MAX_VALUE)
            .add("min_duration", SerializableDataTypes.INT, 0)
            .add("max_duration", SerializableDataTypes.INT, Integer.MAX_VALUE),
            (data, entity) -> {
                StatusEffect effect = data.get("effect");
                if(entity instanceof LivingEntity living) {
                    if (living.hasStatusEffect(effect)) {
                        StatusEffectInstance instance = living.getStatusEffect(effect);
                        return instance.getDuration() <= data.getInt("max_duration") && instance.getDuration() >= data.getInt("min_duration")
                            && instance.getAmplifier() <= data.getInt("max_amplifier") && instance.getAmplifier() >= data.getInt("min_amplifier");
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("submerged_in"), new SerializableData().add("fluid", SerializableDataTypes.FLUID_TAG),
            (data, entity) -> ((SubmergableEntity)entity).isSubmergedInLoosely(data.get("fluid"))));
        register(new ConditionFactory<>(Apoli.identifier("fluid_height"), new SerializableData()
            .add("fluid", SerializableDataTypes.FLUID_TAG)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(((SubmergableEntity)entity).getFluidHeightLoosely(data.get("fluid")), data.getDouble("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("power"), new SerializableData()
            .add("power", SerializableDataTypes.IDENTIFIER)
            .add("source", SerializableDataTypes.IDENTIFIER, null),
            (data, entity) -> {
                try {
                    PowerType<?> powerType = PowerTypeRegistry.get(data.getId("power"));
                    if(data.isPresent("source")) {
                        return PowerHolderComponent.KEY.get(entity).hasPower(powerType, data.getId("source"));
                    }
                    return PowerHolderComponent.KEY.get(entity).hasPower(powerType);
                } catch(IllegalArgumentException e) {
                    return false;
                }
            }));
        register(new ConditionFactory<>(Apoli.identifier("food_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    return ((Comparison)data.get("comparison")).compare(((PlayerEntity)entity).getHungerManager().getFoodLevel(), data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("saturation_level"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    return ((Comparison) data.get("comparison")).compare(((PlayerEntity)entity).getHungerManager().getSaturationLevel(), data.getFloat("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("on_block"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION, null),
            (data, entity) -> entity.isOnGround() &&
                (!data.isPresent("block_condition") || ((ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition")).test(
                    new CachedBlockPosition(entity.world, new BlockPos(entity.getX(), entity.getBoundingBox().minY - 0.5000001D, entity.getZ()), true)))));
        register(new ConditionFactory<>(Apoli.identifier("equipped_item"), new SerializableData()
            .add("equipment_slot", SerializableDataTypes.EQUIPMENT_SLOT)
            .add("item_condition", ApoliDataTypes.ITEM_CONDITION),
            (data, entity) -> entity instanceof LivingEntity && ((ConditionFactory<ItemStack>.Instance) data.get("item_condition")).test(
                ((LivingEntity) entity).getEquippedStack(data.get("equipment_slot")))));
        register(new ConditionFactory<>(Apoli.identifier("attribute"), new SerializableData()
            .add("attribute", SerializableDataTypes.ATTRIBUTE)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.DOUBLE),
            (data, entity) -> {
                double attrValue = 0F;
                if(entity instanceof LivingEntity living) {
                    EntityAttributeInstance attributeInstance = living.getAttributeInstance(data.get("attribute"));
                    if(attributeInstance != null) {
                        attrValue = attributeInstance.getValue();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(attrValue, data.getDouble("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("swimming"), new SerializableData(), (data, entity) -> entity.isSwimming()));
        register(new ConditionFactory<>(Apoli.identifier("resource"), new SerializableData()
            .add("resource", ApoliDataTypes.POWER_TYPE)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                int resourceValue = 0;
                PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                Power p = component.getPower((PowerType<?>)data.get("resource"));
                if(p instanceof VariableIntPower) {
                    resourceValue = ((VariableIntPower)p).getValue();
                } else if(p instanceof CooldownPower) {
                    resourceValue = ((CooldownPower)p).getRemainingTicks();
                }
                return ((Comparison)data.get("comparison")).compare(resourceValue, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("air"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity.getAir(), data.getInt("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("in_block"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION),
            (data, entity) ->((ConditionFactory<CachedBlockPosition>.Instance)data.get("block_condition")).test(
                new CachedBlockPosition(entity.world, entity.getBlockPos(), true))));
        register(new ConditionFactory<>(Apoli.identifier("block_in_radius"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("radius", SerializableDataTypes.INT)
            .add("shape", SerializableDataType.enumValue(Shape.class), Shape.CUBE)
            .add("compare_to", SerializableDataTypes.INT, 1)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL),
            (data, entity) -> {
                Predicate<CachedBlockPosition> blockCondition = data.get("block_condition");
                int stopAt = -1;
                Comparison comparison = data.get("comparison");
                int compareTo = data.getInt("compare_to");
                switch(comparison) {
                    case EQUAL: case LESS_THAN_OR_EQUAL: case GREATER_THAN:
                        stopAt = compareTo + 1;
                        break;
                    case LESS_THAN: case GREATER_THAN_OR_EQUAL:
                        stopAt = compareTo;
                        break;
                }
                int count = 0;
                for(BlockPos pos : Shape.getPositions(entity.getBlockPos(), data.get("shape"), data.getInt("radius"))) {
                    if(blockCondition.test(new CachedBlockPosition(entity.world, pos, true))) {
                        count++;
                        if(count == stopAt) {
                            break;
                        }
                    }
                }
                return comparison.compare(count, compareTo);
            }));
        DistanceFromCoordinatesConditionRegistry.registerEntityCondition(EntityConditions::register);
        register(new ConditionFactory<>(Apoli.identifier("dimension"), new SerializableData()
            .add("dimension", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> entity.world.getRegistryKey() == RegistryKey.of(Registry.WORLD_KEY, data.getId("dimension"))));
        register(new ConditionFactory<>(Apoli.identifier("xp_levels"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    return ((Comparison)data.get("comparison")).compare(((PlayerEntity)entity).experienceLevel, data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("xp_points"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    return ((Comparison)data.get("comparison")).compare(((PlayerEntity)entity).totalExperience, data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("health"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity instanceof LivingEntity ? ((LivingEntity)entity).getHealth() : 0f, data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("relative_health"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> {
                float health = 0f;
                if(entity instanceof LivingEntity living) {
                    health = living.getHealth() / living.getMaxHealth();
                }
                return ((Comparison)data.get("comparison")).compare(health, data.getFloat("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("biome"), new SerializableData()
            .add("biome", SerializableDataTypes.IDENTIFIER, null)
            .add("biomes", SerializableDataTypes.IDENTIFIERS, null)
            .add("condition", ApoliDataTypes.BIOME_CONDITION, null),
            (data, entity) -> {
                Biome biome = entity.world.getBiome(entity.getBlockPos());
                ConditionFactory<Biome>.Instance condition = data.get("condition");
                if(data.isPresent("biome") || data.isPresent("biomes")) {
                    Identifier biomeId = entity.world.getRegistryManager().get(Registry.BIOME_KEY).getId(biome);
                    if(data.isPresent("biome") && biomeId.equals(data.getId("biome"))) {
                        return condition == null || condition.test(biome);
                    }
                    if(data.isPresent("biomes") && ((List<Identifier>)data.get("biomes")).contains(biomeId)) {
                        return condition == null || condition.test(biome);
                    }
                    return false;
                }
                return condition == null || condition.test(biome);
            }));
        register(new ConditionFactory<>(Apoli.identifier("entity_type"), new SerializableData()
            .add("entity_type", SerializableDataTypes.ENTITY_TYPE),
            (data, entity) -> entity.getType() == data.get("entity_type")));
        register(new ConditionFactory<>(Apoli.identifier("scoreboard"), new SerializableData()
            .add("objective", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
            (data, entity) -> {
                if(entity instanceof PlayerEntity) {
                    PlayerEntity player = (PlayerEntity)entity;
                    Scoreboard scoreboard = player.getScoreboard();
                    ScoreboardObjective objective = scoreboard.getObjective(data.getString("objective"));
                    String playerName = player.getName().asString();

                    if (scoreboard.playerHasObjective(playerName, objective)) {
                        int value = scoreboard.getPlayerScore(playerName, objective).getScore();
                        return ((Comparison)data.get("comparison")).compare(value, data.getInt("compare_to"));
                    }
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("command"), new SerializableData()
            .add("command", SerializableDataTypes.STRING)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT),
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
                        server,
                        entity);
                    int output = server.getCommandManager().execute(source, data.getString("command"));
                    return ((Comparison)data.get("comparison")).compare(output, data.getInt("compare_to"));
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("predicate"), new SerializableData()
            .add("predicate", SerializableDataTypes.IDENTIFIER),
            (data, entity) -> {
                MinecraftServer server = entity.world.getServer();
                if (server != null) {
                    LootCondition lootCondition = server.getPredicateManager().get(data.get("predicate"));
                    if (lootCondition != null) {
                        LootContext.Builder lootBuilder = (new LootContext.Builder((ServerWorld) entity.world))
                            .parameter(LootContextParameters.ORIGIN, entity.getPos())
                            .optionalParameter(LootContextParameters.THIS_ENTITY, entity);
                        return lootCondition.test(lootBuilder.build(LootContextTypes.COMMAND));
                    }
                }
                return false;
            }
        ));
        register(new ConditionFactory<>(Apoli.identifier("fall_distance"), new SerializableData()
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.FLOAT),
            (data, entity) -> ((Comparison)data.get("comparison")).compare(entity.fallDistance, data.getFloat("compare_to"))));
        register(new ConditionFactory<>(Apoli.identifier("collided_horizontally"), new SerializableData(),
            (data, entity) -> entity.horizontalCollision));
        register(new ConditionFactory<>(Apoli.identifier("in_block_anywhere"), new SerializableData()
            .add("block_condition", ApoliDataTypes.BLOCK_CONDITION)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                Predicate<CachedBlockPosition> blockCondition = data.get("block_condition");
                int stopAt = -1;
                Comparison comparison = data.get("comparison");
                int compareTo = data.getInt("compare_to");
                switch(comparison) {
                    case EQUAL: case LESS_THAN_OR_EQUAL: case GREATER_THAN: case NOT_EQUAL:
                        stopAt = compareTo + 1;
                        break;
                    case LESS_THAN: case GREATER_THAN_OR_EQUAL:
                        stopAt = compareTo;
                        break;
                }
                int count = 0;
                Box box = entity.getBoundingBox();
                BlockPos blockPos = new BlockPos(box.minX + 0.001D, box.minY + 0.001D, box.minZ + 0.001D);
                BlockPos blockPos2 = new BlockPos(box.maxX - 0.001D, box.maxY - 0.001D, box.maxZ - 0.001D);
                BlockPos.Mutable mutable = new BlockPos.Mutable();
                for(int i = blockPos.getX(); i <= blockPos2.getX() && count < stopAt; ++i) {
                    for(int j = blockPos.getY(); j <= blockPos2.getY() && count < stopAt; ++j) {
                        for(int k = blockPos.getZ(); k <= blockPos2.getZ() && count < stopAt; ++k) {
                            mutable.set(i, j, k);
                            if(blockCondition.test(new CachedBlockPosition(entity.world, mutable, true))) {
                                count++;
                            }
                        }
                    }
                }
                return comparison.compare(count, compareTo);}));
        register(new ConditionFactory<>(Apoli.identifier("entity_group"), new SerializableData()
            .add("group", SerializableDataTypes.ENTITY_GROUP),
            (data, entity) -> entity instanceof LivingEntity && ((LivingEntity) entity).getGroup() == data.get("group")));
        register(new ConditionFactory<>(Apoli.identifier("in_tag"), new SerializableData()
            .add("tag", SerializableDataTypes.ENTITY_TAG),
            (data, entity) -> ((Tag<EntityType<?>>)data.get("tag")).contains(entity.getType())));
        register(new ConditionFactory<>(Apoli.identifier("climbing"), new SerializableData(), (data, entity) -> entity instanceof LivingEntity && ((LivingEntity)entity).isClimbing()));
        register(new ConditionFactory<>(Apoli.identifier("tamed"), new SerializableData(), (data, entity) -> {
            if(entity instanceof TameableEntity) {
                return ((TameableEntity)entity).isTamed();
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("using_item"), new SerializableData()
            .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null), (data, entity) -> {
            if(entity instanceof LivingEntity living) {
                if (living.isUsingItem()) {
                    ConditionFactory<ItemStack>.Instance condition = data.get("item_condition");
                    if (condition != null) {
                        Hand activeHand = living.getActiveHand();
                        ItemStack handStack = living.getStackInHand(activeHand);
                        return condition.test(handStack);
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }));
        register(new ConditionFactory<>(Apoli.identifier("moving"), new SerializableData(),
            (data, entity) -> ((MovingEntity)entity).isMoving()));
        register(new ConditionFactory<>(Apoli.identifier("enchantment"), new SerializableData()
            .add("enchantment", SerializableDataTypes.ENCHANTMENT)
            .add("comparison", ApoliDataTypes.COMPARISON)
            .add("compare_to", SerializableDataTypes.INT)
            .add("calculation", SerializableDataTypes.STRING, "sum"),
            (data, entity) -> {
                int value = 0;
                if(entity instanceof LivingEntity le) {
                    Enchantment enchantment = data.get("enchantment");
                    String calculation = data.getString("calculation");
                    switch(calculation) {
                        case "sum":
                            for(ItemStack stack : enchantment.getEquipment(le).values()) {
                                value += EnchantmentHelper.getLevel(enchantment, stack);
                            }
                            break;
                        case "max":
                            value = EnchantmentHelper.getEquipmentLevel(enchantment, le);
                            break;
                        default:
                            Apoli.LOGGER.error("Error in \"enchantment\" entity condition, undefined calculation type: \"" + calculation + "\".");
                            break;
                    }
                }
                return ((Comparison)data.get("comparison")).compare(value, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("riding"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            (data, entity) -> {
                if(entity.hasVehicle()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Pair<Entity, Entity>> condition = data.get("bientity_condition");
                        Entity vehicle = entity.getVehicle();
                        return condition.test(new Pair<>(entity, vehicle));
                    }
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("riding_root"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null),
            (data, entity) -> {
                if(entity.hasVehicle()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Pair<Entity, Entity>> condition = data.get("bientity_condition");
                        Entity vehicle = entity.getRootVehicle();
                        return condition.test(new Pair<>(entity, vehicle));
                    }
                    return true;
                }
                return false;
            }));
        register(new ConditionFactory<>(Apoli.identifier("riding_recursive"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                int count = 0;
                if(entity.hasVehicle()) {
                    Predicate<Pair<Entity, Entity>> cond = data.get("bientity_condition");
                    Entity vehicle = entity.getVehicle();
                    while(vehicle != null) {
                        if(cond == null || cond.test(new Pair<>(entity, vehicle))) {
                            count++;
                        }
                        vehicle = vehicle.getVehicle();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(count, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("living"), new SerializableData(), (data, entity) -> entity instanceof LivingEntity));
        register(new ConditionFactory<>(Apoli.identifier("passenger"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                int count = 0;
                if(entity.hasPassengers()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Pair<Entity, Entity>> condition = data.get("bientity_condition");
                        count = (int)entity.getPassengerList().stream().filter(e -> condition.test(new Pair<>(e, entity))).count();
                    } else {
                        count = entity.getPassengerList().size();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(count, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("passenger_recursive"), new SerializableData()
            .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION, null)
            .add("comparison", ApoliDataTypes.COMPARISON, Comparison.GREATER_THAN_OR_EQUAL)
            .add("compare_to", SerializableDataTypes.INT, 1),
            (data, entity) -> {
                int count = 0;
                if(entity.hasPassengers()) {
                    if(data.isPresent("bientity_condition")) {
                        Predicate<Pair<Entity, Entity>> condition = data.get("bientity_condition");
                        List<Entity> passengers = entity.getPassengerList();
                        count = (int)passengers.stream().flatMap(Entity::streamSelfAndPassengers).filter(e -> condition.test(new Pair<>(e, entity))).count();
                    } else {
                        count = (int)entity.getPassengerList().stream().flatMap(Entity::streamSelfAndPassengers).count();
                    }
                }
                return ((Comparison)data.get("comparison")).compare(count, data.getInt("compare_to"));
            }));
        register(new ConditionFactory<>(Apoli.identifier("nbt"), new SerializableData()
            .add("nbt", SerializableDataTypes.NBT),
            (data, entity) -> {
                NbtCompound nbt = new NbtCompound();
                entity.writeNbt(nbt);
                return NbtHelper.matches(data.get("nbt"), nbt, true);
            }));
        register(new ConditionFactory<>(Apoli.identifier("exists"), new SerializableData(), (data, entity) -> entity != null));
        register(new ConditionFactory<>(Apoli.identifier("creative_flying"), new SerializableData(),
            (data, entity) -> entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().flying));
        register(new ConditionFactory<>(Apoli.identifier("power_type"), new SerializableData()
            .add("power_type", ApoliDataTypes.POWER_TYPE),
            (data, entity) -> {
                PowerTypeReference<?> powerTypeReference = data.get("power_type");
                PowerType<?> powerType = powerTypeReference.getReferencedPowerType();
                return PowerHolderComponent.KEY.maybeGet(entity).map(phc -> phc.getPowerTypes(true).contains(powerType)).orElse(false);
            }));
        register(new ConditionFactory<>(Apoli.identifier("ability"), new SerializableData()
            .add("ability", ApoliDataTypes.PLAYER_ABILITY),
            (data, entity) -> {
                if(entity instanceof PlayerEntity && !entity.world.isClient) {
                    return ((PlayerAbility) data.get("ability")).isEnabledFor((PlayerEntity) entity);
                }
                return false;
            }));
        register(RaycastCondition.getFactory());
        register(ElytraFlightPossibleCondition.getFactory());
    }

    private static void register(ConditionFactory<Entity> conditionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
