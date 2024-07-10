package io.github.apace100.apoli.component.item;

import io.github.apace100.apoli.Apoli;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ApoliDataComponentTypes {

    public static final ComponentType<ItemPowersComponent> POWERS = ComponentType.<ItemPowersComponent>builder()
        .codec(ItemPowersComponent.CODEC)
        .packetCodec(ItemPowersComponent.PACKET_CODEC)
        .build();

    public static void register() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Apoli.identifier("powers"), POWERS);
    }

}
