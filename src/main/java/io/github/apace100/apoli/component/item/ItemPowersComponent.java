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
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
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
public class ItemPowersComponent {

    public static final Codec<ItemPowersComponent> CODEC = SetCodec.of(Entry.CODEC).xmap(
        entries -> new ItemPowersComponent(new ObjectLinkedOpenHashSet<>(entries)),
        itemPowersComponent -> itemPowersComponent.entries
    );
    public static final PacketCodec<PacketByteBuf, ItemPowersComponent> PACKET_CODEC = PacketCodecs.collection(size -> new ObjectLinkedOpenHashSet<>(), Entry.PACKET_CODEC).xmap(
        ItemPowersComponent::new,
        itemPowersComponent -> itemPowersComponent.entries
    );

    final ObjectLinkedOpenHashSet<Entry> entries;

    ItemPowersComponent(ObjectLinkedOpenHashSet<Entry> entries) {
        this.entries = entries;
    }

    public void appendTooltip(AttributeModifierSlot modifierSlot, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {

        for (Entry entry : entries) {

            PowerType<?> power = entry.power();
            if (entry.hidden() || !entry.slots().contains(modifierSlot)) {
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
        return entries
            .stream()
            .anyMatch(entry -> entry.slots().contains(modifierSlot));
    }

    public static void onChangeEquipment(LivingEntity entity, EquipmentSlot slot, ItemStack previousStack, ItemStack currentStack) {

        if (ItemStack.areEqual(previousStack, currentStack) || !PowerHolderComponent.KEY.isProvidedBy(entity)) {
            return;
        }

        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(entity);
        Identifier sourceId = Apoli.identifier("item/" + slot.getName());

        ItemPowersComponent prevStackPowers = previousStack.get(ApoliDataComponentTypes.POWERS);
        if (prevStackPowers != null) {

            boolean revoked = false;
            for (Entry prevEntry : prevStackPowers.entries) {

                if (prevEntry.matchesSlot(slot) && powerComponent.removePower(prevEntry.power(), sourceId)) {
                    revoked = true;
                }

            }

            if (revoked) {
                powerComponent.sync();
            }

        }

        ItemPowersComponent currStackPowers = currentStack.get(ApoliDataComponentTypes.POWERS);
        if (currStackPowers != null) {

            boolean granted = false;
            for (Entry currEntry : currStackPowers.entries) {

                if (currEntry.matchesSlot(slot) && powerComponent.addPower(currEntry.power(), sourceId)) {
                    granted = true;
                }

            }

            if (granted) {
                powerComponent.sync();
            }

        }

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

        public boolean matchesSlot(EquipmentSlot equipmentSlot) {
            return slots
                .stream()
                .anyMatch(modifierSlot -> modifierSlot.matches(equipmentSlot));
        }

    }

}
