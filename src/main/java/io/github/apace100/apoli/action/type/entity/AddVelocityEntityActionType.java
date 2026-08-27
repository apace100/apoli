package io.github.apace100.apoli.action.type.entity;

import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.context.EntityActionContext;
import io.github.apace100.apoli.action.type.EntityActionType;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.Entity;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

public class AddVelocityEntityActionType extends EntityActionType {

    public static final TypedDataObjectFactory<AddVelocityEntityActionType> DATA_FACTORY = TypedDataObjectFactory.simple(
        new SerializableData()
            .add("x", SerializableDataTypes.FLOAT, 0F)
            .add("y", SerializableDataTypes.FLOAT, 0F)
            .add("z", SerializableDataTypes.FLOAT, 0F)
            .addFunctionedDefault("velocity", ApoliDataTypes.VECTOR_3_FLOAT, data -> new Vector3f(data.getFloat("x"), data.getFloat("y"), data.getFloat("z")))
            .add("x_modifier", Modifier.DATA_TYPE, null)
            .add("y_modifier", Modifier.DATA_TYPE, null)
            .add("z_modifier", Modifier.DATA_TYPE, null)
            .addFunctionedDefault("x_modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrEmpty(data.get("x_modifier")))
            .addFunctionedDefault("y_modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrEmpty(data.get("y_modifier")))
            .addFunctionedDefault("z_modifiers", Modifier.LIST_TYPE, data -> MiscUtil.singletonListOrEmpty(data.get("z_modifier")))
            .add("space", ApoliDataTypes.SPACE, Space.WORLD)
            .add("set", SerializableDataTypes.BOOLEAN, false),
        data -> new AddVelocityEntityActionType(
            data.get("velocity"),
            data.get("space"),
            data.get("set"),
            data.get("x_modifiers"),
            data.get("y_modifiers"),
            data.get("z_modifiers")
        ),
        (actionType, serializableData) -> serializableData.instance()
            .set("velocity", actionType.velocity)
            .set("space", actionType.space)
            .set("set", actionType.set)
            .set("x_modifiers", actionType.xModifiers)
            .set("y_modifiers", actionType.yModifiers)
            .set("z_modifiers", actionType.zModifiers)
    );

    private final Vector3f velocity;
    private final Space space;

    private final boolean set;

    private final List<Modifier> xModifiers;
    private final List<Modifier> yModifiers;
    private final List<Modifier> zModifiers;

    public AddVelocityEntityActionType(Vector3f velocity, Space space, boolean set) {
        this(velocity, space, set, List.of(), List.of(), List.of());
    }

    public AddVelocityEntityActionType(Vector3f velocity, Space space, boolean set, List<Modifier> xModifiers, List<Modifier> yModifiers, List<Modifier> zModifiers) {
        this.velocity = velocity;
        this.space = space;
        this.set = set;
        this.xModifiers = xModifiers;
        this.yModifiers = yModifiers;
        this.zModifiers = zModifiers;
    }

    @Override
    public void accept(EntityActionContext context) {

        Entity entity = context.entity();

        Vector3f velocityCopy = new Vector3f(velocity);
        velocityCopy.set(
            ModifierUtil.applyModifiers(entity, this.xModifiers, velocityCopy.x()),
            ModifierUtil.applyModifiers(entity, this.yModifiers, velocityCopy.y()),
            ModifierUtil.applyModifiers(entity, this.zModifiers, velocityCopy.z())
        );
        TriConsumer<Float, Float, Float> method = set
            ? entity::setVelocity
            : entity::addVelocity;

        space.toGlobal(velocityCopy, entity);
        method.accept(velocityCopy.x(), velocityCopy.y(), velocityCopy.z());

        entity.velocityModified = true;

    }

    @Override
    public @NotNull ActionConfiguration<?> getConfig() {
        return EntityActionTypes.ADD_VELOCITY;
    }

}
