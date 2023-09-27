package io.github.apace100.apoli.networking;

import io.github.apace100.apoli.networking.packet.VersionHandshakePacket;
import io.github.apace100.apoli.networking.packet.c2s.PlayerLandedC2SPacket;
import io.github.apace100.apoli.networking.packet.c2s.PreventedEntityInteractionC2SPacket;
import io.github.apace100.apoli.networking.packet.c2s.UseActivePowersC2SPacket;
import io.github.apace100.apoli.networking.packet.s2c.*;
import net.minecraft.util.Identifier;


/**
 *  A class containing all the {@link Identifier Identifiers} of the packets used by Apoli. This is <b>deprecated</b>, instantiate
 *  classes from the package {@link io.github.apace100.apoli.networking.packet} instead.
 */
@SuppressWarnings("unused")
@Deprecated(forRemoval = true, since = "1.11.0")
public class ModPackets {

    public static final Identifier HANDSHAKE = VersionHandshakePacket.TYPE.getId();
    public static final Identifier USE_ACTIVE_POWERS = UseActivePowersC2SPacket.TYPE.getId();
    public static final Identifier POWER_LIST = SyncPowerTypeRegistryS2CPacket.TYPE.getId();
    public static final Identifier SYNC_POWER = SyncPowerS2CPacket.TYPE.getId();
    public static final Identifier PLAYER_LANDED = PlayerLandedC2SPacket.TYPE.getId();
    public static final Identifier PLAYER_MOUNT = MountPlayerS2CPacket.TYPE.getId();
    public static final Identifier PLAYER_DISMOUNT = DismountPlayerS2CPacket.TYPE.getId();
    public static final Identifier PREVENTED_ENTITY_USE = PreventedEntityInteractionC2SPacket.TYPE.getId();
    public static final Identifier SET_ATTACKER = SyncAttackerS2CPacket.TYPE.getId();
    public static final Identifier SYNC_STATUS_EFFECT = SyncStatusEffectS2CPacket.TYPE.getId();

}
