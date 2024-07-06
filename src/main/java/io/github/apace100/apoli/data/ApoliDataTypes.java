package io.github.apace100.apoli.data;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.action.*;
import io.github.apace100.apoli.power.factory.condition.*;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.*;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.SerializationHelper;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.ArgumentWrapper;
import io.github.apace100.calio.util.DynamicIdentifier;
import io.github.apace100.calio.util.IdentifierAlias;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.component.ComponentChanges;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.AdvancementCommand;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ApoliDataTypes {

    public static final SerializableDataType<PowerTypeReference> POWER_TYPE = SerializableDataType.wrap(
        PowerTypeReference.class, SerializableDataTypes.IDENTIFIER,
        PowerType::getIdentifier, PowerTypeReference::new);

    public static final SerializableDataType<ConditionFactory<Entity>.Instance> ENTITY_CONDITION = condition(ApoliRegistries.ENTITY_CONDITION, EntityConditions.ALIASES, "Entity condition type");

    public static final SerializableDataType<List<ConditionFactory<Entity>.Instance>> ENTITY_CONDITIONS = SerializableDataType.list(ENTITY_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_CONDITION = condition(ApoliRegistries.BIENTITY_CONDITION, BiEntityConditions.ALIASES, "Bi-entity condition type");

    public static final SerializableDataType<List<ConditionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_CONDITIONS = SerializableDataType.list(BIENTITY_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<World, ItemStack>>.Instance> ITEM_CONDITION = condition(ApoliRegistries.ITEM_CONDITION, ItemConditions.ALIASES, "Item condition type");

    public static final SerializableDataType<List<ConditionFactory<Pair<World, ItemStack>>.Instance>> ITEM_CONDITIONS = SerializableDataType.list(ITEM_CONDITION);

    public static final SerializableDataType<ConditionFactory<CachedBlockPosition>.Instance> BLOCK_CONDITION = condition(ApoliRegistries.BLOCK_CONDITION, BlockConditions.ALIASES, "Block condition type");

    public static final SerializableDataType<List<ConditionFactory<CachedBlockPosition>.Instance>> BLOCK_CONDITIONS = SerializableDataType.list(BLOCK_CONDITION);

    public static final SerializableDataType<ConditionFactory<FluidState>.Instance> FLUID_CONDITION = condition(ApoliRegistries.FLUID_CONDITION, FluidConditions.ALIASES, "Fluid condition type");

    public static final SerializableDataType<List<ConditionFactory<FluidState>.Instance>> FLUID_CONDITIONS = SerializableDataType.list(FLUID_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<DamageSource, Float>>.Instance> DAMAGE_CONDITION = condition(ApoliRegistries.DAMAGE_CONDITION, DamageConditions.ALIASES, "Damage condition type");

    public static final SerializableDataType<List<ConditionFactory<Pair<DamageSource, Float>>.Instance>> DAMAGE_CONDITIONS = SerializableDataType.list(DAMAGE_CONDITION);

    public static final SerializableDataType<ConditionFactory<RegistryEntry<Biome>>.Instance> BIOME_CONDITION = condition(ApoliRegistries.BIOME_CONDITION, BiomeConditions.ALIASES, "Biome condition type");

    public static final SerializableDataType<List<ConditionFactory<RegistryEntry<Biome>>.Instance>> BIOME_CONDITIONS = SerializableDataType.list(BIOME_CONDITION);

    public static final SerializableDataType<ActionFactory<Entity>.Instance> ENTITY_ACTION = action(ApoliRegistries.ENTITY_ACTION, EntityActions.ALIASES, "Entity action type");

    public static final SerializableDataType<List<ActionFactory<Entity>.Instance>> ENTITY_ACTIONS = SerializableDataType.list(ENTITY_ACTION);

    public static final SerializableDataType<ActionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_ACTION = action(ApoliRegistries.BIENTITY_ACTION, BiEntityActions.ALIASES, "Bi-entity action type");

    public static final SerializableDataType<List<ActionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_ACTIONS = SerializableDataType.list(BIENTITY_ACTION);

    public static final SerializableDataType<ActionFactory<Triple<World, BlockPos, Direction>>.Instance> BLOCK_ACTION = action(ApoliRegistries.BLOCK_ACTION, BlockActions.ALIASES, "Block action type");

    public static final SerializableDataType<List<ActionFactory<Triple<World, BlockPos, Direction>>.Instance>> BLOCK_ACTIONS = SerializableDataType.list(BLOCK_ACTION);

    public static final SerializableDataType<ActionFactory<Pair<World, StackReference>>.Instance> ITEM_ACTION = action(ApoliRegistries.ITEM_ACTION, ItemActions.ALIASES, "Item action type");

    public static final SerializableDataType<List<ActionFactory<Pair<World, StackReference>>.Instance>> ITEM_ACTIONS = SerializableDataType.list(ITEM_ACTION);

    public static final SerializableDataType<Space> SPACE = SerializableDataType.enumValue(Space.class);

    public static final SerializableDataType<ResourceOperation> RESOURCE_OPERATION = SerializableDataType.enumValue(ResourceOperation.class);

    public static final SerializableDataType<InventoryUtil.InventoryType> INVENTORY_TYPE = SerializableDataType.enumValue(InventoryUtil.InventoryType.class);

    public static final SerializableDataType<EnumSet<InventoryUtil.InventoryType>> INVENTORY_TYPE_SET = SerializableDataType.enumSet(InventoryUtil.InventoryType.class, INVENTORY_TYPE);

    public static final SerializableDataType<InventoryUtil.ProcessMode> PROCESS_MODE = SerializableDataType.enumValue(InventoryUtil.ProcessMode.class);

    public static final SerializableDataType<AttributedEntityAttributeModifier> ATTRIBUTED_ATTRIBUTE_MODIFIER = SerializableDataType.compound(
        ClassUtil.castClass(AttributedEntityAttributeModifier.class),
        new SerializableData()
            .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY)
            .add("operation", SerializableDataTypes.MODIFIER_OPERATION)
            .add("value", SerializableDataTypes.DOUBLE)
            .add("id", SerializableDataTypes.IDENTIFIER),
        data -> {

            EntityAttributeModifier modifier = new EntityAttributeModifier(
                data.get("id"),
                data.get("value"),
                data.get("operation")
            );

            return new AttributedEntityAttributeModifier(
                data.get("attribute"),
                modifier
            );

        },
        (serializableData, attributedEntityAttributeModifier) -> {

            SerializableData.Instance data = serializableData.new Instance();
            EntityAttributeModifier attributeModifier = attributedEntityAttributeModifier.modifier();

            data.set("attribute", attributedEntityAttributeModifier.attribute());
            data.set("operation", attributeModifier.operation());
            data.set("value", attributeModifier.value());
            data.set("id", attributeModifier.id());

            return data;

        }
    );

    public static final SerializableDataType<List<AttributedEntityAttributeModifier>> ATTRIBUTED_ATTRIBUTE_MODIFIERS =
        SerializableDataType.list(ATTRIBUTED_ATTRIBUTE_MODIFIER);

    public static final SerializableDataType<Pair<Integer, ItemStack>> POSITIONED_ITEM_STACK = SerializableDataType.compound(
        ClassUtil.castClass(Pair.class),
        new SerializableData()
            .add("item", SerializableDataTypes.ITEM)
            .add("amount", SerializableDataTypes.INT, 1)
            .add("components", SerializableDataTypes.COMPONENT_CHANGES, ComponentChanges.EMPTY)
            .add("slot", SerializableDataTypes.INT, Integer.MIN_VALUE),
        data -> {

            Item item = data.get("item");
            ItemStack stack = item.getDefaultStack();

            stack.setCount(data.getInt("amount"));
            stack.applyChanges(data.get("components"));

            return new Pair<>(data.getInt("slot"), stack);

        },
        (serializableData, slotAndStack) -> {

            SerializableData.Instance data = serializableData.new Instance();
            ItemStack stack = slotAndStack.getRight();

            data.set("item", stack.getItem());
            data.set("amount", stack.getCount());
            data.set("components", stack.getComponentChanges());
            data.set("slot", slotAndStack.getLeft());

            return data;

        }
    );

    public static final SerializableDataType<List<Pair<Integer, ItemStack>>> POSITIONED_ITEM_STACKS = SerializableDataType.list(POSITIONED_ITEM_STACK);

    public static final SerializableDataType<Active.Key> KEY = SerializableDataType.compound(
        Active.Key.class,
        new SerializableData()
            .add("key", SerializableDataTypes.STRING)
            .add("continuous", SerializableDataTypes.BOOLEAN, false),
        data -> {

            Active.Key key = new Active.Key();

            key.key = data.getString("key");
            key.continuous = data.getBoolean("continuous");

            return key;

        },
        (serializableData, key) -> {

            SerializableData.Instance data = serializableData.new Instance();

            data.set("key", key.key);
            data.set("continuous", key.continuous);

            return data;

        }
    );

    public static final SerializableDataType<Active.Key> BACKWARDS_COMPATIBLE_KEY = new SerializableDataType<>(
        Active.Key.class,
        KEY::send,
        KEY::receive,
        jsonElement -> {

            if (!(jsonElement instanceof JsonPrimitive jsonPrimitive) || !jsonPrimitive.isString()) {
                return KEY.read(jsonElement);
            }

            Active.Key key = new Active.Key();

            key.key = jsonPrimitive.getAsString();
            key.continuous = false;

            return key;

        },
        KEY::write
    );

    public static final SerializableDataType<HudRender> SINGLE_HUD_RENDER = SerializableDataType.compound(
        HudRender.class,
        new SerializableData()
            .add("should_render", SerializableDataTypes.BOOLEAN, true)
            .add("bar_index", SerializableDataTypes.INT, 0)
            .add("icon_index", SerializableDataTypes.INT, 0)
            .add("sprite_location", SerializableDataTypes.IDENTIFIER, Apoli.identifier("textures/gui/resource_bar.png"))
            .add("condition", ENTITY_CONDITION, null)
            .add("inverted", SerializableDataTypes.BOOLEAN, false)
            .add("order", SerializableDataTypes.INT, 0),
        (data) -> new HudRender(
            data.get("should_render"),
            data.get("bar_index"),
            data.get("icon_index"),
            data.get("sprite_location"),
            data.get("condition"),
            data.get("inverted"),
            data.get("order")
        ),
        (serializableData, hudRender) -> {

            SerializableData.Instance data = serializableData.new Instance();

            data.set("should_render", hudRender.shouldRender());
            data.set("bar_index", hudRender.getBarIndex());
            data.set("icon_index", hudRender.getIconIndex());
            data.set("sprite_location", hudRender.getSpriteLocation());
            data.set("condition", hudRender.getCondition());
            data.set("inverted", hudRender.isInverted());
            data.set("order", hudRender.getOrder());

            return data;

        }
    );

    public static final SerializableDataType<List<HudRender>> MULTIPLE_HUD_RENDERS = SerializableDataType.list(SINGLE_HUD_RENDER);

    /**
     *  <p>A HUD render data type that accepts either a single HUD render or multiple HUD renders. The first HUD render will be considered
     *  the <b>"parent"</b> and the following HUD renders will be considered its <b>"children."</b></p>
     *
     *  <p>If the children don't specify an order value, the order value of the parent will be inherited instead.</p>
     */
    public static final SerializableDataType<HudRender> HUD_RENDER = new SerializableDataType<>(
        HudRender.class,
        (buf, hudRender) -> hudRender.send(buf),
        HudRender::receive,
        jsonElement -> {

            LinkedList<HudRender> hudRenders = (LinkedList<HudRender>) MULTIPLE_HUD_RENDERS.read(jsonElement);
            if (hudRenders.isEmpty()) {
                return HudRender.DONT_RENDER;
            }

            HudRender parentHudRender = hudRenders.removeFirst();
            for (HudRender hudRender : hudRenders) {
                parentHudRender.addChild(hudRender);
            }

            return parentHudRender;

        },
        SINGLE_HUD_RENDER::write
    );

    public static final SerializableDataType<Comparison> COMPARISON = SerializableDataType.enumValue(Comparison.class,
        SerializationHelper.buildEnumMap(Comparison.class, Comparison::getComparisonString));

    public static final SerializableDataType<PlayerAbility> PLAYER_ABILITY = SerializableDataType.wrap(
        PlayerAbility.class, SerializableDataTypes.IDENTIFIER,
        PlayerAbility::getId, id -> Pal.provideRegisteredAbility(id).get());

    public static final SerializableDataType<ArgumentWrapper<Integer>> ITEM_SLOT = SerializableDataType.argumentType(ItemSlotArgumentType.itemSlot());

    public static final SerializableDataType<List<ArgumentWrapper<Integer>>> ITEM_SLOTS = SerializableDataType.list(ITEM_SLOT);

    public static final SerializableDataType<Explosion.DestructionType> BACKWARDS_COMPATIBLE_DESTRUCTION_TYPE = SerializableDataType.mapped(Explosion.DestructionType.class,
            HashBiMap.create(ImmutableBiMap.of(
                    "none", Explosion.DestructionType.KEEP,
                    "break", Explosion.DestructionType.DESTROY,
                    "destroy", Explosion.DestructionType.DESTROY_WITH_DECAY)
            ));

    public static final SerializableDataType<ArgumentWrapper<EntitySelector>> ENTITIES_SELECTOR = SerializableDataType.argumentType(EntityArgumentType.entities());

    public static final SerializableDataType<DamageSourceDescription> DAMAGE_SOURCE_DESCRIPTION = SerializableDataType.compound(DamageSourceDescription.class,
            DamageSourceDescription.DATA, DamageSourceDescription::fromData, DamageSourceDescription::toData);

    public static final SerializableDataType<LegacyMaterial> LEGACY_MATERIAL = SerializableDataType.wrap(
            LegacyMaterial.class, SerializableDataTypes.STRING,
            LegacyMaterial::getMaterial, LegacyMaterial::new
    );

    public static final SerializableDataType<AdvancementCommand.Operation> ADVANCEMENT_OPERATION = SerializableDataType.enumValue(AdvancementCommand.Operation.class);

    public static final SerializableDataType<AdvancementCommand.Selection> ADVANCEMENT_SELECTION = SerializableDataType.enumValue(AdvancementCommand.Selection.class);

    public static final SerializableDataType<List<LegacyMaterial>> LEGACY_MATERIALS = SerializableDataType.list(LEGACY_MATERIAL);

    public static final SerializableDataType<ClickType> CLICK_TYPE = SerializableDataType.enumValue(ClickType.class);

    public static final SerializableDataType<EnumSet<ClickType>> CLICK_TYPE_SET = SerializableDataType.enumSet(ClickType.class, CLICK_TYPE);

    public static final SerializableDataType<TextAlignment> TEXT_ALIGNMENT = SerializableDataType.enumValue(TextAlignment.class);

    public static final SerializableDataType<Map<Identifier, Identifier>> IDENTIFIER_MAP = new SerializableDataType<>(
        ClassUtil.castClass(Map.class),
        (buffer, idMap) -> buffer.writeMap(
            idMap,
            PacketByteBuf::writeIdentifier,
            PacketByteBuf::writeIdentifier
        ),
        buffer -> buffer.readMap(
            PacketByteBuf::readIdentifier,
            PacketByteBuf::readIdentifier
        ),
        jsonElement -> {

            if (!(jsonElement instanceof JsonObject jsonObject)) {
                throw new JsonParseException("Expected a JSON object");
            }

            Map<Identifier, Identifier> map = new LinkedHashMap<>();
            for (String key : jsonObject.keySet()) {

                if (!(jsonObject.get(key) instanceof JsonPrimitive jsonPrimitive) || !jsonPrimitive.isString()) {
                    continue;
                }

                Identifier keyId = DynamicIdentifier.of(key);
                Identifier valId = DynamicIdentifier.of(jsonPrimitive.getAsString());

                map.put(keyId, valId);

            }

            return map;

        },
        idMap -> {

            JsonObject jsonObject = new JsonObject();
            idMap.forEach((keyId, valId) -> jsonObject.addProperty(keyId.toString(), valId.toString()));

            return jsonObject;

        }
    );

    public static final SerializableDataType<Map<Pattern, Identifier>> REGEX_MAP = new SerializableDataType<>(
        ClassUtil.castClass(Map.class),
        (buffer, regexMap) -> buffer.writeMap(
            regexMap,
            (keyBuffer, pattern) -> keyBuffer.writeString(pattern.toString()),
            PacketByteBuf::writeIdentifier
        ),
        buffer -> buffer.readMap(
            keyBuffer -> Pattern.compile(keyBuffer.readString()),
            PacketByteBuf::readIdentifier
        ),
        jsonElement -> {

            if (!(jsonElement instanceof JsonObject jsonObject)) {
                throw new JsonSyntaxException("Expected a JSON object.");
            }

            Map<Pattern, Identifier> regexMap = new HashMap<>();
            for (String key : jsonObject.keySet()) {

                if (!(jsonObject.get(key) instanceof JsonPrimitive jsonPrimitive) || !jsonPrimitive.isString()) {
                    continue;
                }

                Pattern pattern = Pattern.compile(key);
                Identifier id = DynamicIdentifier.of(jsonPrimitive);

                regexMap.put(pattern, id);

            }

            return regexMap;

        },
        regexMap -> {

            JsonObject jsonObject = new JsonObject();
            regexMap.forEach((regex, id) -> jsonObject.addProperty(regex.pattern(), id.toString()));

            return jsonObject;

        }
    );

    public static final SerializableDataType<GameMode> GAME_MODE = SerializableDataType.enumValue(GameMode.class);

    //  This is for keeping backwards compatibility to fields that used to accept strings as translation keys
    public static final SerializableDataType<Text> DEFAULT_TRANSLATABLE_TEXT = new SerializableDataType<>(
        ClassUtil.castClass(Text.class),
        TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC::encode,
        TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC::decode,
        jsonElement -> jsonElement instanceof JsonPrimitive jsonPrimitive
            ? Text.translatable(jsonPrimitive.getAsString())
            : SerializableDataTypes.TEXT.read(jsonElement),
        text -> TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text)
            .mapError(err -> "Failed to serialize text to JSON (skipping): " + err)
            .resultOrPartial(Apoli.LOGGER::warn)
            .orElseGet(JsonObject::new)
    );

    public static final SerializableDataType<Integer> NON_NEGATIVE_INT = SerializableDataType.boundNumber(
        SerializableDataTypes.INT, 0, Integer.MAX_VALUE,
        value -> (min, max) -> {

            if (value < min) {
                throw new IllegalArgumentException("Expected value to be equal or greater than " + min + "! (current value: " + value + ")");
            }

            return value;

        }
    );
  
    public static final SerializableDataType<StackClickPhase> STACK_CLICK_PHASE = SerializableDataType.enumValue(StackClickPhase.class);

    public static final SerializableDataType<EnumSet<StackClickPhase>> STACK_CLICK_PHASE_SET = SerializableDataType.enumSet(StackClickPhase.class, STACK_CLICK_PHASE);
  
    public static final SerializableDataType<BlockUsagePhase> BLOCK_USAGE_PHASE = SerializableDataType.enumValue(BlockUsagePhase.class);

    public static final SerializableDataType<EnumSet<BlockUsagePhase>> BLOCK_USAGE_PHASE_SET = SerializableDataType.enumSet(BlockUsagePhase.class, BLOCK_USAGE_PHASE);

    public static final SerializableDataType<EntityPose> ENTITY_POSE = SerializableDataType.enumValue(EntityPose.class);

    public static final SerializableDataType<ArmPoseReference> ARM_POSE_REFERENCE = SerializableDataType.enumValue(ArmPoseReference.class);

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(Registry<ConditionFactory<T>> registry, String name) {
        return condition(registry, IdentifierAlias.GLOBAL, name);
    }

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(Registry<ConditionFactory<T>> registry, IdentifierAlias aliases, String name) {
        return condition(registry, aliases, (conditionFactories, id) -> new IllegalArgumentException(name + " \"" + id + "\" is not registered"));
    }

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(Registry<ConditionFactory<T>> registry, IdentifierAlias aliases, BiFunction<Registry<ConditionFactory<T>>, Identifier, RuntimeException> errorHandler) {
        return new SerializableDataType<>(
            ClassUtil.castClass(ConditionFactory.Instance.class),
            (buf, instance) -> instance.write(buf),
            buf -> {
                Identifier factoryId = buf.readIdentifier();
                return registry
                    .getOrEmpty(aliases.resolveAlias(factoryId, registry::containsId))
                    .orElseThrow(() -> errorHandler.apply(registry, factoryId))
                    .read(buf);
            },
            jsonElement -> {

                if (!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new JsonSyntaxException("Expected a JSON object.");
                }

                Identifier factoryId = DynamicIdentifier.of(JsonHelper.getString(jsonObject, "type"));
                return registry
                    .getOrEmpty(aliases.resolveAlias(factoryId, registry::containsId))
                    .orElseThrow(() -> errorHandler.apply(registry, factoryId))
                    .read(jsonObject);

            },
            ConditionFactory.Instance::toJson
        );
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(Registry<ActionFactory<T>> registry, String name) {
        return action(registry, IdentifierAlias.GLOBAL, name);
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(Registry<ActionFactory<T>> registry, IdentifierAlias aliases, String name) {
        return action(registry, aliases, (conditionFactories, id) -> new IllegalArgumentException(name + " \"" + id + "\" is not registered"));
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(Registry<ActionFactory<T>> registry, IdentifierAlias aliases, BiFunction<Registry<ActionFactory<T>>, Identifier, RuntimeException> errorHandler) {
        return new SerializableDataType<>(
            ClassUtil.castClass(ActionFactory.Instance.class),
            (buf, instance) -> instance.write(buf),
            buf -> {
                Identifier factoryId = buf.readIdentifier();
                return registry
                    .getOrEmpty(aliases.resolveAlias(factoryId, registry::containsId))
                    .orElseThrow(() -> errorHandler.apply(registry, factoryId))
                    .read(buf);
            },
            jsonElement -> {

                if (!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new JsonSyntaxException("Expected a JSON object.");
                }

                Identifier factoryId = DynamicIdentifier.of(JsonHelper.getString(jsonObject, "type"));
                return registry
                    .getOrEmpty(aliases.resolveAlias(factoryId, registry::containsId))
                    .orElseThrow(() -> errorHandler.apply(registry, factoryId))
                    .read(jsonObject);

            },
            ActionFactory.Instance::toJson
        );
    }

}
