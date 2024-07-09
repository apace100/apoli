package io.github.apace100.apoli.component.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.apoli.util.ApoliCodecs;
import io.github.apace100.apoli.util.ApoliPacketCodecs;
import io.github.apace100.apoli.util.codec.SetCodec;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.function.Consumer;

//  TODO: Make the data of stack powers persist in the item stack
public class StackPowersComponent {

    public static final Codec<StackPowersComponent> CODEC = SetCodec.of(Entry.CODEC).xmap(
        StackPowersComponent::fromEntrySet,
        stackPowersComponent -> stackPowersComponent.entries
    );
    public static final PacketCodec<PacketByteBuf, StackPowersComponent> PACKET_CODEC = PacketCodecs.collection(size -> new ObjectArraySet<>(), Entry.PACKET_CODEC).xmap(
        StackPowersComponent::new,
        stackPowersComponent -> stackPowersComponent.entries
    );

    final ObjectArraySet<Entry> entries;

    public StackPowersComponent(ObjectArraySet<Entry> entries) {
        this.entries = entries;
    }

    public void appendTooltip(AttributeModifierSlot modifierSlot, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {

        for (Entry entry : entries) {

            PowerType<?> power = entry.power();
            if (entry.hidden() || !containsSlot(entry, modifierSlot)) {
                continue;
            }

            tooltip.accept(Text
                .translatable("tooltip.apoli.stack_power.name", power.getName())
                .formatted(entry.negative()
                    ? Formatting.RED
                    : Formatting.YELLOW));

            if (!type.isAdvanced()) {
                continue;
            }

            tooltip.accept(Text
                .translatable("tooltip.apoli.stack_power.description", power.getDescription())
                .formatted(Formatting.GRAY));

        }

    }

    public boolean containsSlot(AttributeModifierSlot modifierSlot) {
        return entries.stream()
            .anyMatch(entry -> containsSlot(entry, modifierSlot));
    }

    public static boolean matchesSlot(Entry entry, EquipmentSlot slot) {
        return entry.slots()
            .stream()
            .anyMatch(modifierSlot -> modifierSlot.matches(slot));
    }

    public static boolean containsSlot(Entry entry, AttributeModifierSlot modifierSlot) {
        return entry.slots()
            .stream()
            .anyMatch(modifierSlot::equals);
    }

    @SuppressWarnings("ReplaceInefficientStreamCount")
    public static void onChangeEquipment(LivingEntity entity, EquipmentSlot slot, ItemStack previousStack, ItemStack currentStack) {

        if (ItemStack.areEqual(previousStack, currentStack) || !PowerHolderComponent.KEY.isProvidedBy(entity)) {
            return;
        }

        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(entity);
        Identifier sourceId = Apoli.identifier("stack_power/" + slot.getName());

        StackPowersComponent prevStackPowers = previousStack.get(ApoliDataComponentTypes.STACK_POWERS_COMPONENT);
        if (prevStackPowers != null) {

            boolean revoked = prevStackPowers.entries
                .stream()
                .filter(entry -> matchesSlot(entry, slot)
                    && powerComponent.removePower(entry.power(), sourceId))
                .count() > 0;

            if (revoked) {
                powerComponent.sync();
            }

        }

        StackPowersComponent currStackPowers = currentStack.get(ApoliDataComponentTypes.STACK_POWERS_COMPONENT);
        if (currStackPowers != null) {

            boolean granted = currStackPowers.entries
                .stream()
                .filter(entry -> matchesSlot(entry, slot)
                    && powerComponent.addPower(entry.power(), sourceId))
                .count() > 0;

            if (granted) {
                powerComponent.sync();
            }

        }

    }

    private static StackPowersComponent fromEntrySet(Set<Entry> entries) {
        return new StackPowersComponent(new ObjectArraySet<>(entries));
    }

    public record Entry(PowerType<?> power, Set<AttributeModifierSlot> slots, NbtCompound data, boolean hidden, boolean negative) {

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PowerTypeRegistry.DISPATCH_CODEC.fieldOf("power").forGetter(Entry::power),
            ApoliCodecs.ATTRIBUTE_MODIFIER_SLOT_SET.fieldOf("slots").forGetter(Entry::slots),
            NbtCompound.CODEC.optionalFieldOf("data", new NbtCompound()).forGetter(Entry::data),
            Codec.BOOL.optionalFieldOf("hidden", false).forGetter(Entry::hidden),
            Codec.BOOL.optionalFieldOf("negative", false).forGetter(Entry::negative)
        ).apply(instance, Entry::new));

        public static final PacketCodec<PacketByteBuf, Entry> PACKET_CODEC = PacketCodec.tuple(
            PowerTypeRegistry.DISPATCH_PACKET_CODEC, Entry::power,
            ApoliPacketCodecs.ATTRIBUTE_MODIFIER_SLOT_SET, Entry::slots,
            PacketCodecs.UNLIMITED_NBT_COMPOUND, Entry::data,
            PacketCodecs.BOOL, Entry::hidden,
            PacketCodecs.BOOL, Entry::negative,
            Entry::new
        );

        @Override
        public boolean equals(Object obj) {
            return this == obj
                || (obj instanceof Entry other
                && this.power().equals(other.power()));
        }

    }

}
