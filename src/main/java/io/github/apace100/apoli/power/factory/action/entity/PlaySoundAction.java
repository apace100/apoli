package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;

public class PlaySoundAction {

    public static void action(SerializableData.Instance data, Entity entity) {
        SoundCategory category = data.isPresent("category") ? data.get("category") : entity.getSoundCategory();
        entity.getWorld().playSound(null, entity.getBlockPos(), data.get("sound"), category, data.getFloat("volume"), data.getFloat("pitch"));
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(
            Apoli.identifier("play_sound"),
            new SerializableData()
                .add("sound", SerializableDataTypes.SOUND_EVENT)
                .add("category", SerializableDataType.enumValue(SoundCategory.class), null)
                .add("volume", SerializableDataTypes.FLOAT, 1.0f)
                .add("pitch", SerializableDataTypes.FLOAT, 1.0f),
            PlaySoundAction::action
        );
    }

}
