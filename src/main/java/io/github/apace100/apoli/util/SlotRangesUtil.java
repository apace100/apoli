package io.github.apace100.apoli.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.apace100.apoli.mixin.SlotRangesAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.SlotRange;
import net.minecraft.inventory.SlotRanges;

import java.util.List;
import java.util.Objects;

public class SlotRangesUtil {

	private static final Codec<SlotRange> SINGLE_BY_INDEX_CODEC = Codec.INT.flatXmap(
		id -> {

			List<SlotRange> slotRanges = SlotRangesAccessor.getSlotRanges();

			for (SlotRange slotRange : slotRanges) {

				IntList slotIds = slotRange.getSlotIds();

				if (slotIds.size() == 1 && Objects.equals(slotIds.getFirst(), id)) {
					return DataResult.success(slotRange);
				}

			}

			return DataResult.error(() -> "Single slot range with ID  \"" + id + "\" is undefined!");

		},
		slotRange -> {
			int index = SlotRangesAccessor.getSlotRanges().lastIndexOf(slotRange);
			return index == -1
				? DataResult.error(() -> "Unknown slot range \"" + slotRange.asString() + "\"!")
				: DataResult.success(index);
		}
	);

	public static final Codec<SlotRange> SINGLE_SLOT_CODEC = new Codec<>() {

		@Override
		public <T> DataResult<Pair<SlotRange, T>> decode(DynamicOps<T> ops, T input) {

			if (ops.getNumberValue(input).isSuccess()) {
				return SINGLE_BY_INDEX_CODEC.decode(ops, input);
			}

			else {
				return SlotRanges.CODEC.parse(ops, input)
					.flatMap(SlotRangesUtil::validateSingleSlot)
					.map(slotRange -> Pair.of(slotRange, input));
			}

		}

		@Override
		public <T> DataResult<T> encode(SlotRange input, DynamicOps<T> ops, T prefix) {
			return DataResult.success(ops.createString(input.asString()));
		}

	};

	public static DataResult<SlotRange> validateSingleSlot(SlotRange slotRange) {
		return slotRange.getSlotCount() == 1
			? DataResult.success(slotRange)
			: DataResult.error(() -> "Slot range \"" + slotRange + "\" has multiple slot IDs, which is not allowed!");
	}

}
