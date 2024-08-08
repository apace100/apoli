package io.github.apace100.apoli.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.power.factory.PowerTypes;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.power.factory.action.*;
import io.github.apace100.apoli.power.factory.condition.*;
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

    public static final SerializableDataType<ConditionFactory<Entity>.Instance> ENTITY_CONDITION = condition(ApoliRegistries.ENTITY_CONDITION, EntityConditions.ALIASES, "Entity condition type");

    public static final SerializableDataType<List<ConditionFactory<Entity>.Instance>> ENTITY_CONDITIONS = ENTITY_CONDITION.listOf();

    public static final SerializableDataType<ConditionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_CONDITION = condition(ApoliRegistries.BIENTITY_CONDITION, BiEntityConditions.ALIASES, "Bi-entity condition type");

    public static final SerializableDataType<List<ConditionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_CONDITIONS = BIENTITY_CONDITION.listOf();

    public static final SerializableDataType<ConditionFactory<Pair<World, ItemStack>>.Instance> ITEM_CONDITION = condition(ApoliRegistries.ITEM_CONDITION, ItemConditions.ALIASES, "Item condition type");

    public static final SerializableDataType<List<ConditionFactory<Pair<World, ItemStack>>.Instance>> ITEM_CONDITIONS = ITEM_CONDITION.listOf();

    public static final SerializableDataType<ConditionFactory<CachedBlockPosition>.Instance> BLOCK_CONDITION = condition(ApoliRegistries.BLOCK_CONDITION, BlockConditions.ALIASES, "Block condition type");

    public static final SerializableDataType<List<ConditionFactory<CachedBlockPosition>.Instance>> BLOCK_CONDITIONS = BLOCK_CONDITION.listOf();

    public static final SerializableDataType<ConditionFactory<FluidState>.Instance> FLUID_CONDITION = condition(ApoliRegistries.FLUID_CONDITION, FluidConditions.ALIASES, "Fluid condition type");

    public static final SerializableDataType<List<ConditionFactory<FluidState>.Instance>> FLUID_CONDITIONS = FLUID_CONDITION.listOf();

    public static final SerializableDataType<ConditionFactory<Pair<DamageSource, Float>>.Instance> DAMAGE_CONDITION = condition(ApoliRegistries.DAMAGE_CONDITION, DamageConditions.ALIASES, "Damage condition type");

    public static final SerializableDataType<List<ConditionFactory<Pair<DamageSource, Float>>.Instance>> DAMAGE_CONDITIONS = DAMAGE_CONDITION.listOf();

    public static final SerializableDataType<ConditionFactory<RegistryEntry<Biome>>.Instance> BIOME_CONDITION = condition(ApoliRegistries.BIOME_CONDITION, BiomeConditions.ALIASES, "Biome condition type");

    public static final SerializableDataType<List<ConditionFactory<RegistryEntry<Biome>>.Instance>> BIOME_CONDITIONS = BIOME_CONDITION.listOf();

    public static final SerializableDataType<ActionFactory<Entity>.Instance> ENTITY_ACTION = action(ApoliRegistries.ENTITY_ACTION, EntityActions.ALIASES, "Entity action type");

    public static final SerializableDataType<List<ActionFactory<Entity>.Instance>> ENTITY_ACTIONS = ENTITY_ACTION.listOf();

    public static final SerializableDataType<ActionFactory<Pair<Entity, Entity>>.Instance> BIENTITY_ACTION = action(ApoliRegistries.BIENTITY_ACTION, BiEntityActions.ALIASES, "Bi-entity action type");

    public static final SerializableDataType<List<ActionFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_ACTIONS = BIENTITY_ACTION.listOf();

    public static final SerializableDataType<ActionFactory<Triple<World, BlockPos, Direction>>.Instance> BLOCK_ACTION = action(ApoliRegistries.BLOCK_ACTION, BlockActions.ALIASES, "Block action type");

    public static final SerializableDataType<List<ActionFactory<Triple<World, BlockPos, Direction>>.Instance>> BLOCK_ACTIONS = BLOCK_ACTION.listOf();

    public static final SerializableDataType<ActionFactory<Pair<World, StackReference>>.Instance> ITEM_ACTION = action(ApoliRegistries.ITEM_ACTION, ItemActions.ALIASES, "Item action type");

    public static final SerializableDataType<List<ActionFactory<Pair<World, StackReference>>.Instance>> ITEM_ACTIONS = ITEM_ACTION.listOf();

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

    @Deprecated(forRemoval = true)
    public static final SerializableDataType<Explosion.DestructionType> BACKWARDS_COMPATIBLE_DESTRUCTION_TYPE = SerializableDataType.enumValue(Explosion.DestructionType.class, () -> ImmutableMap.of(
        "none", Explosion.DestructionType.KEEP,
        "break", Explosion.DestructionType.DESTROY,
        "destroy", Explosion.DestructionType.DESTROY_WITH_DECAY,
        "trigger", Explosion.DestructionType.TRIGGER_BLOCK
    ));

    public static final SerializableDataType<Explosion.DestructionType> DESTRUCTION_TYPE = SerializableDataType.enumValue(Explosion.DestructionType.class);

    public static final SerializableDataType<ArgumentWrapper<EntitySelector>> ENTITIES_SELECTOR = SerializableDataType.argumentType(EntityArgumentType.entities());

    @Deprecated(forRemoval = true)
    public static final SerializableDataType<DamageSourceDescription> DAMAGE_SOURCE_DESCRIPTION = SerializableDataType.compound(
        DamageSourceDescription.DATA,
        DamageSourceDescription::fromData,
        DamageSourceDescription::toData
    );

    @Deprecated(forRemoval = true)
    public static final SerializableDataType<LegacyMaterial> LEGACY_MATERIAL = SerializableDataTypes.STRING.xmap(LegacyMaterial::new, LegacyMaterial::getMaterial);

    @Deprecated(forRemoval = true)
    public static final SerializableDataType<List<LegacyMaterial>> LEGACY_MATERIALS = LEGACY_MATERIAL.listOf();

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

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(Registry<ConditionFactory<T>> registry, String name) {
        return condition(registry, null, name);
    }

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(Registry<ConditionFactory<T>> registry, IdentifierAlias aliases, String name) {
        return condition("type", registry, aliases, (conditionFactories, id) -> new IllegalArgumentException(name + " \"" + id + "\" is not registered"));
    }

    public static <T> SerializableDataType<ConditionFactory<T>.Instance> condition(String field, Registry<ConditionFactory<T>> registry, @Nullable IdentifierAlias aliases, BiFunction<Registry<ConditionFactory<T>>, Identifier, RuntimeException> errorHandler) {
        return SerializableDataType.of(
            new StrictCodec<>() {

                @Override
                public <I> com.mojang.datafixers.util.Pair<ConditionFactory<T>.Instance, I> strictDecode(DynamicOps<I> ops, I input) {

                    MapLike<I> mapInput = ops.getMap(input).getOrThrow();
                    Identifier factoryId = Optional.ofNullable(mapInput.get(field))
                        .map(type -> SerializableDataTypes.IDENTIFIER.strictParse(ops, type))
                        .orElseThrow(() -> new DataException(DataException.Phase.READING, field, "Field is required, but is missing!"));

                    ConditionFactory<T> factory = registry
                        .getOrEmpty(aliases == null ? factoryId : aliases.resolveAlias(factoryId, registry::containsId))
                        .orElseThrow(() -> errorHandler.apply(registry, factoryId));

                    SerializableData.Instance data = factory.getSerializableData().strictDecode(ops, mapInput);
                    return com.mojang.datafixers.util.Pair.of(factory.fromData(data), input);

                }

                @Override
                public <I> I strictEncode(ConditionFactory<T>.Instance input, DynamicOps<I> ops, I prefix) {

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
                public ConditionFactory<T>.Instance decode(RegistryByteBuf buf) {
                    Identifier factoryId = buf.readIdentifier();
                    return registry.getOrEmpty(factoryId)
                        .orElseThrow(() -> errorHandler.apply(registry, factoryId))
                        .receive(buf);
                }

                @Override
                public void encode(RegistryByteBuf buf, ConditionFactory<T>.Instance instance) {
                    instance.send(buf);
                }

            }
        );
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(Registry<ActionFactory<T>> registry, String name) {
        return action(registry, null, name);
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(Registry<ActionFactory<T>> registry, IdentifierAlias aliases, String name) {
        return action("type", registry, aliases, (factories, id) -> new IllegalArgumentException(name + " \"" + id + "\" is not registered"));
    }

    public static <T> SerializableDataType<ActionFactory<T>.Instance> action(String field, Registry<ActionFactory<T>> registry, @Nullable IdentifierAlias aliases, BiFunction<Registry<ActionFactory<T>>, Identifier, RuntimeException> errorHandler) {
        return SerializableDataType.of(
            new StrictCodec<>() {

                @Override
                public <I> com.mojang.datafixers.util.Pair<ActionFactory<T>.Instance, I> strictDecode(DynamicOps<I> ops, I input) {

                    MapLike<I> mapInput = ops.getMap(input).getOrThrow();
                    Identifier factoryId = Optional.ofNullable(mapInput.get(field))
                        .map(id -> SerializableDataTypes.IDENTIFIER.strictParse(ops, id))
                        .orElseThrow(() -> new DataException(DataException.Phase.READING, field, "Field is required, but is missing!"));

                    ActionFactory<T> factory = registry
                        .getOrEmpty(aliases == null ? factoryId : aliases.resolveAlias(factoryId, registry::containsId))
                        .orElseThrow(() -> errorHandler.apply(registry, factoryId));

                    SerializableData.Instance data = factory.getSerializableData().strictDecode(ops, mapInput);
                    return com.mojang.datafixers.util.Pair.of(factory.fromData(data), input);

                }

                @Override
                public <I> I strictEncode(ActionFactory<T>.Instance input, DynamicOps<I> ops, I prefix) {

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
                public ActionFactory<T>.Instance decode(RegistryByteBuf buf) {
                    Identifier factoryId = buf.readIdentifier();
                    return registry.getOrEmpty(factoryId)
                        .orElseThrow(() -> errorHandler.apply(registry, factoryId))
                        .receive(buf);
                }

                @Override
                public void encode(RegistryByteBuf buf, ActionFactory<T>.Instance instance) {
                    instance.send(buf);
                }

            }
        );
    }

    public static void init() {

    }

}
