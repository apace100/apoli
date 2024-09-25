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
import io.github.apace100.calio.data.*;
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
import net.minecraft.recipe.*;
import net.minecraft.registry.Registries;
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
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ApoliDataTypes {

    public static final SerializableDataType<PowerReference> POWER_REFERENCE = SerializableDataTypes.IDENTIFIER.xmap(PowerReference::new, Power::getId);

    public static final SerializableDataType<PowerTypeFactory<? extends PowerType>> POWER_TYPE_FACTORY = SerializableDataType.lazy(() -> SerializableDataType.registry(ApoliRegistries.POWER_FACTORY, Apoli.MODID, PowerTypes.ALIASES, (registry, id) -> "Power type \"" + id + "\" is not registered"));

    public static final SerializableDataType<ConditionTypeFactory<Entity>.Instance> ENTITY_CONDITION = condition(ApoliRegistries.ENTITY_CONDITION, EntityConditionTypes.ALIASES, "Entity condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<Entity>.Instance>> ENTITY_CONDITIONS = ENTITY_CONDITION.list();

    public static final SerializableDataType<ConditionTypeFactory<Pair<Entity, Entity>>.Instance> BIENTITY_CONDITION = condition(ApoliRegistries.BIENTITY_CONDITION, BiEntityConditionTypes.ALIASES, "Bi-entity condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_CONDITIONS = BIENTITY_CONDITION.list();

    public static final SerializableDataType<ConditionTypeFactory<Pair<World, ItemStack>>.Instance> ITEM_CONDITION = condition(ApoliRegistries.ITEM_CONDITION, ItemConditionTypes.ALIASES, "Item condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<Pair<World, ItemStack>>.Instance>> ITEM_CONDITIONS = ITEM_CONDITION.list();

    public static final SerializableDataType<ConditionTypeFactory<CachedBlockPosition>.Instance> BLOCK_CONDITION = condition(ApoliRegistries.BLOCK_CONDITION, BlockConditionTypes.ALIASES, "Block condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<CachedBlockPosition>.Instance>> BLOCK_CONDITIONS = BLOCK_CONDITION.list();

    public static final SerializableDataType<ConditionTypeFactory<FluidState>.Instance> FLUID_CONDITION = condition(ApoliRegistries.FLUID_CONDITION, FluidConditionTypes.ALIASES, "Fluid condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<FluidState>.Instance>> FLUID_CONDITIONS = FLUID_CONDITION.list();

    public static final SerializableDataType<ConditionTypeFactory<Pair<DamageSource, Float>>.Instance> DAMAGE_CONDITION = SerializableDataType.lazy(() -> condition(ApoliRegistries.DAMAGE_CONDITION, DamageConditionTypes.ALIASES, "Damage condition type"));

    public static final SerializableDataType<List<ConditionTypeFactory<Pair<DamageSource, Float>>.Instance>> DAMAGE_CONDITIONS = SerializableDataType.lazy(DAMAGE_CONDITION::list);

    public static final SerializableDataType<ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>>.Instance> BIOME_CONDITION = condition(ApoliRegistries.BIOME_CONDITION, BiomeConditionTypes.ALIASES, "Biome condition type");

    public static final SerializableDataType<List<ConditionTypeFactory<Pair<BlockPos, RegistryEntry<Biome>>>.Instance>> BIOME_CONDITIONS = BIOME_CONDITION.list();

    public static final SerializableDataType<ActionTypeFactory<Entity>.Instance> ENTITY_ACTION = action(ApoliRegistries.ENTITY_ACTION, EntityActionTypes.ALIASES, "Entity action type");

    public static final SerializableDataType<List<ActionTypeFactory<Entity>.Instance>> ENTITY_ACTIONS = ENTITY_ACTION.list();

    public static final SerializableDataType<ActionTypeFactory<Pair<Entity, Entity>>.Instance> BIENTITY_ACTION = action(ApoliRegistries.BIENTITY_ACTION, BiEntityActionTypes.ALIASES, "Bi-entity action type");

    public static final SerializableDataType<List<ActionTypeFactory<Pair<Entity, Entity>>.Instance>> BIENTITY_ACTIONS = BIENTITY_ACTION.list();

    public static final SerializableDataType<ActionTypeFactory<Triple<World, BlockPos, Direction>>.Instance> BLOCK_ACTION = action(ApoliRegistries.BLOCK_ACTION, BlockActionTypes.ALIASES, "Block action type");

    public static final SerializableDataType<List<ActionTypeFactory<Triple<World, BlockPos, Direction>>.Instance>> BLOCK_ACTIONS = BLOCK_ACTION.list();

    public static final SerializableDataType<ActionTypeFactory<Pair<World, StackReference>>.Instance> ITEM_ACTION = action(ApoliRegistries.ITEM_ACTION, ItemActionTypes.ALIASES, "Item action type");

    public static final SerializableDataType<List<ActionTypeFactory<Pair<World, StackReference>>.Instance>> ITEM_ACTIONS = ITEM_ACTION.list();

    public static final SerializableDataType<Space> SPACE = SerializableDataType.enumValue(Space.class);

    public static final SerializableDataType<ResourceOperation> RESOURCE_OPERATION = SerializableDataType.enumValue(ResourceOperation.class);

    public static final SerializableDataType<InventoryUtil.InventoryType> INVENTORY_TYPE = SerializableDataType.enumValue(InventoryUtil.InventoryType.class);

    public static final SerializableDataType<EnumSet<InventoryUtil.InventoryType>> INVENTORY_TYPE_SET = SerializableDataType.enumSet(INVENTORY_TYPE);

    public static final SerializableDataType<InventoryUtil.ProcessMode> PROCESS_MODE = SerializableDataType.enumValue(InventoryUtil.ProcessMode.class);

    public static final SerializableDataType<AttributedEntityAttributeModifier> ATTRIBUTED_ATTRIBUTE_MODIFIER = SerializableDataType.compound(
        SerializableDataTypes.ATTRIBUTE_MODIFIER.serializableData().copy()
            .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY),
        data -> new AttributedEntityAttributeModifier(
            data.get("attribute"),
            SerializableDataTypes.ATTRIBUTE_MODIFIER_OBJ_FACTORY.fromData(data)
        ),
        (attributedAttributeModifier, serializableData) -> SerializableDataTypes.ATTRIBUTE_MODIFIER_OBJ_FACTORY
            .toData(attributedAttributeModifier.modifier(), serializableData)
            .set("attribute", attributedAttributeModifier.attribute())
    );

    public static final SerializableDataType<List<AttributedEntityAttributeModifier>> ATTRIBUTED_ATTRIBUTE_MODIFIERS = ATTRIBUTED_ATTRIBUTE_MODIFIER.list();

    public static final SerializableDataType<Pair<Integer, ItemStack>> POSITIONED_ITEM_STACK = SerializableDataType.compound(
        SerializableDataTypes.ITEM_STACK.serializableData().copy()
            .add("slot", SerializableDataTypes.INT, Integer.MIN_VALUE),
        data -> new Pair<>(
            data.getInt("slot"),
            SerializableDataTypes.ITEM_STACK_OBJ_FACTORY.fromData(data)
        ),
        (positionedStack, serializableData) -> SerializableDataTypes.ITEM_STACK_OBJ_FACTORY
            .toData(positionedStack.getRight(), serializableData)
            .set("slot", positionedStack.getLeft())
    );

    public static final SerializableDataType<List<Pair<Integer, ItemStack>>> POSITIONED_ITEM_STACKS = POSITIONED_ITEM_STACK.list();

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
        (key, serializableData) -> serializableData.instance()
            .set("key", key.key)
            .set("continuous", key.continuous)
    );

    public static final SerializableDataType<Active.Key> BACKWARDS_COMPATIBLE_KEY = SerializableDataType.of(
        new Codec<>() {

            @Override
            public <T> DataResult<com.mojang.datafixers.util.Pair<Active.Key, T>> decode(DynamicOps<T> ops, T input) {

                DataResult<String> stringInput = ops.getStringValue(input);
                if (stringInput.isSuccess()) {

                    Active.Key key = new Active.Key();

                    key.key = stringInput.getOrThrow();
                    key.continuous = false;

                    return DataResult.success(com.mojang.datafixers.util.Pair.of(key, input));

                }

                else {
                    return KEY.codec().decode(ops, input);
                }

            }

            @Override
            public <T> DataResult<T> encode(Active.Key input, DynamicOps<T> ops, T prefix) {
                return KEY.codec().encode(input, ops, prefix);
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

    public static final SerializableDataType<List<ArgumentWrapper<Integer>>> ITEM_SLOTS = ITEM_SLOT.list();

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

	public static SerializableDataType<Recipe<?>> RECIPE = new CompoundSerializableDataType<>(
		new SerializableData()
			.add("type", SerializableDataType.registry(Registries.RECIPE_SERIALIZER)),
		serializableData -> new MapCodec<>() {

			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops) {
				return serializableData.keys(ops);
			}

			@Override
			public <T> DataResult<Recipe<?>> decode(DynamicOps<T> ops, MapLike<T> input) {
				return serializableData.decode(ops, input)
					.map(recipeData -> (RecipeSerializer<?>) recipeData.get("type"))
					.flatMap(recipeSerializer -> recipeSerializer.codec().decode(ops, input)
						.map(Function.identity()));
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> RecordBuilder<T> encode(Recipe<?> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {

				RecipeSerializer<Recipe<?>> recipeSerializer = (RecipeSerializer<Recipe<?>>) input.getSerializer();
				Identifier recipeSerializerId = Objects.requireNonNull(Registries.RECIPE_SERIALIZER.getId(recipeSerializer), "Recipe serializer " + recipeSerializer + " is not registered?");

				prefix.add("type", SerializableDataTypes.IDENTIFIER.write(ops, recipeSerializerId));
				recipeSerializer.codec().encode(input, ops, prefix);

				return prefix;

			}

		},
		serializableData -> Recipe.PACKET_CODEC.cast()
	);

	public static SerializableDataType<CraftingRecipe> DISALLOWING_INTERNAL_CRAFTING_RECIPE = RECIPE.comapFlatMap(MiscUtil::validateCraftingRecipe, Function.identity());

    public static <T> SerializableDataType<ConditionTypeFactory<T>.Instance> condition(Registry<ConditionTypeFactory<T>> registry, String name) {
        return condition(registry, null, name);
    }

    public static <T> SerializableDataType<ConditionTypeFactory<T>.Instance> condition(Registry<ConditionTypeFactory<T>> registry, IdentifierAlias aliases, String name) {
        return condition("type", registry, aliases, (conditionFactories, id) -> name + " \"" + id + "\" is not registered!");
    }

    @SuppressWarnings("unchecked")
    public static <E> SerializableDataType<ConditionTypeFactory<E>.Instance> condition(String fieldName, Registry<ConditionTypeFactory<E>> registry, @Nullable IdentifierAlias aliases, BiFunction<Registry<ConditionTypeFactory<E>>, Identifier, String> errorHandler) {
        return new CompoundSerializableDataType<>(
            new SerializableData()
                .add(fieldName, SerializableDataType.registry(registry, Apoli.MODID, aliases, errorHandler)),
            serializableData -> {
				boolean root = serializableData.isRoot();
				return new MapCodec<>() {

					@Override
					public <T> Stream<T> keys(DynamicOps<T> ops) {
						return serializableData.keys(ops);
					}

					@Override
					public <T> DataResult<ConditionTypeFactory<E>.Instance> decode(DynamicOps<T> ops, MapLike<T> input) {
						return serializableData.decode(ops, input)
							.map(factoryData -> (ConditionTypeFactory<E>) factoryData.get(fieldName))
							.flatMap(factory -> factory.getSerializableData().setRoot(root).decode(ops, input)
								.map(factory::fromData));
					}

					@Override
					public <T> RecordBuilder<T> encode(ConditionTypeFactory<E>.Instance input, DynamicOps<T> ops, RecordBuilder<T> prefix) {

						prefix.add(fieldName, SerializableDataTypes.IDENTIFIER.write(ops, input.getSerializerId()));
						input.getSerializableData().setRoot(root).encode(input.getData(), ops, prefix);

						return prefix;

					}

				};
			},
            serializableData -> new PacketCodec<>() {

				@Override
				public ConditionTypeFactory<E>.Instance decode(RegistryByteBuf buf) {
					Identifier factoryId = buf.readIdentifier();
                    return registry.getOrEmpty(factoryId)
                        .map(factory -> factory.receive(buf))
                        .orElseThrow(() -> new IllegalStateException(errorHandler.apply(registry, factoryId)));
				}

				@Override
				public void encode(RegistryByteBuf buf, ConditionTypeFactory<E>.Instance value) {
                    value.send(buf);
				}

			}
        );
    }

    public static <E> SerializableDataType<ActionTypeFactory<E>.Instance> action(Registry<ActionTypeFactory<E>> registry, String name) {
        return action(registry, null, name);
    }

    public static <E> SerializableDataType<ActionTypeFactory<E>.Instance> action(Registry<ActionTypeFactory<E>> registry, IdentifierAlias aliases, String name) {
        return action("type", registry, aliases, (factories, id) -> name + " \"" + id + "\" is not registered!");
    }

    @SuppressWarnings("unchecked")
    public static <E> SerializableDataType<ActionTypeFactory<E>.Instance> action(String fieldName, Registry<ActionTypeFactory<E>> registry, @Nullable IdentifierAlias aliases, BiFunction<Registry<ActionTypeFactory<E>>, Identifier, String> errorHandler) {

        CompoundSerializableDataType<ActionTypeFactory<E>.Instance> dataType = new CompoundSerializableDataType<>(
            new SerializableData()
                .add(fieldName, SerializableDataType.registry(registry, Apoli.MODID, aliases, errorHandler)),
            serializableData -> {
				boolean root = serializableData.isRoot();
				return new MapCodec<>() {

					@Override
					public <T> Stream<T> keys(DynamicOps<T> ops) {
						return serializableData.keys(ops);
					}

					@Override
					public <T> DataResult<ActionTypeFactory<E>.Instance> decode(DynamicOps<T> ops, MapLike<T> input) {
						return serializableData.decode(ops, input)
							.map(factoryData -> (ActionTypeFactory<E>) factoryData.get(fieldName))
							.flatMap(factory -> factory.getSerializableData().setRoot(root).decode(ops, input)
								.map(factory::fromData));
					}

					@Override
					public <T> RecordBuilder<T> encode(ActionTypeFactory<E>.Instance input, DynamicOps<T> ops, RecordBuilder<T> prefix) {

						prefix.add(fieldName, SerializableDataTypes.IDENTIFIER.write(ops, input.getSerializerId()));
						input.getSerializableData().setRoot(root).encode(input.getData(), ops, prefix);

						return prefix;

					}

				};
			},
            serializableData -> new PacketCodec<>() {

				@Override
				public ActionTypeFactory<E>.Instance decode(RegistryByteBuf buf) {
					Identifier factoryId = buf.readIdentifier();
                    return registry.getOrEmpty(factoryId)
                        .map(factory -> factory.receive(buf))
                        .orElseThrow(() -> new IllegalStateException(errorHandler.apply(registry, factoryId)));
				}

				@Override
				public void encode(RegistryByteBuf buf, ActionTypeFactory<E>.Instance value) {
                    value.send(buf);
				}

			}
        );

        return SerializableDataType.recursive(self -> {

			SerializableDataType<ActionTypeFactory<E>.Instance> singleDataType = dataType.setRoot(self.isRoot());
			SerializableDataType<List<ActionTypeFactory<E>.Instance>> listDataType = singleDataType.list();

			return SerializableDataType.of(
				new Codec<>() {

					@Override
					public <T> DataResult<com.mojang.datafixers.util.Pair<ActionTypeFactory<E>.Instance, T>> decode(DynamicOps<T> ops, T input) {

						Optional<ActionTypeFactory<E>> optAndFactory = registry.getOrEmpty(Apoli.identifier("and"));

						if (ops.getList(input).isSuccess() && optAndFactory.isPresent()) {
							ActionTypeFactory<E> andFactory = optAndFactory.get();
							return listDataType.codec().decode(ops, input)
								.map(actionsAndInput -> actionsAndInput
									.mapFirst(actions -> andFactory.fromData(andFactory.getSerializableData().instance()
										.set("actions", actions))));
						}

						else {
							return singleDataType.codec().decode(ops, input);
						}

					}

					@Override
					public <T> DataResult<T> encode(ActionTypeFactory<E>.Instance input, DynamicOps<T> ops, T prefix) {
						return singleDataType.codec().encode(input, ops, prefix);
					}

				},
				singleDataType.packetCodec()
			);

		});

    }

}
