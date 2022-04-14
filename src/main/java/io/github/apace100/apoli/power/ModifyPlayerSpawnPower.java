package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureStart;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;

import java.util.Optional;

public class ModifyPlayerSpawnPower extends Power {
    public final RegistryKey<World> dimension;
    public final float dimensionDistanceMultiplier;
    public final Identifier biomeId;
    public final String spawnStrategy;
    public final RegistryKey<ConfiguredStructureFeature<?, ?>> structure;
    public final SoundEvent spawnSound;

    public ModifyPlayerSpawnPower(PowerType<?> type, LivingEntity entity, RegistryKey<World> dimension, float dimensionDistanceMultiplier, Identifier biomeId, String spawnStrategy, RegistryKey<ConfiguredStructureFeature<?, ?>> structure, SoundEvent spawnSound) {
        super(type, entity);
        this.dimension = dimension;
        this.dimensionDistanceMultiplier = dimensionDistanceMultiplier;
        this.biomeId = biomeId;
        this.spawnStrategy = spawnStrategy;
        this.structure = structure;
        this.spawnSound = spawnSound;
    }

    public void teleportToModifiedSpawn() {
        if(entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entity;
            Pair<ServerWorld, BlockPos> spawn = getSpawn(false);
            if(spawn != null) {
                Vec3d tpPos = Dismounting.findRespawnPos(EntityType.PLAYER, spawn.getLeft(), spawn.getRight(), true);
                if(tpPos != null) {
                    serverPlayer.teleport(spawn.getLeft(), tpPos.x, tpPos.y, tpPos.z, entity.getPitch(), entity.getYaw());
                } else {
                    serverPlayer.teleport(spawn.getLeft(), spawn.getRight().getX(), spawn.getRight().getY(), spawn.getRight().getZ(), entity.getPitch(), entity.getYaw());
                    Apoli.LOGGER.warn("Could not spawn player with `ModifySpawnPower` at the desired location.");
                }
            }
        }
    }

    @Override
    public void onRemoved() {
        if(entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entity;
            if(!serverPlayer.isDisconnected() && serverPlayer.getSpawnPointPosition() != null && serverPlayer.isSpawnForced()) {
                serverPlayer.setSpawnPoint(World.OVERWORLD, null, 0F, false, false);
            }
        }
    }

