package io.github.apace100.apoli.util.transformer;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.RotationAxis;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public enum ApoliTransformOperation implements TransformOperation {

    TRANSLATE {

        private static final SerializableData DATA = new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0.0F)
            .add("y", SerializableDataTypes.FLOAT, 0.0F);

        @Override
        public SerializableData getSerializableData() {
            return DATA;
        }

        @Override
        public void apply(SerializableData.Instance data, MatrixStack matrices, int x, int y, float tickDelta) {
            matrices.translate(data.get("x"), data.get("y"), 0.0F);
        }

    },
    ROTATE {

        private static final SerializableData DATA = new SerializableData()
            .add("axis", ApoliDataTypes.ROTATION_AXIS)
            .add("degrees", SerializableDataTypes.DOUBLE)
            .add("modifier", Modifier.LIST_TYPE, new LinkedList<>());

        @Override
        public SerializableData getSerializableData() {
            return DATA;
        }

        @Override
        public void apply(SerializableData.Instance data, MatrixStack matrices, int x, int y, float tickDelta) {

            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;

            if (player == null) {
                return;
            }

            RotationAxis axis = data.get("axis");
            List<Modifier> modifiers = data.get("modifier");

            float modifiedDegrees = (float) (ModifierUtil.applyModifiers(player, modifiers, data.get("degrees")) % 360);
            matrices.multiply(axis.rotationDegrees(modifiedDegrees), x, y, 0.0F);

        }

    },
    SCALE {

        private static final SerializableData DATA = new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 1.0F)
            .add("y", SerializableDataTypes.FLOAT, 1.0F);

        @Override
        public SerializableData getSerializableData() {
            return DATA;
        }

        @Override
        public void apply(SerializableData.Instance data, MatrixStack matrices, int x, int y, float tickDelta) {
            matrices.scale(data.get("x"), data.get("y"), 1.0F);
        }

    };

    public static void registerAll() {

        for (ApoliTransformOperation operation : ApoliTransformOperation.values()) {
            String path = operation.toString().toLowerCase(Locale.ROOT);
            Registry.register(ApoliRegistries.TRANSFORM_OPERATION, Apoli.identifier(path), operation);
        }

    }

}
