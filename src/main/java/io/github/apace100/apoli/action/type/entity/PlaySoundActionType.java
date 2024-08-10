package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.factory.ActionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

public class PlaySoundActionType {

    public static void action(Entity entity, SoundEvent sound, @Nullable SoundCategory category, float volume, float pitch) {
        entity.getWorld().playSound(null, entity.getBlockPos(), sound, category != null ? category : entity.getSoundCategory(), volume, pitch);
    }

    public static ActionTypeFactory<Entity> getFactory() {
        return new ActionTypeFactory<>(
            Apoli.identifier("play_sound"),
            new SerializableData()
                .add("sound", SerializableDataTypes.SOUND_EVENT)
                .add("category", SerializableDataType.enumValue(SoundCategory.class), null)
                .add("volume", SerializableDataTypes.FLOAT, 1.0f)
                .add("pitch", SerializableDataTypes.FLOAT, 1.0f),
            (data, entity) -> action(entity,
                data.get("sound"),
                data.get("category"),
                data.get("volume"),
                data.get("pitch")
            )
        );
    }

}
