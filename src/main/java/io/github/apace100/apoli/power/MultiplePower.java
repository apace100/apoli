package io.github.apace100.apoli.power;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MultiplePower extends Power {

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

}
