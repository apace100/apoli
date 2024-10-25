package io.github.apace100.apoli.util;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.inventory.SlotRange;
import net.minecraft.item.ItemStack;

public record IndexedStack(ItemStack stack, SlotRange slot) {

	public IndexedStack {
		SlotRangesUtil.validateSingleSlot(slot).getOrThrow(IllegalArgumentException::new);
	}

	public static final CompoundSerializableDataType<IndexedStack> DATA_TYPE = SerializableDataType.compound(
		SerializableDataTypes.ITEM_STACK.serializableData().copy()
			.add("slot", ApoliDataTypes.SINGLE_SLOT_RANGE),
		data -> new IndexedStack(
			SerializableDataTypes.ITEM_STACK_OBJ_FACTORY.fromData(data),
			data.get("slot")
		),
		(indexedStack, serializableData) -> SerializableDataTypes.ITEM_STACK_OBJ_FACTORY.toData(indexedStack.stack(), serializableData)
			.set("slot", indexedStack.slot())
	);

	public int slotId() {
		return slot.getSlotIds().getFirst();
	}

}
