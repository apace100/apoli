package io.github.apace100.apoli.data;

import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.action.ActionType;
import io.github.apace100.apoli.power.factory.action.ActionTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionType;
import io.github.apace100.apoli.power.factory.condition.ConditionTypes;
import io.github.apace100.apoli.util.AttributedEntityAttributeModifier;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.apoli.util.Space;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.SerializationHelper;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.client.render.CameraSubmersionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Triple;

import java.util.EnumSet;
import java.util.List;

public class ApoliDataTypes {

    public static final SerializableDataType<PowerTypeReference> POWER_TYPE = SerializableDataType.wrap(
        PowerTypeReference.class, SerializableDataTypes.IDENTIFIER,
        PowerType::getIdentifier, PowerTypeReference::new);

    public static final SerializableDataType<ConditionFactory<Entity>.Instance> ENTITY_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.ENTITY);

    public static final SerializableDataType<List<ConditionFactory<Entity>.Instance>> ENTITY_CONDITIONS =
        SerializableDataType.list(ENTITY_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.BIENTITY);

    public static final SerializableDataType<List<ConditionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_CONDITIONS =
        SerializableDataType.list(BIENTITY_CONDITION);

    public static final SerializableDataType<ConditionFactory<ItemStack>.Instance> ITEM_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.ITEM);

    public static final SerializableDataType<List<ConditionFactory<ItemStack>.Instance>> ITEM_CONDITIONS =
        SerializableDataType.list(ITEM_CONDITION);

    public static final SerializableDataType<ConditionFactory<CachedBlockPosition>.Instance> BLOCK_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.BLOCK);

    public static final SerializableDataType<List<ConditionFactory<CachedBlockPosition>.Instance>> BLOCK_CONDITIONS =
        SerializableDataType.list(BLOCK_CONDITION);

    public static final SerializableDataType<ConditionFactory<FluidState>.Instance> FLUID_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.FLUID);

    public static final SerializableDataType<List<ConditionFactory<FluidState>.Instance>> FLUID_CONDITIONS =
        SerializableDataType.list(FLUID_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<DamageSource, Float>>.Instance> DAMAGE_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.DAMAGE);

    public static final SerializableDataType<List<ConditionFactory<Pair<DamageSource, Float>>.Instance>> DAMAGE_CONDITIONS =
        SerializableDataType.list(DAMAGE_CONDITION);

    public static final SerializableDataType<ConditionFactory<Biome>.Instance> BIOME_CONDITION =
        condition(ClassUtil.castClass(ConditionFactory.Instance.class), ConditionTypes.BIOME);

    public static final SerializableDataType<List<ConditionFactory<Biome>.Instance>> BIOME_CONDITIONS =
        SerializableDataType.list(BIOME_CONDITION);

    public static final SerializableDataType<ActionFactory<Entity>.Instance> ENTITY_ACTION =
        action(ClassUtil.castClass(ActionFactory.Instance.class), ActionTypes.ENTITY);

    public static final SerializableDataType<List<ActionFactory<Entity>.Instance>> ENTITY_ACTIONS =
        SerializableDataType.list(ENTITY_ACTION);

    public static final SerializableDataType<ActionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_ACTION =
        action(ClassUtil.castClass(ActionFactory.Instance.class), ActionTypes.BIENTITY);

    public static final SerializableDataType<List<ActionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_ACTIONS =
        SerializableDataType.list(BIENTITY_ACTION);

    public static final SerializableDataType<ActionFactory<Triple<World, BlockPos, Direction>>.Instance> BLOCK_ACTION =
        action(ClassUtil.castClass(ActionFactory.Instance.class), ActionTypes.BLOCK);

    public static final SerializableDataType<List<ActionFactory<Triple<World, BlockPos, Direction>>.Instance>> BLOCK_ACTIONS =
        SerializableDataType.list(BLOCK_ACTION);

    public static final SerializableDataType<ActionFactory<Pair<World, ItemStack>>.Instance> ITEM_ACTION =
        action(ClassUtil.castClass(ActionFactory.Instance.class), ActionTypes.ITEM);

    public static final SerializableDataType<List<ActionFactory<Pair<World, ItemStack>>.Instance>> ITEM_ACTIONS =
        SerializableDataType.list(ITEM_ACTION);

    public static final SerializableDataType<Space> SPACE = SerializableDataType.enumValue(Space.class);

    public static final SerializableDataType<AttributedEntityAttributeModifier> ATTRIBUTED_ATTRIBUTE_MODIFIER = SerializableDataType.compound(
        AttributedEntityAttributeModifier.class,
        new SerializableData()
            .add("attribute", SerializableDataTypes.ATTRIBUTE)
            .add("operation", SerializableDataTypes.MODIFIER_OPERATION)
            .add("value", SerializableDataTypes.DOUBLE)
            .add("name", SerializableDataTypes.STRING, "Unnamed EntityAttributeModifier"),
        dataInst -> new AttributedEntityAttributeModifier((EntityAttribute)dataInst.get("attribute"),
            new EntityAttributeModifier(
                dataInst.getString("name"),
                dataInst.getDouble("value"),
                (EntityAttributeModifier.Operation)dataInst.get("operation"))),
        (data, inst) -> {
            SerializableData.Instance dataInst = data.new Instance();
            dataInst.set("attribute", inst.getAttribute());
            dataInst.set("operation", inst.getModifier().getOperation());
            dataInst.set("value", inst.getModifier().getValue());
            dataInst.set("name", inst.getModifier().getName());
            return dataInst;
        });

    public static final SerializableDataType<List<AttributedEntityAttributeModifier>> ATTRIBUTED_ATTRIBUTE_MODIFIERS =
        SerializableDataType.list(ATTRIBUTED_ATTRIBUTE_MODIFIER);

    public static final SerializableDataType<Pair<Integer, ItemStack>> POSITIONED_ITEM_STACK = SerializableDataType.compound(ClassUtil.castClass(Pair.class),
        new SerializableData()
            .add("item", SerializableDataTypes.ITEM)
            .add("amount", SerializableDataTypes.INT, 1)
            .add("tag", SerializableDataTypes.NBT, null)
            .add("slot", SerializableDataTypes.INT, Integer.MIN_VALUE),
        (data) ->  {
            ItemStack stack = new ItemStack((Item)data.get("item"), data.getInt("amount"));
            if(data.isPresent("tag")) {
                stack.setNbt((NbtCompound) data.get("tag"));
            }
            return new Pair<>(data.getInt("slot"), stack);
        },
        ((serializableData, positionedStack) -> {
            SerializableData.Instance data = serializableData.new Instance();
            data.set("item", positionedStack.getRight().getItem());
            data.set("amount", positionedStack.getRight().getCount());
            data.set("tag", positionedStack.getRight().hasNbt() ? positionedStack.getRight().getNbt() : null);
            data.set("slot", positionedStack.getLeft());
            return data;
        }));

    public static final SerializableDataType<List<Pair<Integer, ItemStack>>> POSITIONED_ITEM_STACKS = SerializableDataType.list(POSITIONED_ITEM_STACK);

    public static final SerializableDataType<Active.Key> KEY = SerializableDataType.compound(Active.Key.class,
        new SerializableData()
            .add("key", SerializableDataTypes.STRING)
            .add("continuous", SerializableDataTypes.BOOLEAN, false),
        (data) ->  {
            Active.Key key = new Active.Key();
            key.key = data.getString("key");
            key.continuous = data.getBoolean("continuous");
            return key;
        },
        ((serializableData, key) -> {
            SerializableData.Instance data = serializableData.new Instance();
            data.set("key", key.key);
            data.set("continuous", key.continuous);
            return data;
        }));

    public static final SerializableDataType<Active.Key> BACKWARDS_COMPATIBLE_KEY = new SerializableDataType<>(Active.Key.class,
        KEY::send, KEY::receive, jsonElement -> {
        if(jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
            String keyString = jsonElement.getAsString();
            Active.Key key = new Active.Key();
            key.key = keyString;
            key.continuous = false;
            return key;
        }
        return KEY.read(jsonElement);
    });

    public static final SerializableDataType<HudRender> HUD_RENDER = SerializableDataType.compound(HudRender.class, new
            SerializableData()
            .add("should_render", SerializableDataTypes.BOOLEAN, true)
            .add("bar_index", SerializableDataTypes.INT, 0)
            .add("sprite_location", SerializableDataTypes.IDENTIFIER, new Identifier("origins", "textures/gui/resource_bar.png"))
            .add("condition", ENTITY_CONDITION, null)
            .add("inverted", SerializableDataTypes.BOOLEAN, false),
        (dataInst) -> new HudRender(
            dataInst.getBoolean("should_render"),
            dataInst.getInt("bar_index"),
            dataInst.getId("sprite_location"),
            (ConditionFactory<LivingEntity>.Instance)dataInst.get("condition"),
            dataInst.getBoolean("inverted")),
        (data, inst) -> {
            SerializableData.Instance dataInst = data.new Instance();
            dataInst.set("should_render", inst.shouldRender());
            dataInst.set("bar_index", inst.getBarIndex());
            dataInst.set("sprite_location", inst.getSpriteLocation());
            dataInst.set("condition", inst.getCondition());
            dataInst.set("inverted", inst.isInverted());
            return dataInst;
        });

    public static final SerializableDataType<Comparison> COMPARISON = SerializableDataType.enumValue(Comparison.class,
        SerializationHelper.buildEnumMap(Comparison.class, Comparison::getComparisonString));

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(Class<ConditionFactory<T>.Instance> dataClass, ConditionType<T> conditionType) {
        return new SerializableDataType<>(dataClass, conditionType::write, conditionType::read, conditionType::read);
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(Class<ActionFactory<T>.Instance> dataClass, ActionType<T> actionType) {
        return new SerializableDataType<>(dataClass, actionType::write, actionType::read, actionType::read);
    }

}
