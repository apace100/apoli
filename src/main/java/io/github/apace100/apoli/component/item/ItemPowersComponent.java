package io.github.apace100.apoli.component.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerManager;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

//  TODO: Make the data of stack powers persist in the item stack
public class ItemPowersComponent {

    public static final ItemPowersComponent DEFAULT = new ItemPowersComponent(Set.of());

    public static final Codec<ItemPowersComponent> CODEC = Entry.SET_CODEC.xmap(
        ItemPowersComponent::new,
		ItemPowersComponent::entries
    );

    public static final PacketCodec<ByteBuf, ItemPowersComponent> PACKET_CODEC = PacketCodecs.collection(ObjectLinkedOpenHashSet::new, Entry.PACKET_CODEC).xmap(
        ItemPowersComponent::new,
        ItemPowersComponent::entries
    );

    final ObjectLinkedOpenHashSet<Entry> entries;

    ItemPowersComponent(Collection<Entry> entries) {
        this.entries = new ObjectLinkedOpenHashSet<>(entries);
    }

    @Override
    public String toString() {
        return "ItemPowersComponent{entries=" + entries + "}";
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        else if (!(obj instanceof ItemPowersComponent that)) {
            return false;
        }

        else {
            return Objects.equals(this.entries(), that.entries());
        }

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entries);
    }

    private ObjectLinkedOpenHashSet<Entry> entries() {
        return entries;
    }

    public Stream<Entry> stream() {
        return entries.stream();
    }

    public void appendTooltip(AttributeModifierSlot modifierSlot, Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {

        for (Entry entry : entries) {

            Power power = PowerManager.getOptional(entry.powerId()).orElse(null);
            if (power == null || entry.hidden() || !entry.slot().equals(modifierSlot)) {
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
            .map(Entry::slot)
            .anyMatch(modifierSlot::equals);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int size() {
        return entries.size();
    }

    public static void onChangeEquipment(LivingEntity entity, EquipmentSlot equipmentSlot, ItemStack previousStack, ItemStack currentStack) {

        Identifier sourceId = Apoli.identifier("item/" + equipmentSlot.getName());
        if (ItemStack.areEqual(previousStack, currentStack) || !PowerHolderComponent.KEY.isProvidedBy(entity)) {
            return;
        }

        List<Power> revokedPowers = new ObjectArrayList<>();
        ItemPowersComponent prevStackPowers = previousStack.getOrDefault(ApoliDataComponentTypes.POWERS, DEFAULT);

        for (Entry prevEntry : prevStackPowers.entries) {
            PowerManager.getOptional(prevEntry.powerId())
                .filter(power -> prevEntry.slot().matches(equipmentSlot))
                .ifPresent(revokedPowers::add);
        }

        List<Power> grantedPowers = new ObjectArrayList<>();
        ItemPowersComponent currStackPowers = currentStack.getOrDefault(ApoliDataComponentTypes.POWERS, DEFAULT);

        for (Entry currEntry : currStackPowers.entries) {
            PowerManager.getOptional(currEntry.powerId())
                .filter(power -> currEntry.slot().matches(equipmentSlot))
                .ifPresent(grantedPowers::add);
        }

        if (!revokedPowers.isEmpty()) {
            PowerHolderComponent.revokePowers(entity, Map.of(sourceId, revokedPowers), true);
        }

        if (!grantedPowers.isEmpty()) {
            PowerHolderComponent.grantPowers(entity, Map.of(sourceId, grantedPowers), true);
        }

    }

    public record Entry(Identifier powerId, AttributeModifierSlot slot, boolean hidden, boolean negative) {

        public static final MapCodec<Entry> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PowerManager.VALIDATING_CODEC.fieldOf("power").forGetter(Entry::powerId),
            AttributeModifierSlot.CODEC.fieldOf("slot").forGetter(Entry::slot),
            Codec.BOOL.optionalFieldOf("hidden", false).forGetter(Entry::hidden),
            Codec.BOOL.optionalFieldOf("negative", false).forGetter(Entry::negative)
        ).apply(instance, Entry::new));

        public static final PacketCodec<ByteBuf, Entry> PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, Entry::powerId,
            AttributeModifierSlot.PACKET_CODEC, Entry::slot,
            PacketCodecs.BOOL, Entry::hidden,
            PacketCodecs.BOOL, Entry::negative,
            Entry::new
        );

        public static final Codec<Set<Entry>> SET_CODEC = MAP_CODEC.codec().listOf().xmap(
			ImmutableSet::copyOf,
			ImmutableList::copyOf
        );

        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }

            else if (obj instanceof Entry other) {
                return this.powerId().equals(other.powerId())
                    && this.slot().equals(other.slot());
            }

            else {
                return false;
            }

        }

        @Override
        public int hashCode() {
            return Objects.hash(powerId, slot);
        }

    }

    public static Builder builder() {
        return builder(DEFAULT);
    }

    public static Builder builder(ItemPowersComponent baseItemPowers) {
        return new Builder(baseItemPowers);
    }

    public static class Builder {

        private final ObjectLinkedOpenHashSet<Entry> entries = new ObjectLinkedOpenHashSet<>();

        private Builder(ItemPowersComponent baseItemPowers) {
            this.entries.addAll(baseItemPowers.entries);
        }

        public Builder add(EnumSet<AttributeModifierSlot> slots, Identifier powerId, boolean hidden, boolean negative) {

            NbtCompound entryNbt = new NbtCompound();
            for (AttributeModifierSlot slot : slots) {

                entryNbt.putString("slot", slot.asString());
                entryNbt.putString("power", powerId.toString());
                entryNbt.putBoolean("hidden", hidden);
                entryNbt.putBoolean("negative", negative);

                Entry.MAP_CODEC.codec().parse(NbtOps.INSTANCE, entryNbt)
                    .resultOrPartial(err -> Apoli.LOGGER.warn("Cannot add element ({}) as an item power entry: {}", entryNbt, err))
                    .ifPresent(entries::add);

            }

            return this;

        }

        public Builder remove(EnumSet<AttributeModifierSlot> slots, Identifier powerId) {
            return remove(slots, powerId, modifierSlot -> {});
        }

        public Builder remove(EnumSet<AttributeModifierSlot> slots, Identifier powerId, Consumer<Collection<Entry>> removalCallback) {

            ObjectListIterator<Entry> entryIterator = entries.iterator();
            ObjectLinkedOpenHashSet<Entry> removedEntries = new ObjectLinkedOpenHashSet<>();

            while (entryIterator.hasNext()) {

                Entry entry = entryIterator.next();

                if (entry.powerId().equals(powerId) && slots.contains(entry.slot())) {
                    removedEntries.add(entry);
                    entryIterator.remove();
                }

            }

            if (!removedEntries.isEmpty()) {
                removalCallback.accept(removedEntries);
            }

            return this;

        }

        public Builder remove(Predicate<Entry> entryPredicate) {
            entries.removeIf(entryPredicate);
            return this;
        }

        public ItemPowersComponent build() {
            return !entries.isEmpty()
                ? new ItemPowersComponent(entries)
                : DEFAULT;
        }

    }

}
