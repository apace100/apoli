package io.github.apace100.apoli.util;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class KeyBindingData {

    private Identifier identifier;
    private final Text name;
    private final String keyKey;
    private final String category;

    public KeyBindingData(Identifier identifier, Text name, String keyKey, String category) {
        this.identifier = identifier;
        this.name = name;
        this.keyKey = keyKey;
        this.category = category;
    }

    public KeyBindingData(Text name, String keyKey, String category) {
        this.name = name;
        this.keyKey = keyKey;
        this.category = category;
    }

    public void toBuffer(PacketByteBuf buf) {
        buf.writeString(this.keyKey);
        buf.writeString(this.category);
        buf.writeBoolean(name != null);
        if(name != null) {
            buf.writeText(this.name);
        }
    }

    public String getCategory() {
        return category;
    }

    public Text getName() {
        if(name == null) return Text.translatable(getTranslationKey());
        return name;
    }

    public String getKey() {
        return keyKey;
    }

    public String getTranslationKey() {
        if(identifier == null) return null;
        return "key." + identifier.getNamespace() + "." + identifier.getPath();
    }

    public static KeyBindingData fromBuffer(PacketByteBuf buf, Identifier identifier) {
        String key = buf.readString();
        String category = buf.readString();
        boolean hasName = buf.readBoolean();

        Text name = hasName ? buf.readText() : Text.translatable("key." + identifier.getNamespace() + "." + identifier.getPath());

        return new KeyBindingData(identifier, name, key, category);
    }
}
