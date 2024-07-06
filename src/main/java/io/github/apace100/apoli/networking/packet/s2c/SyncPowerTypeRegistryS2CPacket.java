package io.github.apace100.apoli.networking.packet.s2c;

import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public record SyncPowerTypeRegistryS2CPacket(Map<Identifier, PowerType<?>> powers) implements CustomPayload {

    public static final Id<SyncPowerTypeRegistryS2CPacket> PACKET_ID = new Id<>(Apoli.identifier("s2c/sync_power_type_registry"));
    public static final PacketCodec<RegistryByteBuf, SyncPowerTypeRegistryS2CPacket> PACKET_CODEC = PacketCodec.of(SyncPowerTypeRegistryS2CPacket::write, SyncPowerTypeRegistryS2CPacket::read);

    public static SyncPowerTypeRegistryS2CPacket read(RegistryByteBuf buf) {

        int powerSize = buf.readVarInt();
        Map<Identifier, PowerType<?>> powers = new HashMap<>(powerSize);

        for (int i = 0; i < powerSize; i++) {

            Identifier powerTypeId = buf.readIdentifier();
            Identifier powerFactoryId = buf.readIdentifier();

            try {

                PowerFactory<?>.Instance powerFactory = ApoliRegistries.POWER_FACTORY
                    .getOrEmpty(powerFactoryId)
                    .orElseThrow(() -> new JsonSyntaxException("Power type \"" + powerFactoryId + "\" was not registered."))
                    .read(buf);

                PowerType<?> powerType;
                if (!buf.readBoolean()) {
                    powerType = new PowerType<>(powerTypeId, powerFactory);
                } else {

                    powerType = new MultiplePowerType<>(powerTypeId, powerFactory);
                    List<Identifier> subPowers = new LinkedList<>();

                    int subPowersSize = buf.readVarInt();
                    for (int j = 0; j < subPowersSize; j++) {
                        subPowers.add(buf.readIdentifier());
                    }

                    ((MultiplePowerType<?>) powerType).setSubPowers(subPowers);

                }

                powerType
                    .setDisplayTexts(TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.decode(buf), TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.decode(buf))
                    .setSubPower(buf.readBoolean())
                    .setHidden(buf.readBoolean());

                powers.put(powerTypeId, powerType);

            } catch (Exception e) {
                Apoli.LOGGER.error("Error while receiving power \"{}\" (power type: \"{}\"): {}", powerTypeId, powerFactoryId, e.getMessage());
            }

        }

        return new SyncPowerTypeRegistryS2CPacket(powers);

    }

    public void write(RegistryByteBuf buf) {

        Map<Identifier, PowerType<?>> filteredPowers = powers.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getFactory() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (o, o2) -> o2, LinkedHashMap::new));

        buf.writeVarInt(filteredPowers.size());
        filteredPowers.forEach((id, powerType) -> {

            buf.writeIdentifier(id);
            powerType.getFactory().write(buf);

            if (!(powerType instanceof MultiplePowerType<?> multiplePowerType)) {
                buf.writeBoolean(false);
            } else {

                List<Identifier> subPowerIds = multiplePowerType.getSubPowers();

                buf.writeBoolean(true);
                buf.writeVarInt(subPowerIds.size());

                subPowerIds.forEach(buf::writeIdentifier);

            }

            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, powerType.getName());
            TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, powerType.getDescription());

            buf.writeBoolean(powerType.isSubPower());
            buf.writeBoolean(powerType.isHidden());

        });

    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
