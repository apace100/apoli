package io.github.apace100.apoli.networking.packet.s2c;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.util.KeyBindingData;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public record SyncKeyBindingRegistryS2CPacket(Map<Identifier, KeyBindingData> keyBindings) implements FabricPacket {

    public static final PacketType<SyncKeyBindingRegistryS2CPacket> TYPE = PacketType.create(
        Apoli.identifier("s2c/sync_keybinding_registry"), SyncKeyBindingRegistryS2CPacket::read
    );

    public static SyncKeyBindingRegistryS2CPacket read(PacketByteBuf buffer) {
        int keybindingSize = buffer.readVarInt();
        Map<Identifier, KeyBindingData> keyBindings = new HashMap<>(keybindingSize);

        for (int i = 0; i < keybindingSize; i++) {
            Identifier keyBindingId = buffer.readIdentifier();
            try {
                KeyBindingData data = KeyBindingData.fromBuffer(buffer, keyBindingId);

                keyBindings.put(keyBindingId, data);
            } catch (Exception e) {
                Apoli.LOGGER.error("Error while receiving keybinding \"{}\": {}", keyBindingId, e.getMessage());
            }

        }

        return new SyncKeyBindingRegistryS2CPacket(keyBindings);

    }

    @Override
    public void write(PacketByteBuf buffer) {
        Map<Identifier, KeyBindingData> filteredKeyBindings = keyBindings.entrySet()
            .stream()
            .filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, o2) -> o2, LinkedHashMap::new));

        buffer.writeVarInt(filteredKeyBindings.size());

        filteredKeyBindings.forEach((id, keyBindingData) -> {
            buffer.writeIdentifier(id);
            keyBindingData.toBuffer(buffer);
        });

    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}