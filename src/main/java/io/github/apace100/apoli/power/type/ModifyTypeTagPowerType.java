package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.networking.packet.s2c.SyncEntityTypeTagCacheS2CPacket;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.mixin.TagEntryAccessor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.DependencyTracker;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ModifyTypeTagPowerType extends PowerType {

    private static final Map<Identifier, Collection<Identifier>> ENTITY_TYPE_SUB_TAGS = new ConcurrentHashMap<>();
    private static final String ENTITY_TYPE_TAG_PATH = RegistryKeys.getTagPath(RegistryKeys.ENTITY_TYPE);

    private final TagKey<EntityType<?>> tag;

    public ModifyTypeTagPowerType(Power power, LivingEntity entity, TagKey<EntityType<?>> tag) {
        super(power, entity);
        this.tag = tag;
    }

    public boolean doesApply(TagKey<EntityType<?>> typeTag) {
        return Objects.equals(typeTag, tag) || ENTITY_TYPE_SUB_TAGS.getOrDefault(typeTag.id(), new ObjectArrayList<>())
            .stream()
            .map(id -> TagKey.of(RegistryKeys.ENTITY_TYPE, id))
            .anyMatch(this::doesApply);
    }

    public static boolean doesApply(Entity entity, TagKey<EntityType<?>> typeTag) {
        return PowerHolderComponent.hasPowerType(entity, ModifyTypeTagPowerType.class, type -> type.doesApply(typeTag));
    }

    public static boolean doesApply(Entity entity, RegistryEntryList<EntityType<?>> entryList) {
        return entryList.getTagKey()
            .map(tagKey -> doesApply(entity, tagKey))
            .orElse(false);
    }

    @ApiStatus.Internal
    public static <T> void setTagCache(String directory, TagEntry.ValueGetter<T> valueGetter, DependencyTracker<Identifier, TagGroupLoader.TagDependencies> dependencyTracker) {

        if (ENTITY_TYPE_TAG_PATH.equals(directory)) {
            dependencyTracker.traverse((id, dependencies) -> dependencies.entries()
                .stream()
                .map(TagGroupLoader.TrackedEntry::entry)
                .filter(entry -> entry.resolve(valueGetter, value -> {}))
                .map(TagEntryAccessor.class::cast)
                .filter(TagEntryAccessor::isTag)
                .forEach(entry -> ENTITY_TYPE_SUB_TAGS
                    .computeIfAbsent(id, k -> new ObjectArraySet<>())
                    .add(entry.getId())));
        }

    }

    @ApiStatus.Internal
    public static void resetTagCache(MinecraftServer server, LifecycledResourceManager resourceManager) {
        ENTITY_TYPE_SUB_TAGS.clear();
    }

    @Environment(EnvType.CLIENT)
    @ApiStatus.Internal
    public static void receiveTagCache(SyncEntityTypeTagCacheS2CPacket payload, ClientPlayNetworking.Context context) {
        ENTITY_TYPE_SUB_TAGS.clear();
        ENTITY_TYPE_SUB_TAGS.putAll(payload.subTags());
    }

    @ApiStatus.Internal
    public static void sendTagCache(ServerPlayerEntity player, boolean joined) {
        ServerPlayNetworking.send(player, new SyncEntityTypeTagCacheS2CPacket(ENTITY_TYPE_SUB_TAGS));
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_type_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.ENTITY_TAG),
            data -> (power, entity) -> new ModifyTypeTagPowerType(power, entity,
                data.get("tag")
            )
        ).allowCondition();
    }

}
