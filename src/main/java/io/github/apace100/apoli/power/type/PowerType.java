package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.condition.EntityCondition;
import io.github.apace100.apoli.data.TypedDataObjectFactory;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerConfiguration;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.util.Validatable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

public abstract class PowerType implements Validatable {

    protected final Optional<EntityCondition> condition;

    private LivingEntity holder;
    private Power power;

    private boolean ticking;
    private boolean tickingWhenInActive;

    private boolean initialized;

    public PowerType() {
        this(Optional.empty());
    }

    public PowerType(Optional<EntityCondition> condition) {
        this.condition = condition;
    }

	@Override
    public void validate() throws Exception {

		//noinspection unchecked
		TypedDataObjectFactory<PowerType> dataFactory = (TypedDataObjectFactory<PowerType>) getConfig().dataFactory();
        SerializableData.Instance data = dataFactory.toData(this);

        data.validate();

    }

    @NotNull
    public abstract PowerConfiguration<?> getConfig();

    @ApiStatus.Internal
    public final void init(@NotNull final LivingEntity holder, @NotNull final Power power) {

        if (power.getPowerType() != this) {
            throw new IllegalArgumentException("Cannot initialize power type with a mismatched power!");
        }

        this.holder = holder;
        this.power = power;

        this.initialized = true;
        onInit();

    }

    public final Power getPower() {

        if (initialized) {
            return Objects.requireNonNull(power, "Power of initialized power type \"" + getConfig().id() + "\" was null!");
        }

        else {
            throw new IllegalStateException("Power type \"" + getConfig().id() + "\" wasn't initialized yet!");
        }

    }

    public final LivingEntity getHolder() {

        if (initialized) {
            return Objects.requireNonNull(holder, "Holder of initialized power type \"" + getConfig().id() + "\" was null!");
        }

        else {
            throw new IllegalStateException("Power type \"" + getConfig().id() + "\" wasn't initialized yet!");
        }

    }

    public void serverTick() {

    }

    public void clientTick() {

    }

    public void commonTick() {

    }

    public void onInit() {

    }

    public void onGained() {

    }

    public void onLost() {

    }

    public void onAdded() {

    }

    public void onRemoved() {

    }

    public void onRespawn() {

    }

    public boolean isActive() {
        return isInitialized() && condition
            .map(condition -> condition.test(getHolder()))
            .orElse(true);
    }

    public NbtElement toTag() {
        return new NbtCompound();
    }

    public void fromTag(NbtElement tag) {

    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean shouldTick() {
        return ticking;
    }

    public boolean shouldTickWhenInactive() {
        return tickingWhenInActive;
    }

    public final void setTicking() {
        setTicking(false);
    }

    public final void setTicking(boolean whenInActive) {
        this.ticking = true;
        this.tickingWhenInActive = whenInActive;
    }

    public static <T extends PowerType> TypedDataObjectFactory<T> createConditionedDataFactory(SerializableData serializableData, BiFunction<SerializableData.Instance, Optional<EntityCondition>, T> fromData, BiFunction<T, SerializableData, SerializableData.Instance> toData) {
        return TypedDataObjectFactory.simple(
            serializableData
                .add("condition", EntityCondition.DATA_TYPE.optional(), Optional.empty()),
            data -> fromData.apply(
                data,
                data.get("condition")
            ),
            (t, _serializableData) -> toData.apply(t, _serializableData)
                .set("condition", t.condition)
        );
    }

}
