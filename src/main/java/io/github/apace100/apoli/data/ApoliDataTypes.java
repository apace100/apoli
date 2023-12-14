package io.github.apace100.apoli.data;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.*;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeReference;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.power.factory.action.ActionType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.power.factory.condition.ConditionType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.*;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.SerializationHelper;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.ArgumentWrapper;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.AdvancementCommand;
import net.minecraft.text.Text;
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
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ApoliDataTypes {

    public static final SerializableDataType<PowerTypeReference> POWER_TYPE = SerializableDataType.wrap(
        PowerTypeReference.class, SerializableDataTypes.IDENTIFIER,
        PowerType::getIdentifier, PowerTypeReference::new);

    public static final SerializableDataType<ConditionFactory<Entity>.Instance> ENTITY_CONDITION =
        condition(ApoliRegistries.ENTITY_CONDITION, "Entity condition");

    public static final SerializableDataType<List<ConditionFactory<Entity>.Instance>> ENTITY_CONDITIONS =
        SerializableDataType.list(ENTITY_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_CONDITION =
        condition(ApoliRegistries.BIENTITY_CONDITION, "Bi-entity condition");

    public static final SerializableDataType<List<ConditionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_CONDITIONS =
        SerializableDataType.list(BIENTITY_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<World, ItemStack>>.Instance> ITEM_CONDITION =
        condition(ApoliRegistries.ITEM_CONDITION, "Item condition");

    public static final SerializableDataType<List<ConditionFactory<Pair<World, ItemStack>>.Instance>> ITEM_CONDITIONS =
        SerializableDataType.list(ITEM_CONDITION);

    public static final SerializableDataType<ConditionFactory<CachedBlockPosition>.Instance> BLOCK_CONDITION =
        condition(ApoliRegistries.BLOCK_CONDITION, "Block condition");

    public static final SerializableDataType<List<ConditionFactory<CachedBlockPosition>.Instance>> BLOCK_CONDITIONS =
        SerializableDataType.list(BLOCK_CONDITION);

    public static final SerializableDataType<ConditionFactory<FluidState>.Instance> FLUID_CONDITION =
        condition(ApoliRegistries.FLUID_CONDITION, "Fluid condition");

    public static final SerializableDataType<List<ConditionFactory<FluidState>.Instance>> FLUID_CONDITIONS =
        SerializableDataType.list(FLUID_CONDITION);

    public static final SerializableDataType<ConditionFactory<Pair<DamageSource, Float>>.Instance> DAMAGE_CONDITION =
        condition(ApoliRegistries.DAMAGE_CONDITION, "Damage condition");

    public static final SerializableDataType<List<ConditionFactory<Pair<DamageSource, Float>>.Instance>> DAMAGE_CONDITIONS =
        SerializableDataType.list(DAMAGE_CONDITION);

    public static final SerializableDataType<ConditionFactory<RegistryEntry<Biome>>.Instance> BIOME_CONDITION =
        condition(ApoliRegistries.BIOME_CONDITION, "Biome condition");

    public static final SerializableDataType<List<ConditionFactory<RegistryEntry<Biome>>.Instance>> BIOME_CONDITIONS =
        SerializableDataType.list(BIOME_CONDITION);

    public static final SerializableDataType<ActionFactory<Entity>.Instance> ENTITY_ACTION =
        action(ApoliRegistries.ENTITY_ACTION, "Entity action");

    public static final SerializableDataType<List<ActionFactory<Entity>.Instance>> ENTITY_ACTIONS =
        SerializableDataType.list(ENTITY_ACTION);

    public static final SerializableDataType<ActionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_ACTION =
        action(ApoliRegistries.BIENTITY_ACTION, "Bi-entity action");

    public static final SerializableDataType<List<ActionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_ACTIONS =
        SerializableDataType.list(BIENTITY_ACTION);

    public static final SerializableDataType<ActionFactory<Triple<World, BlockPos, Direction>>.Instance> BLOCK_ACTION =
        action(ApoliRegistries.BLOCK_ACTION, "Block action");

    public static final SerializableDataType<List<ActionFactory<Triple<World, BlockPos, Direction>>.Instance>> BLOCK_ACTIONS =
        SerializableDataType.list(BLOCK_ACTION);

    public static final SerializableDataType<ActionFactory<Pair<World, ItemStack>>.Instance> ITEM_ACTION =
        action(ApoliRegistries.ITEM_ACTION, "Item action");

    public static final SerializableDataType<List<ActionFactory<Pair<World, ItemStack>>.Instance>> ITEM_ACTIONS =
        SerializableDataType.list(ITEM_ACTION);

    public static final SerializableDataType<Space> SPACE = SerializableDataType.enumValue(Space.class);

    public static final SerializableDataType<ResourceOperation> RESOURCE_OPERATION = SerializableDataType.enumValue(ResourceOperation.class);

    public static final SerializableDataType<InventoryUtil.InventoryType> INVENTORY_TYPE = SerializableDataType.enumValue(InventoryUtil.InventoryType.class);

    public static final SerializableDataType<EnumSet<InventoryUtil.InventoryType>> INVENTORY_TYPE_SET = SerializableDataType.enumSet(InventoryUtil.InventoryType.class, INVENTORY_TYPE);

    public static final SerializableDataType<InventoryUtil.ProcessMode> PROCESS_MODE = SerializableDataType.enumValue(InventoryUtil.ProcessMode.class);

    public static final SerializableDataType<AttributedEntityAttributeModifier> ATTRIBUTED_ATTRIBUTE_MODIFIER = SerializableDataType.compound(
        AttributedEntityAttributeModifier.class,
        new SerializableData()
            .add("attribute", SerializableDataTypes.ATTRIBUTE)
            .add("operation", SerializableDataTypes.MODIFIER_OPERATION)
            .add("value", SerializableDataTypes.DOUBLE)
            .add("name", SerializableDataTypes.STRING, "Unnamed EntityAttributeModifier"),
        dataInst -> new AttributedEntityAttributeModifier(dataInst.get("attribute"),
            new EntityAttributeModifier(
                dataInst.getString("name"),
                dataInst.getDouble("value"),
                dataInst.get("operation"))),
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
                stack.setNbt(data.get("tag"));
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

                Identifier keyId = new Identifier(key);
                Identifier valId = new Identifier(jsonPrimitive.getAsString());

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
                Identifier id = new Identifier(jsonPrimitive.getAsString());

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
        Text.class,
        PacketByteBuf::writeText,
        PacketByteBuf::readText,
        jsonElement -> {

            //  If the JSON is a primitive, use it as a translation key
            if (jsonElement instanceof JsonPrimitive jsonPrimitive) {
                return Text.translatable(jsonPrimitive.getAsString());
            }

            //  Otherwise, serialize it as a text as usual
            return Text.Serializer.fromJson(jsonElement);

        },
        Text.Serializer::toJsonTree
    );

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(Registry<ConditionFactory<T>> registry, String name) {
        return new SerializableDataType<>(
            ClassUtil.castClass(ConditionFactory.Instance.class),
            (buffer, instance) -> instance.write(buffer),
            buffer -> {

                Identifier factoryId = buffer.readIdentifier();
                Optional<ConditionFactory<T>> factory = registry.getOrEmpty(factoryId);

                if (factory.isEmpty() && IdentifierAlias.hasAlias(factoryId)) {
                    factory = registry.getOrEmpty(IdentifierAlias.resolveAlias(factoryId, IdentifierAlias.Priority.NAMESPACE));
                }

                return factory
                    .orElseThrow(() -> new JsonSyntaxException("%s type \"%s\" was not registered.".formatted(name, factoryId)))
                    .read(buffer);

            },
            jsonElement -> {

                if (!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new JsonSyntaxException("%s has to be a JSON object!".formatted(name));
                }

                if (!jsonObject.has("type")) {
                    throw new JsonSyntaxException("%s JSON requires a \"type\" identifier!".formatted(name));
                }

                Identifier factoryId = new Identifier(JsonHelper.getString(jsonObject, "type"));
                Optional<ConditionFactory<T>> factory = registry.getOrEmpty(factoryId);

                if (factory.isEmpty() && IdentifierAlias.hasAlias(factoryId)) {
                    factory = registry.getOrEmpty(IdentifierAlias.resolveAlias(factoryId, IdentifierAlias.Priority.NAMESPACE));
                }

                return factory
                    .orElseThrow(() -> new JsonSyntaxException("%s type \"%s\" is not registered.".formatted(name, factoryId)))
                    .read(jsonObject);

            },
            ConditionFactory.Instance::toJson
        );
    }

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(Class<ConditionFactory<T>.Instance> dataClass, ConditionType<T> conditionType) {
        return new SerializableDataType<>(dataClass, conditionType::write, conditionType::read, conditionType::read);
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(Registry<ActionFactory<T>> registry, String name) {
        return new SerializableDataType<>(
            ClassUtil.castClass(ActionFactory.Instance.class),
            (buffer, instance) -> instance.write(buffer),
            buffer -> {

                Identifier factoryId = buffer.readIdentifier();
                Optional<ActionFactory<T>> factory = registry.getOrEmpty(factoryId);

                if (factory.isEmpty() && IdentifierAlias.hasAlias(factoryId)) {
                    factory = registry.getOrEmpty(IdentifierAlias.resolveAlias(factoryId, IdentifierAlias.Priority.NAMESPACE));
                }

                return factory
                    .orElseThrow(() -> new JsonSyntaxException("%s type \"%s\" was not registered.".formatted(name, factoryId)))
                    .read(buffer);

            },
            jsonElement -> {

                if (!(jsonElement instanceof JsonObject jsonObject)) {
                    throw new JsonSyntaxException("%s has to be a JSON object!".formatted(name));
                }

                if (!jsonObject.has("type")) {
                    throw new JsonSyntaxException("%s JSON requires a \"type\" identifier!".formatted(name));
                }

                Identifier factoryId = new Identifier(JsonHelper.getString(jsonObject, "type"));
                Optional<ActionFactory<T>> factory = registry.getOrEmpty(factoryId);

                if (factory.isEmpty() && IdentifierAlias.hasAlias(factoryId)) {
                    factory = registry.getOrEmpty(IdentifierAlias.resolveAlias(factoryId, IdentifierAlias.Priority.NAMESPACE));
                }

                return factory
                    .orElseThrow(() -> new JsonSyntaxException("%s type \"%s\" is not registered.".formatted(name, factoryId)))
                    .read(jsonObject);

            },
            ActionFactory.Instance::toJson
        );
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(Class<ActionFactory<T>.Instance> dataClass, ActionType<T> actionType) {
        return new SerializableDataType<>(dataClass, actionType::write, actionType::read, actionType::read);
    }

}
