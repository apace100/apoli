package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MultiplePower extends Power {

	public static final List<Pattern> SUB_POWER_KEY_FILTERS = Util.make(new ObjectArrayList<>(), filters -> {

		Power.SERIALIZABLE_DATA.keys(JavaOps.INSTANCE)
			.map(Object::toString)
			.distinct()
			.map(Pattern::compile)
			.forEach(filters::add);

		filters.add(Pattern.compile("^\\$"));
		filters.add(Pattern.compile(Power.TYPE_KEY));
		filters.add(Pattern.compile(ResourceConditions.CONDITIONS_KEY));

	});

    protected static final Function<Power, PacketCodec<RegistryByteBuf, MultiplePower>> PACKET_CODEC = power -> new PacketCodec<>() {

		@Override
		public MultiplePower decode(RegistryByteBuf buf) {
			Set<Identifier> subPowerIds = buf.readCollection(ObjectLinkedOpenHashSet::new, PacketByteBuf::readIdentifier);
			return new MultiplePower(power, subPowerIds);

		}

		@Override
		public void encode(RegistryByteBuf buf, MultiplePower value) {
			buf.writeCollection(value.getSubPowerIds(), PacketByteBuf::writeIdentifier);
		}

	};

    private ImmutableSet<Identifier> subPowerIds;

    MultiplePower(Power basePower, Set<Identifier> subPowerIds) {
        super(basePower);
        this.subPowerIds = ImmutableSet.copyOf(subPowerIds);
    }

    MultiplePower(Power basePower) {
        super(basePower);
		this.subPowerIds = ImmutableSet.of();
    }

    public ImmutableSet<Identifier> getSubPowerIds() {
        return subPowerIds;
    }

    void setSubPowerIds(Set<Identifier> subPowerIds) {
        this.subPowerIds = ImmutableSet.copyOf(subPowerIds);
    }

    public Set<SubPower> getSubPowers() {
        return this.getSubPowerIds()
            .stream()
            .filter(PowerManager::contains)
            .map(PowerManager::get)
            .filter(SubPower.class::isInstance)
            .map(SubPower.class::cast)
            .collect(Collectors.toCollection(HashSet::new));
    }

	public static DataResult<String> validateSubPowerName(String name) {

		if (name.isEmpty()) {
			return DataResult.error(() -> "Empty sub-power names are not allowed!");
		}

		else if (Identifier.isPathValid(name)) {
			return DataResult.success(name);
		}

		else {
			DataResult<String> result = DataResult.error(() -> "Non [a-z0-9/._-] character(s) in sub-power name: " + name);
			return result.setPartial(name);
		}

	}

	public static boolean isKeyIgnored(String key) {
		return SUB_POWER_KEY_FILTERS
			.stream()
			.anyMatch(filter -> filter.matcher(key).find());
	}

}
