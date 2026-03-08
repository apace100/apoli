package io.github.apace100.apoli.data;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.*;
import io.github.apace100.apoli.action.Action;
import io.github.apace100.apoli.action.ActionConfiguration;
import io.github.apace100.apoli.action.type.ActionType;
import io.github.apace100.apoli.action.type.meta.SequenceMetaActionType;
import io.github.apace100.apoli.condition.Condition;
import io.github.apace100.apoli.condition.ConditionConfiguration;
import io.github.apace100.apoli.condition.type.ConditionType;
import io.github.apace100.apoli.data.container.ContainerType;
import io.github.apace100.apoli.data.container.DynamicContainerType;
import io.github.apace100.apoli.data.container.PresetContainerType;
import io.github.apace100.apoli.integration.ContainerTypeCodecEvents;
import io.github.apace100.apoli.power.PowerReference;
import io.github.apace100.apoli.util.*;
import io.github.apace100.apoli.util.context.ActionContext;
import io.github.apace100.apoli.util.context.ConditionContext;
import io.github.apace100.apoli.util.keybinding.KeyBindingReference;
import io.github.apace100.calio.SerializationHelper;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.registry.DataObjectFactories;
import io.github.apace100.calio.util.ArgumentWrapper;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.PlayerAbility;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemSlotArgumentType;
import net.minecraft.entity.EntityPose;
import net.minecraft.inventory.SlotRange;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.server.command.AdvancementCommand;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import net.minecraft.world.explosion.Explosion;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class ApoliDataTypes {

    public static final SerializableDataType<PowerReference> POWER_REFERENCE = SerializableDataTypes.IDENTIFIER.xmap(PowerReference::of, PowerReference::id);

	public static final SerializableDataType<PowerReference> RESOURCE_REFERENCE = SerializableDataTypes.IDENTIFIER.xmap(PowerReference::resource, PowerReference::id);

	public static final SerializableDataType<Space> SPACE = SerializableDataType.enumValue(Space.class);

    public static final SerializableDataType<ResourceOperation> RESOURCE_OPERATION = SerializableDataType.enumValue(ResourceOperation.class);

    public static final SerializableDataType<InventoryUtil.InventoryType> INVENTORY_TYPE = SerializableDataType.enumValue(InventoryUtil.InventoryType.class);

    public static final SerializableDataType<EnumSet<InventoryUtil.InventoryType>> INVENTORY_TYPE_SET = SerializableDataType.enumSet(INVENTORY_TYPE);

    public static final SerializableDataType<InventoryUtil.ProcessMode> PROCESS_MODE = SerializableDataType.enumValue(InventoryUtil.ProcessMode.class);

    public static final SerializableDataType<AttributedEntityAttributeModifier> ATTRIBUTED_ATTRIBUTE_MODIFIER = SerializableDataType.compound(
        DataObjectFactories.ATTRIBUTE_MODIFIER.getSerializableData().copy()
            .add("attribute", SerializableDataTypes.ATTRIBUTE_ENTRY),
        data -> new AttributedEntityAttributeModifier(
            data.get("attribute"),
            DataObjectFactories.ATTRIBUTE_MODIFIER.fromData(data)
        ),
        (attributedAttributeModifier, serializableData) -> DataObjectFactories.ATTRIBUTE_MODIFIER
            .toData(attributedAttributeModifier.modifier(), serializableData)
            .set("attribute", attributedAttributeModifier.attribute())
    );

    public static final SerializableDataType<List<AttributedEntityAttributeModifier>> ATTRIBUTED_ATTRIBUTE_MODIFIERS = ATTRIBUTED_ATTRIBUTE_MODIFIER.list(1, Integer.MAX_VALUE);

	/**
	 * 	<b>Use {@link IndexedStack#DATA_TYPE} instead.</b>
	 */
	@Deprecated(forRemoval = true)
    public static final SerializableDataType<Pair<Integer, ItemStack>> POSITIONED_ITEM_STACK = SerializableDataType.compound(
        DataObjectFactories.ITEM_STACK.getSerializableData().copy()
            .add("slot", SerializableDataTypes.INT, Integer.MIN_VALUE),
        data -> new Pair<>(
            data.getInt("slot"),
            DataObjectFactories.ITEM_STACK.fromData(data)
        ),
        (positionedStack, serializableData) -> DataObjectFactories.ITEM_STACK
            .toData(positionedStack.getRight(), serializableData)
            .set("slot", positionedStack.getLeft())
    );

	/**
	 * 	<b>Use {@link IndexedStack#DATA_TYPE} instead.</b>
	 */
	@Deprecated(forRemoval = true)
    public static final SerializableDataType<List<Pair<Integer, ItemStack>>> POSITIONED_ITEM_STACKS = POSITIONED_ITEM_STACK.list();

    public static final SerializableDataType<KeyBindingReference> KEY = KeyBindingReference.DATA_FACTORY.getDataType();

    public static final SerializableDataType<KeyBindingReference> BACKWARDS_COMPATIBLE_KEY = SerializableDataType.of(
        new Codec<>() {

            @Override
            public <T> DataResult<com.mojang.datafixers.util.Pair<KeyBindingReference, T>> decode(DynamicOps<T> ops, T input) {

                DataResult<String> stringInput = ops.getStringValue(input);
                if (stringInput.isSuccess()) {
					return stringInput
						.map(KeyBindingReference::new)
						.map(key -> com.mojang.datafixers.util.Pair.of(key, input));
                }

                else {
                    return KEY.codec().decode(ops, input);
                }

            }

            @Override
            public <T> DataResult<T> encode(KeyBindingReference input, DynamicOps<T> ops, T prefix) {
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

	public static final SerializableDataType<SlotRange> SLOT_RANGE = SerializableDataType.of(SlotRangesUtil.INDEX_OR_STRING_CODEC, SlotRangesUtil.PACKET_CODEC.cast());

	public static final SerializableDataType<List<SlotRange>> SLOT_RANGES = SLOT_RANGE.list();

	public static final SerializableDataType<SlotRange> SINGLE_SLOT_RANGE = SerializableDataType.of(SlotRangesUtil.SINGLE_INDEX_OR_STRING_CODEC, SlotRangesUtil.PACKET_CODEC.cast());

	public static final SerializableDataType<List<SlotRange>> SINGLE_SLOT_RANGES = SINGLE_SLOT_RANGE.list();

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

	/**
	 *  <b>Use {@link #REGEX_REPLACEMENT_MAP} instead for further functionality (e.g: referencing capture groups of the paired regex)</b>
	 */
	@Deprecated(forRemoval = true)
    public static final SerializableDataType<Map<Pattern, Identifier>> REGEX_MAP = SerializableDataType.map(REGEX, SerializableDataTypes.IDENTIFIER);

	public static final SerializableDataType<Map<Pattern, String>> REGEX_REPLACEMENT_MAP = SerializableDataType.map(REGEX, SerializableDataTypes.STRING);

    public static final SerializableDataType<GameMode> GAME_MODE = SerializableDataType.enumValue(GameMode.class);

    //  This is for keeping backwards compatibility to fields that used to accept strings as translation keys
    public static final SerializableDataType<Text> DEFAULT_TRANSLATABLE_TEXT = SerializableDataType.of(
		new Codec<>() {

			@Override
			public <T> DataResult<com.mojang.datafixers.util.Pair<Text, T>> decode(DynamicOps<T> ops, T input) {

				DataResult<String> inputString = ops.getStringValue(input);
				if (inputString.isSuccess()) {
					return inputString
						.map(Text::translatable)
						.map(text -> com.mojang.datafixers.util.Pair.of(text, input));
				}

				else {
					return SerializableDataTypes.TEXT.codec().decode(ops, input);
				}

			}

			@Override
			public <T> DataResult<T> encode(Text input, DynamicOps<T> ops, T prefix) {
				return SerializableDataTypes.TEXT.codec().encode(input, ops, prefix);
			}

		},
		TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC
	);

    public static final SerializableDataType<StackClickPhase> STACK_CLICK_PHASE = SerializableDataType.enumValue(StackClickPhase.class);

    public static final SerializableDataType<EnumSet<StackClickPhase>> STACK_CLICK_PHASE_SET = SerializableDataType.enumSet(STACK_CLICK_PHASE);

    public static final SerializableDataType<BlockUsagePhase> BLOCK_USAGE_PHASE = SerializableDataType.enumValue(BlockUsagePhase.class);

    public static final SerializableDataType<EnumSet<BlockUsagePhase>> BLOCK_USAGE_PHASE_SET = SerializableDataType.enumSet(BLOCK_USAGE_PHASE);

    public static final SerializableDataType<EntityPose> ENTITY_POSE = SerializableDataType.enumValue(EntityPose.class);

    public static final SerializableDataType<ArmPoseReference> ARM_POSE_REFERENCE = SerializableDataType.enumValue(ArmPoseReference.class);

	public static final SerializableDataType<CraftingRecipe> DISALLOWING_INTERNAL_CRAFTING_RECIPE = SerializableDataTypes.RECIPE.comapFlatMap(RecipeUtil::validateCraftingRecipe, Function.identity());

	public static final SerializableDataType<Float> NORMALIZED_FLOAT = SerializableDataType.boundNumber(SerializableDataTypes.FLOAT, 0F, 1F);

	public static final SerializableDataType<ContainerType> CONTAINER_TYPE = new SerializableDataType<>(
		new Codec<>() {

			@Override
			public <T> DataResult<com.mojang.datafixers.util.Pair<ContainerType, T>> decode(DynamicOps<T> ops, T input) {

				Optional<DataResult<com.mojang.datafixers.util.Pair<ContainerType, T>>> customResult = ContainerTypeCodecEvents.DECODING.invoker().decode(ops, input);
				var customError = customResult.flatMap(DataResult::error);

				if (customResult.isPresent()) {
					return customResult.get();
				}

				DataResult<String> presetResult = ops.getStringValue(input);
				var presetError = presetResult.error();

				if (presetResult.isSuccess()) {
					return ApoliContainerTypes.REGISTRY_DATA_TYPE.codec().decode(ops, input);
				}

				DataResult<MapLike<T>> dynamicResult = ops.getMap(input);
				var dynamicError = dynamicResult.error();

				if (dynamicResult.isSuccess()) {
					return DynamicContainerType.DATA_TYPE.codec().decode(ops, input)
						.map(containerTypeAndInput -> containerTypeAndInput
							.mapFirst(Function.identity()));
				}

				StringBuilder errorMsg = new StringBuilder("Couldn't decode as a ");
				customError.ifPresent(err -> errorMsg.append("custom container type (").append(err).append("), "));

				presetError.ifPresent(err -> errorMsg.append("preset container type (").append(err).append(")"));
				dynamicError.ifPresent(err -> errorMsg.append(", or as a dynamic container type (").append(err).append(")"));

				return DataResult.error(errorMsg::toString);

			}

			@Override
			public <T> DataResult<T> encode(ContainerType input, DynamicOps<T> ops, T prefix) {
				return switch (input) {
					case DynamicContainerType dynamicContainerType ->
						DynamicContainerType.DATA_TYPE.write(ops, dynamicContainerType);
					case PresetContainerType presetContainerType ->
						ApoliContainerTypes.REGISTRY_DATA_TYPE.write(ops, input);
					default ->
						ContainerTypeCodecEvents.ENCODING.invoker()
							.encode(input, ops, prefix)
							.orElse(DataResult.error(() -> "Missing encoding handler for custom container type \"" + input + "\"!"));
				};
			}

		},
		new PacketCodec<>() {

			@Override
			public ContainerType decode(RegistryByteBuf buf) {

				if (buf.readBoolean()) {
					return DynamicContainerType.DATA_TYPE.receive(buf);
				}

				else {
					return ApoliContainerTypes.REGISTRY_DATA_TYPE.receive(buf);
				}

			}

			@Override
			public void encode(RegistryByteBuf buf, ContainerType value) {

				if (value instanceof DynamicContainerType dynamicContainerType) {
					buf.writeBoolean(true);
					DynamicContainerType.DATA_TYPE.send(buf, dynamicContainerType);
				}

				else {
					buf.writeBoolean(false);
					ApoliContainerTypes.REGISTRY_DATA_TYPE.send(buf, value);
				}

			}

		}
	);

	public static final SerializableDataType<Vec3i> VECTOR_3_INT = SerializableDataType.compound(
		new SerializableData()
			.add("x", SerializableDataTypes.INT, 0)
			.add("y", SerializableDataTypes.INT, 0)
			.add("z", SerializableDataTypes.INT, 0),
		data -> new Vec3i(
			data.get("x"),
			data.get("y"),
			data.get("z")
		),
		(vec3i, serializableData) -> serializableData.instance()
			.set("x", vec3i.getX())
			.set("y", vec3i.getY())
			.set("z", vec3i.getZ())
	);

	public static final SerializableDataType<Vector3f> VECTOR_3_FLOAT = SerializableDataType.compound(
		new SerializableData()
			.add("x", SerializableDataTypes.FLOAT, 0F)
			.add("y", SerializableDataTypes.FLOAT, 0F)
			.add("z", SerializableDataTypes.FLOAT, 0F),
		data -> new Vector3f(
			data.get("x"),
			data.get("y"),
			data.get("z")
		),
		(vector3f, serializableData) -> serializableData.instance()
			.set("x", vector3f.x())
			.set("y", vector3f.y())
			.set("z", vector3f.z())
	);

	@SuppressWarnings("unchecked")
	public static <T extends ConditionContext, C extends Condition<T, CT>, CT extends ConditionType<T, C>> CompoundSerializableDataType<C> condition(String typeField, SerializableDataType<ConditionConfiguration<CT>> registryDataType, BiFunction<CT, Boolean, C> constructor) {
		return new CompoundSerializableDataType<>(
			new SerializableData()
				.add(typeField, registryDataType)
				.add("inverted", SerializableDataTypes.BOOLEAN, false),
			serializableData -> {
				boolean root = serializableData.isRoot();
				return new MapCodec<>() {

					@Override
					public <I> Stream<I> keys(DynamicOps<I> ops) {
						return serializableData.keys(ops);
					}

					@Override
					public <I> DataResult<C> decode(DynamicOps<I> ops, MapLike<I> input) {

						DataResult<SerializableData.Instance> conditionDataResult = serializableData.decode(ops, input);
						DataResult<CT> conditionTypeResult = conditionDataResult
							.map(conditionData -> (ConditionConfiguration<CT>) conditionData.get(typeField))
							.flatMap(config -> config.mapCodec(root).decode(ops, input));

						return conditionDataResult
							.flatMap(conditionData -> conditionTypeResult
								.map(conditionType -> constructor.apply(conditionType, conditionData.getBoolean("inverted"))));

					}

					@Override
					public <I> RecordBuilder<I> encode(C input, DynamicOps<I> ops, RecordBuilder<I> prefix) {

						CT conditionType = input.getType();
						ConditionConfiguration<CT> config = (ConditionConfiguration<CT>) conditionType.getConfig();

						prefix.add(typeField, registryDataType.write(ops, config));
						config.mapCodec(root).encode(conditionType, ops, prefix);

						prefix.add("inverted", ops.createBoolean(input.isInverted()));
						return prefix;

					}

				};
			},
			serializableData -> new PacketCodec<>() {

				@Override
				public C decode(RegistryByteBuf buf) {

					SerializableData.Instance conditionData = serializableData.receive(buf);

					ConditionConfiguration<CT> config = conditionData.get(typeField);
					boolean inverted = conditionData.getBoolean("inverted");

					return constructor.apply(config.dataType().receive(buf), inverted);

				}

				@Override
				public void encode(RegistryByteBuf buf, C value) {

					CT conditionType = value.getType();
					ConditionConfiguration<CT> config = (ConditionConfiguration<CT>) conditionType.getConfig();

					SerializableData.Instance conditionData = serializableData.instance()
						.set(typeField, config)
						.set("inverted", value.isInverted());

					serializableData.send(buf, conditionData);
					config.dataType().send(buf, conditionType);

				}

			}
		);
	}

	@SuppressWarnings("unchecked")
	public static <T extends ActionContext<?>, A extends Action<T, AT>, AT extends ActionType<T, A>> CompoundSerializableDataType<A> action(String typeField, SerializableDataType<ActionConfiguration<AT>> registryDataType, Function<AT, A> constructor) {
		return new CompoundSerializableDataType<>(
			new SerializableData()
				.add(typeField, registryDataType),
			serializableData -> {
				boolean root = serializableData.isRoot();
				return new MapCodec<>() {

					@Override
					public <I> Stream<I> keys(DynamicOps<I> ops) {
						return serializableData.keys(ops);
					}

					@Override
					public <I> DataResult<A> decode(DynamicOps<I> ops, MapLike<I> input) {
						return serializableData.decode(ops, input)
							.map(actionData -> (ActionConfiguration<AT>) actionData.get(typeField))
							.flatMap(config -> config.mapCodec(root).decode(ops, input)
								.map(constructor));
					}

					@Override
					public <I> RecordBuilder<I> encode(A input, DynamicOps<I> ops, RecordBuilder<I> prefix) {

						AT actionType = input.getType();
						ActionConfiguration<AT> config = (ActionConfiguration<AT>) actionType.getConfig();

						prefix.add(typeField, registryDataType.write(ops, config));
						config.mapCodec(root).encode(actionType, ops, prefix);

						return prefix;

					}

				};
			},
			serializableData -> new PacketCodec<>() {

				@Override
				public A decode(RegistryByteBuf buf) {

					SerializableData.Instance actionData = serializableData.receive(buf);
					ActionConfiguration<AT> config = actionData.get(typeField);

					return constructor.apply(config.dataType().receive(buf));

				}

				@Override
				public void encode(RegistryByteBuf buf, A value) {

					AT actionType = value.getType();
					ActionConfiguration<AT> config = (ActionConfiguration<AT>) actionType.getConfig();

					SerializableData.Instance actionData = serializableData.instance()
						.set(typeField, config);

					serializableData.send(buf, actionData);
					config.dataType().send(buf, actionType);

				}

			}
		);
	}

	@SuppressWarnings("unchecked")
	public static <T extends ActionContext<?>, A extends Action<T, AT>, AT extends ActionType<T, A>, M extends ActionType<T, A> & SequenceMetaActionType<T, A>> SerializableDataType<A> actions(String typeField, SerializableDataType<ActionConfiguration<AT>> registryDataType, Function<List<A>, M> multiActionsConstructor, Function<AT, A> constructor) {

		CompoundSerializableDataType<A> dataType = action(typeField, registryDataType, constructor);
		SerializableDataType<List<A>> listDataType = dataType.list();

		return SerializableDataType.recursive(self -> SerializableDataType.of(
			new Codec<>() {

				@Override
				public <I> DataResult<com.mojang.datafixers.util.Pair<A, I>> decode(DynamicOps<I> ops, I input) {

					if (ops.getList(input).isSuccess()) {
						return listDataType.setRoot(self.isRoot()).codec().decode(ops, input)
							.map(actionsAndInput -> actionsAndInput
								.mapFirst(multiActionsConstructor)
								.mapFirst(m -> constructor.apply((AT) m)));
					}

					else {
						return dataType.setRoot(self.isRoot()).codec().decode(ops, input);
					}

				}

				@Override
				public <I> DataResult<I> encode(A input, DynamicOps<I> ops, I prefix) {
					return dataType.setRoot(self.isRoot()).codec().encode(input, ops, prefix);
				}

			},
			dataType.packetCodec()
		));

	}

}
