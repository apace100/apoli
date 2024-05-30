package io.github.apace100.apoli.power;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.util.ApoliConfig;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ModifyPlayerSpawnPower extends Power {

    public final RegistryKey<World> dimension;
    public final float dimensionDistanceMultiplier;
    public final Identifier biomeId;
    public final SpawnStrategy spawnStrategy;
    public final RegistryKey<Structure> structure;
    public final SoundEvent spawnSound;

    private enum SpawnStrategy {

        CENTER((blockPos, center, multiplier) -> new BlockPos(0, center, 0)),
        DEFAULT(
                (blockPos, center, multiplier) -> {

                    BlockPos.Mutable mut = new BlockPos.Mutable();

                    if (multiplier != 0) mut.set(blockPos.getX() * multiplier, blockPos.getY(), blockPos.getZ() * multiplier);
                    else mut.set(blockPos);

                    return mut;

                }
        );

        final TriFunction<BlockPos, Integer, Float, BlockPos> strategyApplier;
        SpawnStrategy(TriFunction<BlockPos, Integer, Float, BlockPos> strategyApplier) {
            this.strategyApplier = strategyApplier;
        }

        public BlockPos apply(BlockPos blockPos, int center, float multiplier) {
            return strategyApplier.apply(blockPos, center, multiplier);
        }

    }

    public ModifyPlayerSpawnPower(PowerType<?> type, LivingEntity entity, RegistryKey<World> dimension, float dimensionDistanceMultiplier, Identifier biomeId, SpawnStrategy spawnStrategy, RegistryKey<Structure> structure, SoundEvent spawnSound) {
        super(type, entity);
        this.dimension = dimension;
        this.dimensionDistanceMultiplier = dimensionDistanceMultiplier;
        this.biomeId = biomeId;
        this.spawnStrategy = spawnStrategy;
        this.structure = structure;
        this.spawnSound = spawnSound;
    }

    @Override
    public void onRemoved() {

        if (entity.getWorld().isClient || !(entity instanceof PlayerEntity playerEntity)) return;

        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerEntity;
        if (serverPlayerEntity.isDisconnected() || serverPlayerEntity.getSpawnPointPosition() == null || !serverPlayerEntity.isSpawnForced()) return;

        serverPlayerEntity.setSpawnPoint(World.OVERWORLD, null, 0F, false, false);

    }

    public void teleportToModifiedSpawn() {

        if (entity.getWorld().isClient || !(entity instanceof PlayerEntity playerEntity)) return;

        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerEntity;
        Pair<ServerWorld, BlockPos> newSpawn = getSpawn(false);

        if (newSpawn == null) return;
        ServerWorld newSpawnDimension = newSpawn.getLeft();
        BlockPos newSpawnPos = newSpawn.getRight();

        Vec3d tpPos = Dismounting.findRespawnPos(playerEntity.getType(), newSpawn.getLeft(), newSpawn.getRight(), true);
        if (tpPos == null) {
            serverPlayerEntity.teleport(newSpawnDimension, newSpawnPos.getX(), newSpawnPos.getY(), newSpawnPos.getZ(), entity.getPitch(), entity.getYaw());
            Apoli.LOGGER.warn("Power {} could not find a suitable spawnpoint for {}! Teleporting to the desired location directly...", this.getType().getIdentifier(), entity.getNameForScoreboard());
        }

        else serverPlayerEntity.teleport(newSpawnDimension, tpPos.x, tpPos.y, tpPos.z, entity.getPitch(), entity.getYaw());

    }

    public Pair<ServerWorld, BlockPos> getSpawn(boolean isSpawnObstructed) {
        if (entity.getWorld().isClient || !(entity instanceof PlayerEntity playerEntity)) return null;

        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerEntity;
        MinecraftServer server = serverPlayerEntity.getServer();
        if (server == null) return null;

        ServerWorld overworldDimension = server.getWorld(World.OVERWORLD);
        if (overworldDimension == null) return null;

        ServerWorld targetDimension = server.getWorld(dimension);
        if (targetDimension == null) {
            Apoli.LOGGER.warn("Power {} could not set {}'s spawnpoint at dimension \"{}\" as it's not registered! Falling back to default spawnpoint...", this.getType().getIdentifier(), entity.getNameForScoreboard(), dimension.getValue());
            return null;
        }

        int center = targetDimension.getLogicalHeight() / 2;
        int range = 64;

        AtomicReference<Vec3d> modifiedSpawnPos = new AtomicReference<>();

        BlockPos regularSpawnBlockPos = overworldDimension.getSpawnPos();
        BlockPos.Mutable modifiedSpawnBlockPos = new BlockPos.Mutable();
        BlockPos.Mutable dimensionSpawnPos = spawnStrategy.apply(regularSpawnBlockPos, center, dimensionDistanceMultiplier).mutableCopy();

        getBiomePos(targetDimension, dimensionSpawnPos).ifPresent(dimensionSpawnPos::set);
        getSpawnPos(targetDimension, dimensionSpawnPos, range).ifPresent(modifiedSpawnPos::set);

        if (modifiedSpawnPos.get() == null) return null;

        Vec3d msp = modifiedSpawnPos.get();
        modifiedSpawnBlockPos.set(msp.x, msp.y, msp.z);
        targetDimension.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(modifiedSpawnBlockPos), 11, Unit.INSTANCE);

        return new Pair<>(targetDimension, modifiedSpawnBlockPos);

    }

    private Optional<BlockPos> getBiomePos(ServerWorld targetDimension, BlockPos originPos) {

        if (biomeId == null) return Optional.empty();

        Optional<Biome> targetBiome = targetDimension.getRegistryManager().get(RegistryKeys.BIOME).getOrEmpty(biomeId);
        if (targetBiome.isEmpty()) {
            Apoli.LOGGER.warn("Power {} could not set {}'s spawnpoint at biome \"{}\" as it's not registered in dimension \"{}\".", this.getType().getIdentifier(), entity.getNameForScoreboard(), biomeId, dimension.getValue());
            return Optional.empty();
        }
        int radius = ((ApoliConfig)Apoli.config).modifyPlayerSpawnPower.radius;
        int horizontalBlockCheckInterval = ((ApoliConfig)Apoli.config).modifyPlayerSpawnPower.horizontalBlockCheckInterval;
        int verticalBlockCheckInterval = ((ApoliConfig)Apoli.config).modifyPlayerSpawnPower.verticalBlockCheckInterval;
        if (radius < 0 ) radius = 6400;
        if (horizontalBlockCheckInterval <= 0) horizontalBlockCheckInterval = 64;
        if (verticalBlockCheckInterval <= 0) verticalBlockCheckInterval = 64;
        com.mojang.datafixers.util.Pair<BlockPos, RegistryEntry<Biome>> targetBiomePos = targetDimension.locateBiome(
                biome -> biome.value() == targetBiome.get(),
                originPos,
                radius,
                horizontalBlockCheckInterval,
                verticalBlockCheckInterval
        );
        if (targetBiomePos != null) return Optional.of(targetBiomePos.getFirst());
        else {
            Apoli.LOGGER.warn("Power {} could not set {}'s spawnpoint at biome \"{}\" as it couldn't be found in dimension \"{}\".", this.getType().getIdentifier(), entity.getNameForScoreboard(), biomeId, dimension.getValue());
            return Optional.empty();
        }
    }

    private Optional<Pair<BlockPos, Structure>> getStructurePos(World world, RegistryKey<Structure> structure, TagKey<Structure> structureTag, RegistryKey<World> dimension) {

        Registry<Structure> structureRegistry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        RegistryEntryList<Structure> structureRegistryEntryList = null;
        String structureTagOrName = "";

        if (structure != null) {

            var entry = structureRegistry.getEntry(structure);
            if (entry.isPresent()) structureRegistryEntryList = RegistryEntryList.of(entry.get());

            structureTagOrName = structure.getValue().toString();

        }

        if (structureRegistryEntryList == null) {

            var entryList = structureRegistry.getEntryList(structureTag);
            if (entryList.isPresent()) structureRegistryEntryList = entryList.get();

            structureTagOrName = "#" + structureTag.id().toString();

        }

        MinecraftServer server = entity.getServer();
        if (server == null) return Optional.empty();

        ServerWorld serverWorld = server.getWorld(dimension);
        if (serverWorld == null) return Optional.empty();

        BlockPos center = new BlockPos(0, 70, 0);
        com.mojang.datafixers.util.Pair<BlockPos, RegistryEntry<Structure>> structurePos = serverWorld
                .getChunkManager()
                .getChunkGenerator()
                .locateStructure(
                        serverWorld,
                        structureRegistryEntryList,
                        center,
                        100,
                        false
                );

        if (structurePos == null) {
            Apoli.LOGGER.warn("Power {} could not set {}'s spawnpoint at structure \"{}\" as it couldn't be found in dimension \"{}\".", this.getType().getIdentifier(), entity.getNameForScoreboard(), structureTagOrName, dimension.getValue());
            return Optional.empty();
        }

        else return Optional.of(new Pair<>(structurePos.getFirst(), structurePos.getSecond().value()));

    }

    private Optional<Vec3d> getSpawnPos(ServerWorld targetDimension, BlockPos originPos, int range) {

        if (structure == null) return getValidSpawn(targetDimension, originPos, range);

        Optional<Pair<BlockPos, Structure>> targetStructure = getStructurePos(targetDimension, structure, null, dimension);
        if (targetStructure.isEmpty()) return Optional.empty();

        BlockPos targetStructurePos = targetStructure.get().getLeft();
        ChunkPos targetStructureChunkPos = new ChunkPos(targetStructurePos.getX() >> 4, targetStructurePos.getZ() >> 4);

        StructureStart targetStructureStart = targetDimension.getStructureAccessor().getStructureStart(ChunkSectionPos.from(targetStructureChunkPos, 0), targetStructure.get().getRight(), targetDimension.getChunk(targetStructurePos));
        if (targetStructureStart == null) return Optional.empty();

        BlockPos targetStructureCenter = new BlockPos(targetStructureStart.getBoundingBox().getCenter());
        return getValidSpawn(targetDimension, targetStructureCenter, range);

    }

    private Optional<Vec3d> getValidSpawn(ServerWorld targetDimension, BlockPos startPos, int range) {

        //  The 'direction' vector that determines the direction of the iteration
        int dx = 1;
        int dz = 0;

        //  The length of the current segment
        int segmentLength = 1;

        //  The center of the structure/dimension
        int center = startPos.getY();

        //  The valid spawn position and (mutable) starting position
        Vec3d spawnPos;
        BlockPos.Mutable mutableStartPos = startPos.mutableCopy();

        //  The current position
        int x = startPos.getX();
        int z = startPos.getZ();

        //  Determines how much of the current segment has been passed
        int segmentPassed = 0;

        //  Vertical offsets
        int upOffset = 0;
        int downOffset = 0;

        //  The min and max Y values of the target dimension
        int maxY = targetDimension.getLogicalHeight();
        int minY = targetDimension.getDimensionEntry().value().minY();

        while (upOffset < maxY || downOffset > minY) {

            for (int steps = 0; steps < range; ++steps) {

                //  Make a step by adding the 'direction' vector to the current position
                x += dx;
                z += dz;
                mutableStartPos.setX(x);
                mutableStartPos.setZ(z);

                //  Increment how much of the current segment has been passed
                ++segmentPassed;

                //  Offset the Y axis (up and down) of the current position to check for valid spawn positions
                mutableStartPos.setY(center + upOffset);
                spawnPos = Dismounting.findRespawnPos(entity.getType(), targetDimension, mutableStartPos, true);
                if (spawnPos != null) return Optional.of(spawnPos);

                mutableStartPos.setY(center + downOffset);
                spawnPos = Dismounting.findRespawnPos(entity.getType(), targetDimension, mutableStartPos, true);
                if (spawnPos != null) return Optional.of(spawnPos);

                //  If the current segment has not been passed, continue the loop
                if (segmentPassed != segmentLength) continue;

                //  Otherwise, reset the value of how much of the current segment has been passed
                segmentPassed = 0;

                //  'Rotate' the 'direction' vector
                int bdx = dx;
                dx = -dz;
                dz = bdx;

                //  Increment the length of the current segment if necessary
                if (dz == 0) ++segmentLength;

            }

            //  Increment/decrement the up/down offsets until it's no longer less/greater than the max/min Y
            if (upOffset < maxY) upOffset++;
            if (downOffset > minY) downOffset--;

        }

        return Optional.empty();

    }

    public static PowerFactory createFactory() {
        return new PowerFactory<>(
                Apoli.identifier("modify_player_spawn"),
                new SerializableData()
                        .add("dimension", SerializableDataTypes.DIMENSION)
                        .add("dimension_distance_multiplier", SerializableDataTypes.FLOAT, 0F)
                        .add("biome", SerializableDataTypes.IDENTIFIER, null)
                        .add("spawn_strategy", SerializableDataType.enumValue(SpawnStrategy.class), SpawnStrategy.DEFAULT)
                        .add("structure", SerializableDataType.registryKey(RegistryKeys.STRUCTURE), null)
                        .add("respawn_sound", SerializableDataTypes.SOUND_EVENT, null),
                data -> (powerType, livingEntity) -> new ModifyPlayerSpawnPower(
                        powerType,
                        livingEntity,
                        data.get("dimension"),
                        data.get("dimension_distance_multiplier"),
                        data.get("biome"),
                        data.get("spawn_strategy"),
                        data.get("structure"),
                        data.get("respawn_sound")
                )
        ).allowCondition();
    }

}

