package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ModifyPlayerSpawnPowerType extends PowerType implements Prioritized<ModifyPlayerSpawnPowerType> {

    private final RegistryKey<World> dimensionKey;

    private final RegistryKey<Structure> structureKey;
    private final TagKey<Structure> structureTag;

    private final RegistryKey<Biome> biomeKey;
    private final TagKey<Biome> biomeTag;

    private final SpawnStrategy spawnStrategy;
    private final SoundEvent respawnSound;

    private final float dimensionDistanceMultiplier;
    private final int priority;

    public ModifyPlayerSpawnPowerType(Power power, LivingEntity entity, RegistryKey<World> dimensionKey, RegistryKey<Structure> structureKey, TagKey<Structure> structureTag, RegistryKey<Biome> biomeKey, TagKey<Biome> biomeTag, SpawnStrategy spawnStrategy, SoundEvent respawnSound, float dimensionDistanceMultiplier, int priority) {
        super(power, entity);
        this.dimensionKey = dimensionKey;
        this.structureKey = structureKey;
        this.structureTag = structureTag;
        this.biomeKey = biomeKey;
        this.biomeTag = biomeTag;
        this.spawnStrategy = spawnStrategy;
        this.respawnSound = respawnSound;
        this.dimensionDistanceMultiplier = dimensionDistanceMultiplier;
        this.priority = priority;
    }

    @Override
    public void onRespawn() {

        if (respawnSound != null) {
            entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getX(), respawnSound, entity.getSoundCategory(), 1.0F, 1.0F);
        }

    }

    @Override
    public void onLost() {

        if (!(entity instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        if (!serverPlayer.isDisconnected() && serverPlayer.getSpawnPointPosition() != null && !serverPlayer.isSpawnForced()) {
            serverPlayer.setSpawnPoint(World.OVERWORLD, null, 0F, false, false);
        }

    }

    @Override
    public int getPriority() {
        return priority;
    }

    public RegistryKey<World> getDimensionKey() {
        return dimensionKey;
    }

    public void teleportToModifiedSpawn() {

        if (!(entity instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        Pair<ServerWorld, BlockPos> spawnPoint = this.getSpawn().orElse(null);
        if (spawnPoint == null) {
            return;
        }

        ServerWorld spawnPointDimension = spawnPoint.getLeft();
        BlockPos spawnPointPosition = spawnPoint.getRight();

        float pitch = serverPlayer.getPitch();
        float yaw = serverPlayer.getYaw();

        Vec3d placement = Dismounting.findRespawnPos(serverPlayer.getType(), spawnPointDimension, spawnPointPosition, true);
        if (placement == null) {
            Apoli.LOGGER.warn("Power \"{}\" could not find a suitable spawn point for player {}! Teleporting to the found location directly...", this.getPowerId(), entity.getName().getString());
            serverPlayer.teleport(spawnPointDimension, spawnPointPosition.getX(), spawnPointPosition.getY(), spawnPointPosition.getZ(), pitch, yaw);
        }

        else {
            serverPlayer.teleport(spawnPointDimension, placement.getX(), placement.getY(), placement.getZ(), pitch, yaw);
        }

    }

    public Optional<Pair<ServerWorld, BlockPos>> getSpawn() {

        if (!(entity instanceof ServerPlayerEntity serverPlayer)) {
            return Optional.empty();
        }

        MinecraftServer server = serverPlayer.server;
        ServerWorld targetDimension = server.getWorld(dimensionKey);

        if (targetDimension == null) {
            return Optional.empty();
        }

        int center = targetDimension.getLogicalHeight() / 2;
        int range = 64;

        AtomicReference<Vec3d> newSpawnPointVec = new AtomicReference<>();
        BlockPos dimensionSpawnPos = serverPlayer.getServerWorld().getSpawnPos();

        BlockPos.Mutable newSpawnPointPos = new BlockPos.Mutable();
        BlockPos.Mutable mutableDimensionSpawnPos = spawnStrategy.apply(dimensionSpawnPos, center, dimensionDistanceMultiplier).mutableCopy();

        this.getBiomePos(targetDimension, mutableDimensionSpawnPos).ifPresent(mutableDimensionSpawnPos::set);
        this.getSpawnPos(targetDimension, mutableDimensionSpawnPos, range).ifPresent(newSpawnPointVec::set);

        if (newSpawnPointVec.get() == null) {
            return Optional.empty();
        }

        Vec3d msp = newSpawnPointVec.get();
        newSpawnPointPos.set(msp.x, msp.y, msp.z);

        targetDimension.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(newSpawnPointPos), 11, Unit.INSTANCE);
        return Optional.of(new Pair<>(targetDimension, newSpawnPointPos));

    }

    private Optional<BlockPos> getBiomePos(ServerWorld targetDimension, BlockPos originPos) {

        if (biomeKey == null && biomeTag == null) {
            return Optional.empty();
        }

        int radius = Apoli.config.modifyPlayerSpawnPower.radius;
        int horizontalBlockCheckInterval = Apoli.config.modifyPlayerSpawnPower.horizontalBlockCheckInterval;
        int verticalBlockCheckInterval = Apoli.config.modifyPlayerSpawnPower.verticalBlockCheckInterval;

        if (radius < 0) {
            radius = 6400;
        }

        if (horizontalBlockCheckInterval <= 0) {
            horizontalBlockCheckInterval = 64;
        }

        if (verticalBlockCheckInterval <= 0) {
            verticalBlockCheckInterval = 64;
        }

        var targetBiomePos = targetDimension.locateBiome(
            biome -> (biomeKey == null || biome.matchesKey(this.biomeKey)) || (biomeTag == null || biome.isIn(biomeTag)),
            originPos,
            radius,
            horizontalBlockCheckInterval,
            verticalBlockCheckInterval
        );

        if (targetBiomePos != null) {
            return Optional.of(targetBiomePos.getFirst());
        }

        else {

            StringBuilder name = new StringBuilder();
            if (biomeKey != null) {
                name.append("biome \"").append(biomeKey.getValue()).append("\"");
            }

            if (biomeTag != null) {
                name.append(!name.isEmpty() ? " or " : "").append("any biomes from tag \"").append(biomeTag.id()).append("\"");
            }

            Apoli.LOGGER.warn("Power \"{}\" could not set player {}'s spawn point at {} as none can be found nearby in dimension \"{}\".", this.getPowerId(), entity.getName().getString(), name, dimensionKey.getValue());
            entity.sendMessage(Text.literal("Power \"%s\" couldn't set spawn point at %s as none can be found nearby in dimension \"%s\"!".formatted(this.getPowerId(), name, dimensionKey.getValue())).formatted(Formatting.ITALIC, Formatting.GRAY));

            return Optional.empty();

        }

    }

    private Optional<Pair<BlockPos, Structure>> getStructurePos(ServerWorld dimension) {

        if (structureKey == null && structureTag == null) {
            return Optional.empty();
        }

        Registry<Structure> structureRegistry = dimension.getRegistryManager().get(RegistryKeys.STRUCTURE);
        List<RegistryEntry<Structure>> structureEntries = new ArrayList<>();

        if (structureKey != null) {
            structureEntries.add(structureRegistry.entryOf(structureKey));
        }

        if (structureTag != null) {
            structureRegistry.getEntryList(structureTag)
                .map(el -> (RegistryEntryList.ListBacked<Structure>) el)
                .map(RegistryEntryList.ListBacked::getEntries)
                .ifPresent(structureEntries::addAll);
        }

        BlockPos center = new BlockPos(0, 70, 0);
        int radius = Apoli.config.modifyPlayerSpawnPower.radius;

        if (radius < 0) {
            radius = 6400;
        }

        var result = Optional.ofNullable(dimension.getChunkManager().getChunkGenerator().locateStructure(dimension, RegistryEntryList.of(structureEntries), center, radius, false))
            .map(pair -> pair.mapSecond(RegistryEntry::value))
            .map(pair -> new Pair<>(pair.getFirst(), pair.getSecond()));

        if (result.isEmpty()) {

            StringBuilder name = new StringBuilder();
            if (structureKey != null) {
                name.append("structure \"").append(structureKey.getValue()).append("\"");
            }

            if (structureTag != null) {
                name.append(!name.isEmpty() ? " or " : "").append("any structures from tag \"").append(structureTag.id()).append("\"");
            }

            Apoli.LOGGER.warn("Power \"{}\" could not set player {}'s spawn point at {} as none can be found nearby in dimension \"{}\".", this.getPowerId(), entity.getName().getString(), name, dimensionKey.getValue());
            entity.sendMessage(Text.literal("Power \"%s\" couldn't set spawn point at %s as none can be found nearby in dimension \"%s\"!".formatted(this.getPowerId(), name, dimensionKey.getValue())).formatted(Formatting.ITALIC, Formatting.GRAY));

            return Optional.empty();

        }

        return result;

    }

    private Optional<Vec3d> getSpawnPos(ServerWorld targetDimension, BlockPos originPos, int range) {

        if (structureKey == null && structureTag == null) {
            return this.getValidSpawn(targetDimension, originPos, range);
        }

        Optional<Pair<BlockPos, Structure>> targetStructure = getStructurePos(targetDimension);
        if (targetStructure.isEmpty()) {
            return Optional.empty();
        }

        BlockPos targetStructurePos = targetStructure.get().getLeft();
        ChunkPos targetStructureChunkPos = new ChunkPos(targetStructurePos.getX() >> 4, targetStructurePos.getZ() >> 4);

        StructureStart targetStructureStart = targetDimension.getStructureAccessor().getStructureStart(ChunkSectionPos.from(targetStructureChunkPos, 0), targetStructure.get().getRight(), targetDimension.getChunk(targetStructurePos));
        if (targetStructureStart == null) {
            return Optional.empty();
        }

        BlockPos targetStructureCenter = new BlockPos(targetStructureStart.getBoundingBox().getCenter());
        return this.getValidSpawn(targetDimension, targetStructureCenter, range);

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

                if (spawnPos != null) {
                    return Optional.of(spawnPos);
                }

                mutableStartPos.setY(center + downOffset);
                spawnPos = Dismounting.findRespawnPos(entity.getType(), targetDimension, mutableStartPos, true);

                if (spawnPos != null) {
                    return Optional.of(spawnPos);
                }

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
            if (upOffset < maxY) {
                upOffset++;
            }

            if (downOffset > minY) {
                downOffset--;
            }

        }

        return Optional.empty();

    }

    public enum SpawnStrategy {

        CENTER((blockPos, center, multiplier) -> new BlockPos(0, center, 0)),
        DEFAULT((blockPos, center, multiplier) -> {

            BlockPos.Mutable mut = new BlockPos.Mutable();

            if (multiplier != 0) {
                mut.set(blockPos.getX() * multiplier, blockPos.getY(), blockPos.getZ() * multiplier);
            }

            else {
                mut.set(blockPos);
            }

            return mut;

        });

        final TriFunction<BlockPos, Integer, Float, BlockPos> strategyApplier;
        SpawnStrategy(TriFunction<BlockPos, Integer, Float, BlockPos> strategyApplier) {
            this.strategyApplier = strategyApplier;
        }

        public BlockPos apply(BlockPos blockPos, int center, float multiplier) {
            return strategyApplier.apply(blockPos, center, multiplier);
        }

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_player_spawn"),
            new SerializableData()
                .add("dimension", SerializableDataTypes.DIMENSION)
                .add("structure", SerializableDataType.registryKey(RegistryKeys.STRUCTURE), null)
                .add("structure_tag", SerializableDataType.tag(RegistryKeys.STRUCTURE), null)
                .add("biome", SerializableDataType.registryKey(RegistryKeys.BIOME), null)
                .add("biome_tag", SerializableDataTypes.BIOME_TAG, null)
                .add("spawn_strategy", SerializableDataType.enumValue(SpawnStrategy.class), SpawnStrategy.DEFAULT)
                .add("respawn_sound", SerializableDataTypes.SOUND_EVENT, null)
                .add("dimension_distance_multiplier", SerializableDataTypes.FLOAT, 0F)
                .add("priority", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new ModifyPlayerSpawnPowerType(power, entity,
                data.get("dimension"),
                data.get("structure"),
                data.get("structure_tag"),
                data.get("biome"),
                data.get("biome_tag"),
                data.get("spawn_strategy"),
                data.get("respawn_sound"),
                data.get("dimension_distance_multiplier"),
                data.get("priority")
            )
        ).allowCondition();
    }

}
