package io.github.apace100.apoli.global;

import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.data.SerializableDataType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TagLike<T> {

    private final Registry<T> registry;
    private final List<TagKey<T>> tags = new LinkedList<>();
    private final Set<T> items = new HashSet<>();

    public TagLike(Registry<T> registry) {
        this.registry = registry;
    }

    public void addTag(Identifier id) {
        addTag(TagKey.of(registry.getKey(), id));
    }

    public void add(Identifier id) {
        add(registry.get(id));
    }

    public void addTag(TagKey<T> tagKey) {
        tags.add(tagKey);
    }

    public void add(T t) {
        items.add(t);
    }

    public boolean contains(T t) {
        if(items.contains(t)) {
            return true;
        }
        RegistryEntry<T> entry = registry.getEntry(t);
        for(TagKey<T> tagKey : tags) {
            if(entry.isIn(tagKey)) {
                return true;
            }
        }
        return false;
    }

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(tags.size());
        for(TagKey<T> tagKey : tags) {
            buf.writeString(tagKey.id().toString());
        }
        buf.writeVarInt(items.size());
        for(T t : items) {
            buf.writeString(registry.getId(t).toString());
        }
    }

    public void read(PacketByteBuf buf) {
        tags.clear();
        int count = buf.readVarInt();
        for(int i = 0; i < count; i++) {
            tags.add(TagKey.of(registry.getKey(), new Identifier(buf.readString())));
        }
        items.clear();
        count = buf.readVarInt();
        for(int i = 0; i < count; i++) {
            T t = registry.get(new Identifier(buf.readString()));
            items.add(t);
        }
    }

    public static <T> SerializableDataType<TagLike<T>> dataType(Registry<T> registry) {
        return new SerializableDataType<TagLike<T>>(ClassUtil.castClass(TagLike.class),
                (packetByteBuf, tagLike) -> tagLike.write(packetByteBuf),
                packetByteBuf -> {
                    TagLike<T> tagLike = new TagLike<>(registry);
                    tagLike.read(packetByteBuf);
                    return tagLike;
                },
                jsonElement -> {
                    TagLike<T> tagLike = new TagLike<>(registry);
                    if(!jsonElement.isJsonArray()) {
                        throw new JsonSyntaxException("Expected a JSON array");
                    }
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    jsonArray.forEach(je -> {
                        String s = je.getAsString();
                        if(s.startsWith("#")) {
                            Identifier id = new Identifier(s.substring(1));
                            tagLike.addTag(id);
                        } else {
                            tagLike.add(new Identifier(s));
                        }
                    });
                    return tagLike;
                });
    }
}
