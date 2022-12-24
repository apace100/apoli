package io.github.apace100.apoli.power.factory.action.entity;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.networking.ModPackets;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class SpawnParticlesAction {
    public static void action(SerializableData.Instance data, Entity entity) {
        if(entity.world.isClient) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) entity.world;
        int count = data.get("count");
        if(count <= 0)
            return;
        boolean force = data.get("force");
        float speed = data.get("speed");
        Vec3d spread = data.get("spread");
        float deltaX = (float) (entity.getWidth() * spread.x);
        float deltaY = (float) (entity.getHeight() * spread.y);
        float deltaZ = (float) (entity.getWidth() * spread.z);
        float offsetY = entity.getHeight() * data.getFloat("offset_y");
        Vec3d velocity = data.get("velocity");

        sendParticlePacket(serverWorld, data.get("particle"), force, entity.getX(), entity.getY() + offsetY, entity.getZ(), deltaX, deltaY, deltaZ, Optional.ofNullable(velocity), speed, count);
    }

    private static void sendParticlePacket(ServerWorld world, ParticleEffect effect, boolean force, double x, double y, double z, float offsetX, float offsetY, float offsetZ, Optional<Vec3d> velocity, float speed, int count) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        SerializableDataTypes.PARTICLE_EFFECT.send(buf, effect);
        buf.writeBoolean(force);

        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);

        buf.writeFloat(offsetX);
        buf.writeFloat(offsetY);
        buf.writeFloat(offsetZ);

        buf.writeBoolean(velocity.isPresent());
        velocity.ifPresentOrElse((Vec3d vec3d) -> {
            buf.writeDouble(vec3d.x);
            buf.writeDouble(vec3d.y);
            buf.writeDouble(vec3d.z);
        }, () -> buf.writeFloat(speed));

        buf.writeInt(count);

        for (int j = 0; j < world.getPlayers().size(); ++j) {
            ServerPlayerEntity player = world.getPlayers().get(j);

            if (player.getWorld() != world) return;
            BlockPos blockPos = player.getBlockPos();
            if (blockPos.isWithinDistance(new Vec3d(x, y, z), force ? 512.0 : 32.0)) {
                ServerPlayNetworking.send(player, ModPackets.SEND_PARTICLES, buf);
            }
        }
    }

    public static ActionFactory<Entity> getFactory() {
        return new ActionFactory<>(Apoli.identifier("spawn_particles"),
                new SerializableData()
                        .add("particle", SerializableDataTypes.PARTICLE_EFFECT_OR_TYPE)
                        .add("count", SerializableDataTypes.INT)
                        .add("speed", SerializableDataTypes.FLOAT, 0.0F)
                        .add("force", SerializableDataTypes.BOOLEAN, false)
                        .add("velocity", SerializableDataTypes.VECTOR, null)
                        .add("spread", SerializableDataTypes.VECTOR, new Vec3d(0.5, 0.25, 0.5))
                        .add("offset_y", SerializableDataTypes.FLOAT, 0.5F),
                io.github.apace100.apoli.power.factory.action.entity.SpawnParticlesAction::action
        );
    }
}
