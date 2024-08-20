package io.github.apace100.apoli.power.type;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TooltipPowerType extends PowerType {

    private final Predicate<Pair<World, ItemStack>> itemCondition;
    private final List<Text> texts;
    private final int tickRate;
    private final int order;

    private List<Text> tooltipTexts;
    private Integer initialTicks;
    private boolean shouldResolve;

    public TooltipPowerType(Power power, LivingEntity entity, Predicate<Pair<World, ItemStack>> itemCondition, Text text, List<Text> texts, boolean shouldResolve, int tickRate, int order) {
        super(power, entity);
        this.texts = new LinkedList<>();
        this.tooltipTexts = new LinkedList<>();
        if (text != null) {
            this.texts.add(text);
        }
        if (texts != null) {
            this.texts.addAll(texts);
        }
        this.itemCondition = itemCondition;
        this.shouldResolve = shouldResolve;
        this.tickRate = tickRate <= 0 ? 1 : tickRate;
        this.order = order;
        if (shouldResolve) {
            this.setTicking(true);
        }
    }

    @Override
    public void tick() {

        if (isActive()) {

            if (initialTicks == null) {
                initialTicks = entity.age % tickRate;
                return;
            }

            if (entity.age % tickRate != initialTicks) {
                return;
            }

            List<Text> parsedTexts = parseTexts();
            if (parsedTexts.isEmpty() || !Collections.disjoint(tooltipTexts, parsedTexts)) {
                return;
            }

            tooltipTexts = parsedTexts;
            PowerHolderComponent.syncPower(entity, this.getPower());

        } else if (initialTicks != null) {
            initialTicks = null;
        }

    }

    @Override
    public NbtElement toTag() {

        NbtCompound rootNbt = new NbtCompound();
        NbtList tooltipTextsNbt = new NbtList();

        for (Text tooltipText : tooltipTexts) {
            NbtString tooltipTextNbt = NbtString.of(Text.Serialization.toJsonString(tooltipText, entity.getRegistryManager()));
            tooltipTextsNbt.add(tooltipTextNbt);
        }

        rootNbt.put("Tooltips", tooltipTextsNbt);
        rootNbt.putBoolean("ShouldResolve", shouldResolve);
        return rootNbt;

    }

    @Override
    public void fromTag(NbtElement tag) {

        tooltipTexts.clear();
        NbtCompound rootNbt = (NbtCompound) tag;
        NbtList tooltipTextsNbt = rootNbt.getList("Tooltips", NbtElement.STRING_TYPE);

        for (int i = 0; i < tooltipTextsNbt.size(); i++) {
            Text tooltipText = Text.Serialization.fromJson(tooltipTextsNbt.getString(i), entity.getRegistryManager());
            tooltipTexts.add(tooltipText);
        }

        shouldResolve = rootNbt.getBoolean("ShouldResolve");

    }

    public int getOrder() {
        return order;
    }

    public void addToTooltip(Consumer<Text> tooltipConsumer) {

        if (shouldResolve) {
            tooltipTexts.forEach(tooltipConsumer);
        }

        else {
            texts.forEach(tooltipConsumer);
        }

    }

    public boolean doesApply(ItemStack stack) {
        return itemCondition == null || itemCondition.test(new Pair<>(entity.getWorld(), stack));
    }

    private List<Text> parseTexts() {

        List<Text> parsedTexts = Lists.newLinkedList();
        if (texts.isEmpty() || entity.getWorld().isClient) {
            return parsedTexts;
        }

        ServerCommandSource source = new ServerCommandSource(
            CommandOutput.DUMMY,
            entity.getPos(),
            entity.getRotationClient(),
            (ServerWorld) entity.getWorld(),
            Apoli.config.executeCommand.permissionLevel,
            entity.getNameForScoreboard(),
            entity.getName(),
            entity.getWorld().getServer(),
            entity
        );

        for (int i = 0; i < texts.size(); i++) {
            try {

                Text text = texts.get(i);
                Text parsedText = Texts.parse(source, text, entity, 0);

                parsedTexts.add(parsedText);

            } catch (CommandSyntaxException e) {
                Apoli.LOGGER.warn("Power {} could not parse tooltip text at index {}: {}", this.getPower().getId(), i, e.getMessage());
            }
        }

        return parsedTexts;

    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("tooltip"),
            new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null)
                .add("text", SerializableDataTypes.TEXT, null)
                .add("texts", SerializableDataTypes.TEXTS, null)
                .add("should_resolve", SerializableDataTypes.BOOLEAN, false)
                .add("tick_rate", SerializableDataTypes.INT, 20)
                .add("order", SerializableDataTypes.INT, 0),
            data -> (power, entity) -> new TooltipPowerType(power, entity,
                data.get("item_condition"),
                data.get("text"),
                data.get("texts"),
                data.get("should_resolve"),
                data.get("tick_rate"),
                data.get("order")
            )
        ).allowCondition();
    }

}
