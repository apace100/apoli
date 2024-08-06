package io.github.apace100.apoli.data;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record CustomToastData(Text title, Text description, Identifier texture, ItemStack iconStack, int duration) {

    public static final Identifier DEFAULT_TEXTURE = Apoli.identifier("toast/custom");

    public static final SerializableData DATA = new SerializableData()
        .add("title", SerializableDataTypes.TEXT)
        .add("description", SerializableDataTypes.TEXT)
        .add("texture", SerializableDataTypes.IDENTIFIER, DEFAULT_TEXTURE)
        .add("icon", SerializableDataTypes.ITEM_STACK, ItemStack.EMPTY)
        .add("duration", SerializableDataTypes.POSITIVE_INT, 100);

    public static final SerializableDataType<CustomToastData> DATA_TYPE = SerializableDataType.compound(
        CustomToastData.class,
        DATA,
        CustomToastData::fromData,
        (serializableData, toastData) -> toastData.toData()
    );

    public static CustomToastData fromData(SerializableData.Instance data) {
        return new CustomToastData(
            data.get("title"),
            data.get("description"),
            data.get("texture"),
            data.get("icon"),
            data.get("duration")
        );
    }

    public SerializableData.Instance toData() {

        SerializableData.Instance data = DATA.new Instance();

        data.set("title", this.title());
        data.set("description", this.description());
        data.set("texture", this.texture());
        data.set("icon", this.iconStack());
        data.set("duration", this.duration());

        return data;

    }

}
