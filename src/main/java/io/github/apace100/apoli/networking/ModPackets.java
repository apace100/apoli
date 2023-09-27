package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.Apoli;
import net.minecraft.util.Identifier;

//  TODO: Refactor how packets are handled
public class ModPackets {

    public static final Identifier HANDSHAKE = Apoli.identifier("handshake");

    public static final Identifier USE_ACTIVE_POWERS = Apoli.identifier("use_active_powers");
    public static final Identifier POWER_LIST = Apoli.identifier("power_list");
    public static final Identifier SYNC_POWER = Apoli.identifier("sync_power");

    public static final Identifier PLAYER_LANDED = Apoli.identifier("player_landed");

    public static final Identifier PLAYER_MOUNT = Apoli.identifier("player_mount");
    public static final Identifier PLAYER_DISMOUNT = Apoli.identifier("player_dismount");

    public static final Identifier PREVENTED_ENTITY_USE = Apoli.identifier("prevented_entity_use");

    public static final Identifier SET_ATTACKER = Apoli.identifier("set_attacker");

    public static final Identifier SYNC_STATUS_EFFECT = Apoli.identifier("sync_status_effect");
}
