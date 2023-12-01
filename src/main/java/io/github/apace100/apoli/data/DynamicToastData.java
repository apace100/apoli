package io.github.apace100.apoli.data;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public record DynamicToastData(Text title, Text description, ItemStack iconStack, @Nullable SoundEvent soundEvent, int duration) {

    public static final SerializableDataType<DynamicToastData> DATA_TYPE = SerializableDataType.compound(
        DynamicToastData.class,
        new SerializableData()
            .add("title", SerializableDataTypes.TEXT)
            .add("description", SerializableDataTypes.TEXT)
            .add("icon", SerializableDataTypes.ITEM_STACK, ItemStack.EMPTY)
            .add("sound_event", SerializableDataTypes.SOUND_EVENT, null)
            .add("duration", SerializableDataTypes.POSITIVE_INT, 5000),
        data -> new DynamicToastData(
            data.get("title"),
            data.get("description"),
            data.get("icon"),
            data.get("sound_event"),
            data.get("duration")
        ),
        (serializableData, toastData) -> {

            SerializableData.Instance data = serializableData.new Instance();

            data.set("title", toastData.title);
            data.set("description", toastData.description);
            data.set("icon", toastData.iconStack);
            data.set("sound_event", toastData.soundEvent);
            data.set("duration", toastData.duration);

            return data;

        }
    );

}
