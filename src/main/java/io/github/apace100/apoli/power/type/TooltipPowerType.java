package io.github.apace100.apoli.power.type;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.condition.ItemCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.apoli.util.MiscUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TooltipPowerType extends PowerType {

    public static final TypedDataObjectFactory<TooltipPowerType> DATA_FACTORY = PowerType.createConditionedDataFactory(
        new SerializableData()
            .add("item_condition", ItemCondition.DATA_TYPE.optional(), Optional.empty())
            .add("text", SerializableDataTypes.TEXT, null)
            .addFunctionedDefault("texts", SerializableDataTypes.TEXTS, data -> MiscUtil.singletonListOrNull(data.get("text")))
            .add("should_resolve", SerializableDataTypes.BOOLEAN, false)
            .addFunctionedDefault("resolve", SerializableDataTypes.BOOLEAN, data -> data.get("should_resolve"))
            .add("tick_rate", SerializableDataTypes.INT, 20)
            .add("order", SerializableDataTypes.INT, 0)
            .validate(MiscUtil.validateAnyFieldsPresent("text", "texts")),
        (data, condition) -> new TooltipPowerType(
            data.get("item_condition"),
            data.get("texts"),
            data.get("resolve"),
            data.get("tick_rate"),
            data.get("order"),
            condition
        ),
        (powerType, serializableData) -> serializableData.instance()
            .set("item_condition", powerType.itemCondition)
            .set("texts", powerType.texts)
            .set("resolve", powerType.resolve)
            .set("tick_rate", powerType.tickRate)
            .set("order", powerType.order)
    );

    private final Optional<ItemCondition> itemCondition;
    private final List<Text> texts;

    private final int tickRate;
    private final int order;

    private List<Text> tooltipTexts;
    private boolean resolve;

    private Integer startTicks;
    private Integer endTicks;

    private boolean wasActive;

    public TooltipPowerType(Optional<ItemCondition> itemCondition, List<Text> texts, boolean resolve, int tickRate, int order, Optional<EntityCondition> condition) {
        super(condition);
        this.itemCondition = itemCondition;
        this.texts = texts;
        this.resolve = resolve;
        this.tickRate = tickRate;
        this.order = order;
    }

    @Override
    public @NotNull PowerConfiguration<?> getConfig() {
        return PowerTypes.TOOLTIP;
    }

    @Override
    public boolean shouldTick() {
        return resolve;
    }

    @Override
    public boolean shouldTickWhenInactive() {
        return shouldTick();
    }

    @Override
    public void serverTick() {

        LivingEntity holder = getHolder();
        int modTicks = holder.age % tickRate;

        if (isActive()) {

            if (startTicks == null) {
                this.startTicks = modTicks;
                this.endTicks = null;
            }

            else if (modTicks == startTicks) {

                List<Text> parsedTexts = parseTexts();
                this.wasActive = true;

                if (!parsedTexts.isEmpty() && Collections.disjoint(tooltipTexts, parsedTexts)) {
                    this.tooltipTexts = parsedTexts;
                    PowerHolderComponent.syncPower(getHolder(), getPower());
                }

            }

        }

        else if (wasActive) {

            if (endTicks == null) {
                this.startTicks = null;
                this.endTicks = modTicks;
            }

            else if (modTicks == endTicks) {
                this.wasActive = false;
            }

        }

    }

    @Override
    public NbtElement toTag() {

        NbtCompound rootNbt = new NbtCompound();
        NbtList tooltipTextsNbt = new NbtList();

        for (Text tooltipText : tooltipTexts) {
            NbtString tooltipTextNbt = NbtString.of(Text.Serialization.toJsonString(tooltipText, getHolder().getRegistryManager()));
            tooltipTextsNbt.add(tooltipTextNbt);
        }

        rootNbt.put("Tooltips", tooltipTextsNbt);
        rootNbt.putBoolean("ShouldResolve", resolve);
        return rootNbt;

    }

    @Override
    public void fromTag(NbtElement tag) {

        tooltipTexts.clear();
        NbtCompound rootNbt = (NbtCompound) tag;
        NbtList tooltipTextsNbt = rootNbt.getList("Tooltips", NbtElement.STRING_TYPE);

        for (int i = 0; i < tooltipTextsNbt.size(); i++) {
            Text tooltipText = Text.Serialization.fromJson(tooltipTextsNbt.getString(i), getHolder().getRegistryManager());
            tooltipTexts.add(tooltipText);
        }

        resolve = rootNbt.getBoolean("ShouldResolve");

    }

    public int getOrder() {
        return order;
    }

    public void addToTooltip(Consumer<Text> tooltipConsumer) {

        if (resolve) {
            tooltipTexts.forEach(tooltipConsumer);
        }

        else {
            texts.forEach(tooltipConsumer);
        }

    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition
            .map(condition -> condition.test(getHolder().getWorld(), stack))
            .orElse(true);
    }

    private List<Text> parseTexts() {

        List<Text> parsedTexts = Lists.newLinkedList();
        LivingEntity holder = getHolder();

        if (texts.isEmpty() || !(holder.getWorld() instanceof ServerWorld serverWorld)) {
            return parsedTexts;
        }

        ServerCommandSource source = holder.getCommandSource()
            .withOutput(serverWorld.getServer())
            .withLevel(Apoli.config.executeCommand.permissionLevel);

        for (int i = 0; i < texts.size(); i++) {

            try {

                Text text = texts.get(i);
                Text parsedText = Texts.parse(source, text, holder, 0);

                parsedTexts.add(parsedText);

            }

            catch (CommandSyntaxException e) {
                Apoli.LOGGER.warn("Power {} could not parse tooltip text at index {}: {}", this.getPower().getId(), i, e.getMessage());
            }

        }

        return parsedTexts;

    }

}
