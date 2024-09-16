package io.github.apace100.apoli.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.util.hud_render.ParentHudRender;
import io.github.apace100.calio.data.CompoundSerializableDataType;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.Validatable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.NullOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class HudRender implements Comparable<HudRender>, Validatable {

    public static final Identifier DEFAULT_SPRITE = Apoli.identifier("textures/gui/resource_bar.png");
    public static final HudRender DONT_RENDER = new HudRender(null, DEFAULT_SPRITE, false, false, 0, 0, 0);

    public static final CompoundSerializableDataType<HudRender> STRICT_DATA_TYPE = SerializableDataType.compound(
        new SerializableData()
            .add("condition", ApoliDataTypes.ENTITY_CONDITION, null)
            .add("sprite_location", SerializableDataTypes.IDENTIFIER, DEFAULT_SPRITE)
            .add("should_render", SerializableDataTypes.BOOLEAN, true)
            .add("inverted", SerializableDataTypes.BOOLEAN, false)
            .add("bar_index", SerializableDataTypes.NON_NEGATIVE_INT, 0)
            .add("icon_index", SerializableDataTypes.NON_NEGATIVE_INT, 0)
            .add("order", SerializableDataTypes.INT, 0),
        data -> new HudRender(
            data.get("condition"),
            data.getId("sprite_location"),
            data.getBoolean("should_render"),
            data.getBoolean("inverted"),
            data.getInt("bar_index"),
            data.getInt("icon_index"),
            data.getInt("order")
        ),
        (hudRender, serializableData) -> serializableData.instance()
            .set("condition", hudRender.getCondition())
            .set("sprite_location", hudRender.getSpriteLocation())
            .set("should_render", hudRender.shouldRender())
            .set("inverted", hudRender.isInverted())
            .set("bar_index", hudRender.getBarIndex())
            .set("icon_index", hudRender.getIconIndex())
            .set("order", hudRender.getOrder())
    );

    public static final SerializableDataType<List<HudRender>> LIST_DATA_TYPE = STRICT_DATA_TYPE.list(1, Integer.MAX_VALUE);

    public static final SerializableDataType<HudRender> DATA_TYPE = SerializableDataType.of(
        new Codec<>() {

            @Override
            public <T> DataResult<Pair<HudRender, T>> decode(DynamicOps<T> ops, T input) {
                return LIST_DATA_TYPE.codec().decode(ops, input)
                    .map(hudRendersAndInput -> hudRendersAndInput
                        .mapFirst(ObjectArrayList::new)
                        .mapFirst(hudRenders -> new ParentHudRender(hudRenders.removeFirst(), hudRenders)));
            }

            @Override
            public <T> DataResult<T> encode(HudRender input, DynamicOps<T> ops, T prefix) {

                if (input instanceof ParentHudRender parent) {
                    return LIST_DATA_TYPE.codec().encode(parent.getChildren(), ops, prefix);
                }

                else {
                    return STRICT_DATA_TYPE.codec().encode(input, ops, prefix);
                }

            }

        },
        new PacketCodec<>() {

            @Override
            public HudRender decode(RegistryByteBuf buf) {

                if (buf.readBoolean()) {

                    List<HudRender> children = new LinkedList<>(LIST_DATA_TYPE.packetCodec().decode(buf));
                    HudRender parent = children.removeFirst();

                    return new ParentHudRender(parent, children);

                }

                else {
                    return STRICT_DATA_TYPE.packetCodec().decode(buf);
                }

            }

            @Override
            public void encode(RegistryByteBuf buf, HudRender hudRender) {

                if (hudRender instanceof ParentHudRender parent) {
                    buf.writeBoolean(true);
                    LIST_DATA_TYPE.packetCodec().encode(buf, parent.getChildren());
                }

                else {
                    buf.writeBoolean(false);
                    STRICT_DATA_TYPE.packetCodec().encode(buf, hudRender);
                }

            }

        }
    );

    @Nullable
    private final ConditionTypeFactory<Entity>.Instance condition;
    private final Identifier spriteLocation;

    private final boolean shouldRender;
    private final boolean inverted;

    private final int barIndex;
    private final int iconIndex;
    private final int order;

    public HudRender(@Nullable ConditionTypeFactory<Entity>.Instance condition, Identifier spriteLocation, boolean shouldRender, boolean inverted, int barIndex, int iconIndex, int order) {
        this.condition = condition;
        this.spriteLocation = spriteLocation;
        this.shouldRender = shouldRender;
        this.inverted = inverted;
        this.barIndex = barIndex;
        this.iconIndex = iconIndex;
        this.order = order;
    }

    @Override
    public int compareTo(@NotNull HudRender other) {
        int orderResult = Integer.compare(this.getOrder(), other.getOrder());
        return orderResult != 0
            ? orderResult
            : this.getSpriteLocation().compareTo(other.getSpriteLocation());
    }

    @Override
    public void validate() throws Exception {
        STRICT_DATA_TYPE.toData(this, NullOps.INSTANCE).validate();
    }

    @Nullable
    public ConditionTypeFactory<Entity>.Instance getCondition() {
        return this.condition;
    }

    public Identifier getSpriteLocation() {
        return spriteLocation;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public boolean shouldRender(Entity viewer) {
        return this.shouldRender() && (this.getCondition() == null || this.getCondition().test(viewer));
    }

    public boolean isInverted() {
        return inverted;
    }

    public int getBarIndex() {
        return barIndex;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public int getOrder() {
        return order;
    }

    public HudRender withOrder(int order) {

        if (this.getOrder() != 0) {
            return this;
        }

        return new HudRender(
            this.getCondition(),
            this.getSpriteLocation(),
            this.shouldRender(),
            this.isInverted(),
            this.getBarIndex(),
            this.getIconIndex(),
            order
        );

    }

    public Optional<HudRender> getActive(Entity viewer) {

        if (this.shouldRender(viewer)) {
            return Optional.of(this);
        }

        else {
            return Optional.empty();
        }

    }

}
