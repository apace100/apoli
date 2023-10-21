package io.github.apace100.apoli.networking.packet.s2c;

import com.google.gson.JsonSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.MultiplePowerType;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public record SyncPowerTypeRegistryS2CPacket(Map<Identifier, PowerType<?>> powers) implements FabricPacket {

    public static final PacketType<SyncPowerTypeRegistryS2CPacket> TYPE = PacketType.create(
        Apoli.identifier("s2c/sync_power_type_registry"), SyncPowerTypeRegistryS2CPacket::read
    );

    public static SyncPowerTypeRegistryS2CPacket read(PacketByteBuf buffer) {

        int powerSize = buffer.readVarInt();
        Map<Identifier, PowerType<?>> powers = new HashMap<>(powerSize);

        for (int i = 0; i < powerSize; i++) {

            Identifier powerTypeId = buffer.readIdentifier();
            Identifier powerFactoryId = buffer.readIdentifier();

            try {

                PowerFactory<?>.Instance powerFactory = ApoliRegistries.POWER_FACTORY
                    .getOrEmpty(powerFactoryId)
                    .orElseThrow(() -> new JsonSyntaxException("Power type \"" + powerFactoryId + "\" was not registered."))
                    .read(buffer);

                PowerType<?> powerType;
                if (!buffer.readBoolean()) {
                    powerType = new PowerType<>(powerTypeId, powerFactory);
                } else {

                    powerType = new MultiplePowerType<>(powerTypeId, powerFactory);
                    List<Identifier> subPowers = new LinkedList<>();

                    int subPowersSize = buffer.readVarInt();
                    for (int j = 0; j < subPowersSize; j++) {
                        subPowers.add(buffer.readIdentifier());
                    }

                    ((MultiplePowerType<?>) powerType).setSubPowers(subPowers);

                }

                powerType.setDisplayTexts(buffer.readText(), buffer.readText());
                if (buffer.readBoolean()) {
                    powerType.setHidden();
                }

                powers.put(powerTypeId, powerType);

            } catch (Exception e) {
                Apoli.LOGGER.error("Error while receiving power \"{}\" (power type: \"{}\"): {}", powerTypeId, powerFactoryId, e.getMessage());
            }

        }

        return new SyncPowerTypeRegistryS2CPacket(powers);

    }

    @Override
    public void write(PacketByteBuf buffer) {

        buffer.writeVarInt(powers.size());
        powers.forEach((id, powerType) -> {

            PowerFactory<?>.Instance powerFactory = powerType.getFactory();
            if (powerFactory == null) {
                return;
            }

            buffer.writeIdentifier(id);
            powerFactory.write(buffer);

            if (!(powerType instanceof MultiplePowerType<?> multiplePowerType)) {
                buffer.writeBoolean(false);
            } else {

                List<Identifier> subPowerIds = multiplePowerType.getSubPowers();

                buffer.writeBoolean(true);
                buffer.writeVarInt(subPowerIds.size());

                subPowerIds.forEach(buffer::writeIdentifier);

            }

            buffer.writeText(powerType.getName());
            buffer.writeText(powerType.getDescription());

            buffer.writeBoolean(powerType.isHidden());

        });

    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}