    public Pair<ServerWorld, BlockPos> getSpawn(boolean isSpawnObstructed) {
        if(entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entity;
            ServerWorld world = serverPlayer.getServer().getWorld(dimension);
            BlockPos regularSpawn = serverPlayer.getServer().getWorld(World.OVERWORLD).getSpawnPos();
            BlockPos spawnToDimPos;
            int iterations = (world.getLogicalHeight() / 2) - 8;
            int center = world.getLogicalHeight() / 2;
            BlockPos.Mutable mutable;
            Vec3d tpPos;
            int range = 64;

            switch(spawnStrategy) {
                case "center":
                    spawnToDimPos = new BlockPos(0, center, 0);
                    break;

                case "default":
                    if(dimensionDistanceMultiplier != 0) {
                        spawnToDimPos = new BlockPos(regularSpawn.getX() * dimensionDistanceMultiplier, regularSpawn.getY(), regularSpawn.getZ() * dimensionDistanceMultiplier);
                    } else {
                        spawnToDimPos = new BlockPos(regularSpawn.getX(), regularSpawn.getY(), regularSpawn.getZ());
                    }
                    break;

                default:
                    Apoli.LOGGER.warn("This case does nothing. The game crashes if there is no spawn strategy set");
                    if(dimensionDistanceMultiplier != 0) {
                        spawnToDimPos = new BlockPos(regularSpawn.getX() * dimensionDistanceMultiplier, regularSpawn.getY(), regularSpawn.getZ() * dimensionDistanceMultiplier);
                    } else {
                        spawnToDimPos = new BlockPos(regularSpawn.getX(), regularSpawn.getY(), regularSpawn.getZ());
                    }
            }

            if(biomeId != null) {
                Optional<Biome> biomeOptional = world.getRegistryManager().get(Registry.BIOME_KEY).getOrEmpty(biomeId);
                if(biomeOptional.isPresent()) {
                    com.mojang.datafixers.util.Pair<BlockPos, RegistryEntry<Biome>> biomePos = world.locateBiome(b -> b.value() == biomeOptional.get(), spawnToDimPos, 6400, 8);
                    if(biomePos != null) {
                        spawnToDimPos = biomePos.getFirst();
                    } else {
                        Apoli.LOGGER.warn("Could not find biome \"" + biomeId + "\" in dimension \"" + dimension.toString() + "\".");
                    }
                } else {
                    Apoli.LOGGER.warn("Biome with ID \"" + biomeId + "\" was not registered.");
                }
            }

            if(structure == null) {
                tpPos = getValidSpawn(spawnToDimPos, range, world);
            } else {
                Pair<BlockPos, ConfiguredStructureFeature<?, ?>> locateStructure = getStructureLocation(world, structure, null, dimension);
                BlockPos structurePos = locateStructure.getLeft();
                ChunkPos structureChunkPos;

                if(structurePos == null) {
                    return null;
                }
                structureChunkPos = new ChunkPos(structurePos.getX() >> 4, structurePos.getZ() >> 4);
                StructureStart structureStart = world.getStructureAccessor().getStructureStart(ChunkSectionPos.from(structureChunkPos, 0), locateStructure.getRight(), world.getChunk(structurePos));
                BlockPos structureCenter = new BlockPos(structureStart.getBoundingBox().getCenter());
                tpPos = getValidSpawn(structureCenter, range, world);
            }

            if(tpPos != null) {
                mutable = new BlockPos(tpPos.x, tpPos.y, tpPos.z).mutableCopy();
                BlockPos spawnLocation = mutable;
                world.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(spawnLocation), 11, Unit.INSTANCE);
                return new Pair(world, spawnLocation);
            }
            return null;
        }
        return null;
    }

    private Pair<BlockPos, ConfiguredStructureFeature<?, ?>> getStructureLocation(World world, RegistryKey<ConfiguredStructureFeature<?, ?>> structure, TagKey<ConfiguredStructureFeature<?, ?>> structureTag, RegistryKey<World> dimension) {
        Registry<ConfiguredStructureFeature<?, ?>> registry = world.getRegistryManager().get(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY);
        RegistryEntryList<ConfiguredStructureFeature<?, ?>> entryList = null;
        String structureOrTagName = "";
        if(structure != null) {
            var entry = registry.getEntry(structure);
            if(entry.isPresent()) {
                entryList = RegistryEntryList.of(entry.get());
            }
            structureOrTagName = structure.getValue().toString();
        }
        if(entryList == null) {
            var optionalList = registry.getEntryList(structureTag);
            if(optionalList.isPresent()) {
                entryList = optionalList.get();
            }
            structureOrTagName = "#" + structureTag.id().toString();
        }
        BlockPos blockPos = new BlockPos(0, 70, 0);
        ServerWorld serverWorld = entity.getServer().getWorld(dimension);
        com.mojang.datafixers.util.Pair<BlockPos, RegistryEntry<ConfiguredStructureFeature<?, ?>>> result = serverWorld.getChunkManager().getChunkGenerator().locateStructure(serverWorld, entryList, blockPos, 100, false);
        if (result == null) {
            Apoli.LOGGER.warn("Could not find structure \"" + structureOrTagName + "\" in dimension: " + dimension.getValue());
            return null;
        } else {
            return new Pair<>(result.getFirst(), result.getSecond().value());
        }
    }

    private Vec3d getValidSpawn(BlockPos startPos, int range, ServerWorld world) {
        // (di, dj) is a vector - direction in which we move right now
        int dx = 1;
        int dz = 0;
        // length of current segment
        int segmentLength = 1;
        BlockPos.Mutable mutable = startPos.mutableCopy();
        // center of our starting structure, or dimension
        int center = startPos.getY();
        // Our valid spawn location
        Vec3d tpPos;

        // current position (x, z) and how much of current segment we passed
        int x = startPos.getX();
        int z = startPos.getZ();
        //position to check up, or down
        int segmentPassed = 0;
        // increase y check
        int i = 0;
        // Decrease y check
        int d = 0;
        while(i < world.getLogicalHeight() || d > 0) {
            for (int coordinateCount = 0; coordinateCount < range; ++coordinateCount) {
                // make a step, add 'direction' vector (di, dj) to current position (i, j)
                x += dx;
                z += dz;
                ++segmentPassed;mutable.setX(x);
                mutable.setZ(z);
                mutable.setY(center + i);
                tpPos = Dismounting.findRespawnPos(EntityType.PLAYER, world, mutable, true);
                if (tpPos != null) {
                    return(tpPos);
                } else {
                    mutable.setY(center + d);
                    tpPos = Dismounting.findRespawnPos(EntityType.PLAYER, world, mutable, true);
                    if (tpPos != null) {
                        return(tpPos);
                    }
                }

                if (segmentPassed == segmentLength) {
                    // done with current segment
                    segmentPassed = 0;

                    // 'rotate' directions
                    int buffer = dx;
                    dx = -dz;
                    dz = buffer;

                    // increase segment length if necessary
                    if (dz == 0) {
                        ++segmentLength;
                    }
                }
            }
            i++;
            d--;
        }
        return(null);
    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(Apoli.identifier("modify_player_spawn"),
            new SerializableData()
                .add("dimension", SerializableDataTypes.DIMENSION)
                .add("dimension_distance_multiplier", SerializableDataTypes.FLOAT, 0F)
                .add("biome", SerializableDataTypes.IDENTIFIER, null)
                .add("spawn_strategy", SerializableDataTypes.STRING, "default")
                .add("structure", SerializableDataType.registryKey(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY), null)
                .add("respawn_sound", SerializableDataTypes.SOUND_EVENT, null),
            data ->
                (type, player) ->
                    new ModifyPlayerSpawnPower(type, player,
                        data.get("dimension"),
                        data.getFloat("dimension_distance_multiplier"),
                        data.getId("biome"),
                        data.getString("spawn_strategy"),
                        data.isPresent("structure") ? data.get("structure") : null,
                        data.get("respawn_sound")))
            .allowCondition();
    }
}

