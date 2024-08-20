package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ModifyTypeTagPowerType extends PowerType {

    @ApiStatus.Internal
    public static final ConcurrentHashMap<Identifier, Collection<Identifier>> TAGS_IN_TAGS = new ConcurrentHashMap<>();
    private static final String TAG_PATH = RegistryKeys.getTagPath(RegistryKeys.ENTITY_TYPE) + "/";

    private final TagKey<EntityType<?>> tag;

    public ModifyTypeTagPowerType(Power power, LivingEntity entity, TagKey<EntityType<?>> tag) {
        super(power, entity);
        this.tag = tag;
    }

    public boolean doesApply(TagKey<EntityType<?>> typeTag) {

        if (Objects.equals(tag, typeTag)) {
            return true;
        }

        else {

            Identifier prefixedThisTagId = tag.id().withPrefixedPath(TAG_PATH);
            Identifier prefixedThatTagId = typeTag.id().withPrefixedPath(TAG_PATH);

            return TAGS_IN_TAGS.getOrDefault(prefixedThatTagId, new ObjectArrayList<>()).contains(prefixedThisTagId);

        }

    }

    public static boolean doesApply(Entity entity, TagKey<EntityType<?>> typeTag) {
        return PowerHolderComponent.hasPowerType(entity, ModifyTypeTagPowerType.class, type -> type.doesApply(typeTag));
    }

    public static boolean doesApply(Entity entity, RegistryEntryList<EntityType<?>> entryList) {
        return entryList.getTagKey()
            .map(tagKey -> doesApply(entity, tagKey))
            .orElse(false);
    }

    public static void registerStartServerReloadCallback(MinecraftServer server, LifecycledResourceManager resourceManager) {
        TAGS_IN_TAGS.clear();
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("modify_type_tag"),
            new SerializableData()
                .add("tag", SerializableDataTypes.ENTITY_TAG),
            data -> (powerType, livingEntity) -> new ModifyTypeTagPowerType(powerType, livingEntity,
                data.get("tag")
            )
        ).allowCondition();
    }

}
