package io.github.apace100.apoli.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.*;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.action.type.BiEntityActionTypes;
import io.github.apace100.apoli.action.type.BlockActionTypes;
import io.github.apace100.apoli.action.type.EntityActionTypes;
import io.github.apace100.apoli.action.type.ItemActionTypes;
import io.github.apace100.apoli.action.factory.*;
import io.github.apace100.apoli.condition.factory.*;
import io.github.apace100.apoli.condition.type.*;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.type.PowerTypes;
import io.github.apace100.apoli.power.type.Active;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.*;
import io.github.apace100.calio.SerializationHelper;
import io.github.apace100.calio.codec.StrictCodec;
import io.github.apace100.calio.data.DataException;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.ArgumentWrapper;
import io.github.apace100.calio.util.IdentifierAlias;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.AdvancementCommand;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ApoliDataTypes {

    public static final SerializableDataType<PowerReference> POWER_REFERENCE = SerializableDataTypes.IDENTIFIER.xmap(PowerReference::new, Power::getId);

    public static final SerializableDataType<PowerTypeFactory<? extends PowerType>> POWER_TYPE_FACTORY = SerializableDataType.lazy(() -> SerializableDataType.registry(
        ApoliRegistries.POWER_FACTORY,
        Apoli.MODID,
        PowerTypes.ALIASES,
        (registry, id) -> new IllegalArgumentException("Power type \"" + id + "\" is not registered")
    ));

    public static final SerializableDataType<ConditionTypeFactory<Entity>.Instance> ENTITY_CONDITION = condition(ApoliRegistries.ENTITY_CONDITION, EntityConditionTypes.ALIASES, "Entity condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<Entity>.Instance>> ENTITY_CONDITIONS = ENTITY_CONDITION.listOf();

    public static final SerializableDataType<ConditionTypeFactory<Pair<Entity, Entity>>.Instance> BIENTITY_CONDITION = condition(ApoliRegistries.BIENTITY_CONDITION, BiEntityConditionTypes.ALIASES, "Bi-entity condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_CONDITIONS = BIENTITY_CONDITION.listOf();

    public static final SerializableDataType<ConditionTypeFactory<Pair<World, ItemStack>>.Instance> ITEM_CONDITION = condition(ApoliRegistries.ITEM_CONDITION, ItemConditionTypes.ALIASES, "Item condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<Pair<World, ItemStack>>.Instance>> ITEM_CONDITIONS = ITEM_CONDITION.listOf();

    public static final SerializableDataType<ConditionTypeFactory<CachedBlockPosition>.Instance> BLOCK_CONDITION = condition(ApoliRegistries.BLOCK_CONDITION, BlockConditionTypes.ALIASES, "Block condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<CachedBlockPosition>.Instance>> BLOCK_CONDITIONS = BLOCK_CONDITION.listOf();

    public static final SerializableDataType<ConditionTypeFactory<FluidState>.Instance> FLUID_CONDITION = condition(ApoliRegistries.FLUID_CONDITION, FluidConditionTypes.ALIASES, "Fluid condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<FluidState>.Instance>> FLUID_CONDITIONS = FLUID_CONDITION.listOf();

    public static final SerializableDataType<ConditionTypeFactory<Pair<DamageSource, Float>>.Instance> DAMAGE_CONDITION = SerializableDataType.lazy(() -> condition(ApoliRegistries.DAMAGE_CONDITION, DamageConditionTypes.ALIASES, "Damage condition type"));

    public static final SerializableDataType<List<ConditionTypeFactory<Pair<DamageSource, Float>>.Instance>> DAMAGE_CONDITIONS = SerializableDataType.lazy(DAMAGE_CONDITION::listOf);

    public static final SerializableDataType<ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>>.Instance> BIOME_CONDITION = condition(ApoliRegistries.BIOME_CONDITION, BiomeConditionTypes.ALIASES, "Biome condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>>.Instance>> BIOME_CONDITIONS = BIOME_CONDITION.listOf();

    public static final SerializableDataType<ActionTypeFactory<Entity>.Instance> ENTITY_ACTION = action(ApoliRegistries.ENTITY_ACTION, EntityActionTypes.ALIASES, "Entity action type");

    public static final SerializableDataType<List<ActionTypeFactory<Entity>.Instance>> ENTITY_ACTIONS = ENTITY_ACTION.listOf();

    public static final SerializableDataType<ActionTypeFactory<Pair<Entity, Entity>>.Instance> BIENTITY_ACTION = action(ApoliRegistries.BIENTITY_ACTION, BiEntityActionTypes.ALIASES, "Bi-entity action type");

    public static final SerializableDataType<List<ActionTypeFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_ACTIONS = BIENTITY_ACTION.listOf();

    public static final SerializableDataType<ActionTypeFactory<Triple<World, BlockPos, Direction>>.Instance> BLOCK_ACTION = action(ApoliRegistries.BLOCK_ACTION, BlockActionTypes.ALIASES, "Block action type");

    public static final SerializableDataType<List<ActionTypeFactory<Triple<World, BlockPos, Direction>>.Instance>> BLOCK_ACTIONS = BLOCK_ACTION.listOf();

    public static final SerializableDataType<ActionTypeFactory<Pair<World, StackReference>>.Instance> ITEM_ACTION = action(ApoliRegistries.ITEM_ACTION, ItemActionTypes.ALIASES, "Item action type");

    public static final SerializableDataType<List<ActionTypeFactory<Pair<World, StackReference>>.Instance>> ITEM_ACTIONS = ITEM_ACTION.listOf();

    public static final SerializableDataType<Space> SPACE = SerializableDataType.enumValue(Space.class);

    public static final SerializableDataType<ResourceOperation> RESOURCE_OPERATION = SerializableDataType.enumValue(ResourceOperation.class);

    public static final SerializableDataType<InventoryUtil.InventoryType> INVENTORY_TYPE = SerializableDataType.enumValue(InventoryUtil.InventoryType.class);

    public static final SerializableDataType<EnumSet<InventoryUtil.InventoryType>> INVENTORY_TYPE_SET = SerializableDataType.enumSet(INVENTORY_TYPE);

    public static final SerializableDataType<InventoryUtil.ProcessMode> PROCESS_MODE = SerializableDataType.enumValue(InventoryUtil.ProcessMode.class);

    public static final SerializableDataType<AttributedEntityAttributeModifier> ATTRIBUTED_ATTRIBUTE_MODIFIER = SerializableDataType.compound(
        SerializableDataTypes.ATTRIBUTE_MODIFIER.serializableData().copy()
            .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY),
        (data, ops) -> new AttributedEntityAttributeModifier(
            data.get("attribute"),
            SerializableDataTypes.ATTRIBUTE_MODIFIER.fromData(data, ops)
        ),
        (attributedAttributeModifier, data) -> SerializableDataTypes.ATTRIBUTE_MODIFIER
            .writeTo(attributedAttributeModifier.modifier(), data)
            .set("attribute", attributedAttributeModifier.attribute())
    );

    public static final SerializableDataType<List<AttributedEntityAttributeModifier>> ATTRIBUTED_ATTRIBUTE_MODIFIERS = ATTRIBUTED_ATTRIBUTE_MODIFIER.listOf();

    public static final SerializableDataType<Pair<Integer, ItemStack>> POSITIONED_ITEM_STACK = SerializableDataType.compound(
        SerializableDataTypes.ITEM_STACK.serializableData().copy()
            .add("slot", SerializableDataTypes.INT, Integer.MIN_VALUE),
        (data, ops) -> new Pair<>(
            data.getInt("slot"),
            SerializableDataTypes.ITEM_STACK.fromData(data, ops)
        ),
        (positionedStack, data) -> SerializableDataTypes.ITEM_STACK
            .writeTo(positionedStack.getRight(), data)
            .set("slot", positionedStack.getLeft())
    );

    public static final SerializableDataType<List<Pair<Integer, ItemStack>>> POSITIONED_ITEM_STACKS = POSITIONED_ITEM_STACK.listOf();

    public static final SerializableDataType<Active.Key> KEY = SerializableDataType.compound(
        new SerializableData()
            .add("key", SerializableDataTypes.STRING)
            .add("continuous", SerializableDataTypes.BOOLEAN, false),
        data -> {

            Active.Key key = new Active.Key();

            key.key = data.getString("key");
            key.continuous = data.getBoolean("continuous");

            return key;

        },
        (key, data) -> data
            .set("key", key.key)
            .set("continuous", key.continuous)
    );

    public static final SerializableDataType<Active.Key> BACKWARDS_COMPATIBLE_KEY = SerializableDataType.of(
        new StrictCodec<>() {

            @Override
            public <T> com.mojang.datafixers.util.Pair<Active.Key, T> strictDecode(DynamicOps<T> ops, T input) {

                DataResult<String> inputString = ops.getStringValue(input);
                if (inputString.result().isPresent()) {

                    Active.Key key = new Active.Key();

                    key.key = inputString.getOrThrow();
                    key.continuous = false;

                    return com.mojang.datafixers.util.Pair.of(key, input);

                }

                else {
                    return KEY.strictDecode(ops, input);
                }

            }

            @Override
            public <T> T strictEncode(Active.Key input, DynamicOps<T> ops, T prefix) {
                return KEY.strictEncode(input, ops, prefix);
            }

        },
        KEY.packetCodec()
    );

    /**
     *  <p>A HUD render data type that accepts either a single HUD render or multiple HUD renders. The first HUD render will be considered
     *  the <b>"parent"</b> and the following HUD renders will be considered its <b>"children."</b></p>
     *
     *  <p>If the children don't specify an order value, the order value of the parent will be inherited instead.</p>
     */
    public static final SerializableDataType<HudRender> HUD_RENDER = HudRender.DATA_TYPE;

    public static final SerializableDataType<Comparison> COMPARISON = SerializableDataType.enumValue(Comparison.class, SerializationHelper.buildEnumMap(Comparison.class, Comparison::getComparisonString));

    public static final SerializableDataType<PlayerAbility> PLAYER_ABILITY = SerializableDataTypes.IDENTIFIER.xmap(id -> Pal.provideRegisteredAbility(id).get(), PlayerAbility::getId);

    public static final SerializableDataType<ArgumentWrapper<Integer>> ITEM_SLOT = SerializableDataType.argumentType(ItemSlotArgumentType.itemSlot());

    public static final SerializableDataType<List<ArgumentWrapper<Integer>>> ITEM_SLOTS = ITEM_SLOT.listOf();

    public static final SerializableDataType<Explosion.DestructionType> DESTRUCTION_TYPE = SerializableDataType.enumValue(Explosion.DestructionType.class);

    public static final SerializableDataType<ArgumentWrapper<EntitySelector>> ENTITIES_SELECTOR = SerializableDataType.argumentType(EntityArgumentType.entities());

    public static final SerializableDataType<AdvancementCommand.Operation> ADVANCEMENT_OPERATION = SerializableDataType.enumValue(AdvancementCommand.Operation.class);

    public static final SerializableDataType<AdvancementCommand.Selection> ADVANCEMENT_SELECTION = SerializableDataType.enumValue(AdvancementCommand.Selection.class);

    public static final SerializableDataType<ClickType> CLICK_TYPE = SerializableDataType.enumValue(ClickType.class, () -> ImmutableMap.of(
        "left", ClickType.LEFT,
        "right", ClickType.RIGHT
    ));

    public static final SerializableDataType<EnumSet<ClickType>> CLICK_TYPE_SET = SerializableDataType.enumSet(CLICK_TYPE);

    public static final SerializableDataType<TextAlignment> TEXT_ALIGNMENT = SerializableDataType.enumValue(TextAlignment.class);

    public static final SerializableDataType<Map<Identifier, Identifier>> IDENTIFIER_MAP = SerializableDataType.map(SerializableDataTypes.IDENTIFIER, SerializableDataTypes.IDENTIFIER);

    public static final SerializableDataType<Pattern> REGEX = SerializableDataTypes.STRING.xmap(Pattern::compile, Pattern::pattern);

    public static final SerializableDataType<Map<Pattern, Identifier>> REGEX_MAP = SerializableDataType.map(REGEX, SerializableDataTypes.IDENTIFIER);

    public static final SerializableDataType<GameMode> GAME_MODE = SerializableDataType.enumValue(GameMode.class);

    //  This is for keeping backwards compatibility to fields that used to accept strings as translation keys
    public static final SerializableDataType<Text> DEFAULT_TRANSLATABLE_TEXT = SerializableDataType.of(TextCodecs.CODEC, TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC);
  
    public static final SerializableDataType<StackClickPhase> STACK_CLICK_PHASE = SerializableDataType.enumValue(StackClickPhase.class);

    public static final SerializableDataType<EnumSet<StackClickPhase>> STACK_CLICK_PHASE_SET = SerializableDataType.enumSet(STACK_CLICK_PHASE);
  
    public static final SerializableDataType<BlockUsagePhase> BLOCK_USAGE_PHASE = SerializableDataType.enumValue(BlockUsagePhase.class);

    public static final SerializableDataType<EnumSet<BlockUsagePhase>> BLOCK_USAGE_PHASE_SET = SerializableDataType.enumSet(BLOCK_USAGE_PHASE);

    public static final SerializableDataType<EntityPose> ENTITY_POSE = SerializableDataType.enumValue(EntityPose.class);

    public static final SerializableDataType<ArmPoseReference> ARM_POSE_REFERENCE = SerializableDataType.enumValue(ArmPoseReference.class);

    public static <T> SerializableDataType<ConditionTypeFactory<T>.Instance> condition(Registry<ConditionTypeFactory<T>> registry, String name) {
        return condition(registry, null, name);
    }

    public static <T> SerializableDataType<ConditionTypeFactory<T>.Instance> condition(Registry<ConditionTypeFactory<T>> registry, IdentifierAlias aliases, String name) {
        return condition("type", registry, aliases, (conditionFactories, id) -> new IllegalArgumentException(name + " \"" + id + "\" is not registered"));
    }

    public static <T> SerializableDataType<ConditionTypeFactory<T>.Instance> condition(String field, Registry<ConditionTypeFactory<T>> registry, @Nullable IdentifierAlias aliases, BiFunction<Registry<ConditionTypeFactory<T>>, Identifier, RuntimeException> errorHandler) {
        return SerializableDataType.of(
            new StrictCodec<>() {

                @Override
                public <I> com.mojang.datafixers.util.Pair<ConditionTypeFactory<T>.Instance, I> strictDecode(DynamicOps<I> ops, I input) {

                    MapLike<I> mapInput = ops.getMap(input).getOrThrow();
                    Identifier factoryId = Optional.ofNullable(mapInput.get(field))
                        .map(type -> SerializableDataTypes.IDENTIFIER.strictParse(ops, type))
                        .orElseThrow(() -> new DataException(DataException.Phase.READING, field, "Field is required, but is missing!"));

                    ConditionTypeFactory<T> factory = registry
                        .getOrEmpty(aliases == null ? factoryId : aliases.resolveAlias(factoryId, registry::containsId))
                        .orElseThrow(() -> errorHandler.apply(registry, factoryId));

                    SerializableData.Instance data = factory.getSerializableData().strictDecode(ops, mapInput);
                    return com.mojang.datafixers.util.Pair.of(factory.fromData(data), input);

                }

                @Override
                public <I> I strictEncode(ConditionTypeFactory<T>.Instance input, DynamicOps<I> ops, I prefix) {

                    Map<I, I> output = new LinkedHashMap<>();

                    output.put(ops.createString(field), ops.createString(input.getSerializerId().toString()));
                    ops.getMapEntries(input.getSerializableData().codec().strictEncodeStart(ops, input.getData()))
                        .getOrThrow()
                        .accept(output::put);

                    return ops.createMap(output);

                }

            },
            new PacketCodec<>() {

                @Override
                public ConditionTypeFactory<T>.Instance decode(RegistryByteBuf buf) {
                    Identifier factoryId = buf.readIdentifier();
                    return registry.getOrEmpty(factoryId)
                        .orElseThrow(() -> errorHandler.apply(registry, factoryId))
                        .receive(buf);
                }

                @Override
                public void encode(RegistryByteBuf buf, ConditionTypeFactory<T>.Instance instance) {
                    instance.send(buf);
                }

            }
        );
    }

    public static <T> SerializableDataType<ActionTypeFactory<T>.Instance> action(Registry<ActionTypeFactory<T>> registry, String name) {
        return action(registry, null, name);
    }

    public static <T> SerializableDataType<ActionTypeFactory<T>.Instance> action(Registry<ActionTypeFactory<T>> registry, IdentifierAlias aliases, String name) {
        return action("type", registry, aliases, (factories, id) -> new IllegalArgumentException(name + " \"" + id + "\" is not registered"));
    }

    public static <T> SerializableDataType<ActionTypeFactory<T>.Instance> action(String field, Registry<ActionTypeFactory<T>> registry, @Nullable IdentifierAlias aliases, BiFunction<Registry<ActionTypeFactory<T>>, Identifier, RuntimeException> errorHandler) {
        return SerializableDataType.of(
            new StrictCodec<>() {

                @Override
                public <I> com.mojang.datafixers.util.Pair<ActionTypeFactory<T>.Instance, I> strictDecode(DynamicOps<I> ops, I input) {

                    MapLike<I> mapInput = ops.getMap(input).getOrThrow();
                    Identifier factoryId = Optional.ofNullable(mapInput.get(field))
                        .map(id -> SerializableDataTypes.IDENTIFIER.strictParse(ops, id))
                        .orElseThrow(() -> new DataException(DataException.Phase.READING, field, "Field is required, but is missing!"));

                    ActionTypeFactory<T> factory = registry
                        .getOrEmpty(aliases == null ? factoryId : aliases.resolveAlias(factoryId, registry::containsId))
                        .orElseThrow(() -> errorHandler.apply(registry, factoryId));

                    SerializableData.Instance data = factory.getSerializableData().strictDecode(ops, mapInput);
                    return com.mojang.datafixers.util.Pair.of(factory.fromData(data), input);

                }

                @Override
                public <I> I strictEncode(ActionTypeFactory<T>.Instance input, DynamicOps<I> ops, I prefix) {

                    Map<I, I> output = new LinkedHashMap<>();

                    output.put(ops.createString(field), ops.createString(input.getSerializerId().toString()));
                    ops.getMapEntries(input.getSerializableData().codec().strictEncodeStart(ops, input.getData()))
                        .getOrThrow()
                        .accept(output::put);

                    return ops.createMap(output);

                }

            },
            new PacketCodec<>() {

                @Override
                public ActionTypeFactory<T>.Instance decode(RegistryByteBuf buf) {
                    Identifier factoryId = buf.readIdentifier();
                    return registry.getOrEmpty(factoryId)
                        .orElseThrow(() -> errorHandler.apply(registry, factoryId))
                        .receive(buf);
                }

                @Override
                public void encode(RegistryByteBuf buf, ActionTypeFactory<T>.Instance instance) {
                    instance.send(buf);
                }

            }
        );
    }

    public static void init() {

    }

}
