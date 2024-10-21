package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.Optional;

public class PlaySoundEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<PlaySoundEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("sound", SerializableDataTypes.SOUND_EVENT)
            .add("category", SerializableDataType.enumValue(SoundCategory.class).optional(), Optional.empty())
            .add("volume", SerializableDataTypes.FLOAT, 1.0F)
            .add("pitch", SerializableDataTypes.FLOAT, 1.0F),
        data -> new PlaySoundEntityActionType(
            data.get("sound"),
            data.get("category"),
            data.get("volume"),
            data.get("pitch")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("sound", actionType.sound)
            .set("category", actionType.category)
            .set("volume", actionType.volume)
            .set("pitch", actionType.pitch)
    );

    private final SoundEvent sound;
    private final Optional<SoundCategory> category;

    private final float volume;
    private final float pitch;

    public PlaySoundEntityActionType(SoundEvent sound, Optional<SoundCategory> category, float volume, float pitch) {
        this.sound = sound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    protected void execute(Entity entity) {
        entity.getWorld().playSound(null, entity.getBlockPos(), sound, category.orElseGet(entity::getSoundCategory), volume, pitch);
    }

    @Override
    public ActionConfiguration<?> configuration() {
        return EntityActionTypes.PLAY_SOUND;
    }

}
