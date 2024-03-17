package io.github.apace100.apoli.util;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class HudRender implements Comparable<HudRender> {

    public static final HudRender DONT_RENDER = new HudRender(false, 0, 0, Apoli.identifier("textures/gui/resource_bar.png"), null, false, 0);

    private final List<HudRender> children = new LinkedList<>();

    private final Identifier spriteLocation;
    private final Predicate<Entity> condition;

    private final boolean shouldRender;
    private final boolean inverted;
    private final int barIndex;
    private final int iconIndex;

    private int order;

    public HudRender(boolean shouldRender, int barIndex, int iconIndex, Identifier spriteLocation, Predicate<Entity> condition, boolean inverted, int order) {
        this.shouldRender = shouldRender;
        this.barIndex = barIndex;
        this.iconIndex = iconIndex;
        this.spriteLocation = spriteLocation;
        this.condition = condition;
        this.inverted = inverted;
        this.order = order;
    }

    @Override
    public int compareTo(@NotNull HudRender other) {
        int orderResult = Integer.compare(this.order, other.order);
        return orderResult != 0 ? orderResult : this.spriteLocation.compareTo(other.spriteLocation);
    }

    public Identifier getSpriteLocation() {
        return spriteLocation;
    }

    public int getBarIndex() {
        return barIndex;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public boolean isInverted() {
        return inverted;
    }

    public boolean shouldRender() {
        return shouldRender;
    }

    public boolean shouldRender(Entity viewer) {
        return shouldRender && (condition == null || condition.test(viewer));
    }

    public Predicate<Entity> getCondition() {
        return condition;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void send(PacketByteBuf buffer) {
        ApoliDataTypes.SINGLE_HUD_RENDER.send(buffer, this);
        ApoliDataTypes.MULTIPLE_HUD_RENDERS.send(buffer, children);
    }

    public static HudRender receive(PacketByteBuf buffer) {

        HudRender parentHudRender = ApoliDataTypes.SINGLE_HUD_RENDER.receive(buffer);
        ApoliDataTypes.MULTIPLE_HUD_RENDERS.receive(buffer).forEach(parentHudRender::addChild);

        return parentHudRender;

    }

    public void addChild(HudRender hudRender) {

        if (this == hudRender) {
            return;
        }

        if (hudRender.getOrder() == 0) {
            hudRender.setOrder(this.order);
        }

        this.children.add(hudRender);

    }

    public Optional<HudRender> getChildOrSelf(Entity viewer) {

        if (this.shouldRender(viewer)) {
            return Optional.of(this);
        }

        return children
            .stream()
            .filter(hudRender -> hudRender.shouldRender(viewer))
            .findFirst();

    }

}
