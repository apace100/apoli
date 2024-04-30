package io.github.apace100.apoli.util;

import com.mojang.datafixers.util.Either;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.HudRendered;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import io.github.apace100.apoli.util.transformer.Transform;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class HudRender implements Comparable<HudRender> {

    public static final Either<Identifier, SpriteData> DEFAULT_SPRITE_LOCATION = Either.left(Apoli.identifier("textures/gui/resource_bar.png"));
    public static final HudRender DONT_RENDER = new HudRender(false, 0, 0, DEFAULT_SPRITE_LOCATION, List.of(), new Vector2i(), null, false, 0);
    public static final Identifier ATLAS_TEXTURE = TextureUtil.GUI_ATLAS_TEXTURE;

    public static final int BAR_WIDTH = 71;
    public static final int BAR_HEIGHT = 8;

    public static final int ICON_WIDTH = 8;
    public static final int ICON_HEIGHT = 8;

    public static final int BAR_HORIZONTAL_INDEX_OFFSET = BAR_HEIGHT + 2;
    public static final int BAR_VERTICAL_INDEX_OFFSET = BAR_WIDTH + 2;

    public static final int ICON_HORIZONTAL_INDEX_OFFSET = ICON_WIDTH + 1;

    private final List<HudRender> children = new LinkedList<>();

    private final Either<Identifier, SpriteData> spriteLocation;
    private final List<Transform> transforms;
    private final Vector2ic position;
    private final Predicate<Entity> condition;

    private final boolean shouldRender;
    private final boolean inverted;

    private final int barIndex;
    private final int iconIndex;

    private boolean init = true;

    private boolean invalidLegacy;
    private boolean invalidBackground;
    private boolean invalidBase;
    private boolean invalidIcon;

    private int order;

    public HudRender(boolean shouldRender, int barIndex, int iconIndex, Either<Identifier, SpriteData> spriteLocation, List<Transform> transforms, Vector2ic position, Predicate<Entity> condition, boolean inverted, int order) {
        this.shouldRender = shouldRender;
        this.barIndex = barIndex;
        this.iconIndex = iconIndex;
        this.spriteLocation = spriteLocation;
        this.transforms = transforms;
        this.position = position;
        this.condition = condition;
        this.inverted = inverted;
        this.order = order;
    }

    @Override
    public int compareTo(@NotNull HudRender other) {
        return Integer.compare(this.order, other.order);
    }

    public Either<Identifier, SpriteData> getSpriteLocation() {
        return spriteLocation;
    }

    public List<Transform> getTransforms() {
        return transforms;
    }

    public Vector2ic getPosition() {
        return position;
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

    public HudRender getChildOrSelf(Entity viewer) {

        if (this.shouldRender(viewer)) {
            return this;
        }

        return children
            .stream()
            .filter(hudRender -> hudRender.shouldRender(viewer))
            .findFirst()
            .orElse(DONT_RENDER);

    }

    public void reset() {
        this.init = true;
        this.children.forEach(HudRender::reset);
    }

    @Environment(EnvType.CLIENT)
    public boolean render(HudRendered source, DrawContext context, int x, int y, float tickDelta) {

        if (init) {

            String sourceStr = source instanceof Power power
                ? "power \"" + power.getType().getIdentifier() + "\""
                : source.toString();
            this.init = false;

            this.spriteLocation
                .ifLeft(id -> {

                    this.invalidLegacy = !TextureUtil.isTextureLoaded(id);

                    if (invalidLegacy) {
                        Apoli.LOGGER.warn("HUD render from {} references sprite sheet texture \"{}\", which doesn't exist!", sourceStr, id);
                    }

                })
                .ifRight(spriteData -> {

                    this.invalidBackground = !TextureUtil.isSpriteLoaded(spriteData.backgroundId(), ATLAS_TEXTURE);
                    this.invalidBase = !TextureUtil.isSpriteLoaded(spriteData.baseId(), ATLAS_TEXTURE);
                    this.invalidIcon = !TextureUtil.isSpriteLoaded(spriteData.iconId(), ATLAS_TEXTURE);

                    if (invalidBackground) {
                        Apoli.LOGGER.warn("HUD render from {} references background sprite texture \"{}\", which doesn't exist!", sourceStr, spriteData.backgroundId());
                    }

                    if (invalidBase) {
                        Apoli.LOGGER.warn("HUD render from {} references base sprite texture \"{}\", which doesn't exist!", sourceStr, spriteData.baseId());
                    }

                    if (invalidIcon) {
                        Apoli.LOGGER.warn("HUD render from {} references icon sprite texture \"{}\", which doesn't exist!", sourceStr, spriteData.iconId());
                    }

                });

        }

        MatrixStack matrices = context.getMatrices();
        int barFillWidth = (int) ((inverted ? 1.0F - source.getFill() : source.getFill()) * BAR_WIDTH);

        int dx = x + position.x();
        int dy = y + position.y();

        return spriteLocation.map(
            id -> {

                if (invalidLegacy) {
                    return false;
                }

                matrices.push();
                transforms.forEach(trfm -> trfm.apply(matrices, dx, dy, tickDelta));

                int barV = BAR_HEIGHT + barIndex * BAR_HORIZONTAL_INDEX_OFFSET;
                int iconU = BAR_VERTICAL_INDEX_OFFSET + iconIndex * ICON_HORIZONTAL_INDEX_OFFSET;

                context.drawTexture(id, dx, dy, 0, 0, BAR_WIDTH, 5);
                context.drawTexture(id, dx, dy - 2, 0, barV, barFillWidth, BAR_HEIGHT);

                context.drawTexture(id, dx - ICON_WIDTH - 2, dy - 2, iconU, barV, ICON_WIDTH, ICON_HEIGHT);

                matrices.pop();
                return true;

            },
            spriteData -> {

                matrices.push();
                transforms.forEach(trfm -> trfm.apply(matrices, dx, dy, tickDelta));

                if (!invalidBackground) {
                    context.drawGuiTexture(spriteData.backgroundId(), dx, dy - 2, BAR_WIDTH, BAR_HEIGHT);
                }

                if (!invalidBase) {
                    context.drawGuiTexture(spriteData.baseId(), BAR_WIDTH, BAR_HEIGHT, 0, 0, dx, dy - 2, barFillWidth, BAR_HEIGHT);
                }

                if (!invalidIcon) {
                    context.drawGuiTexture(spriteData.iconId(), dx - ICON_WIDTH - 2, dy - 2, ICON_WIDTH, ICON_HEIGHT);
                }

                matrices.pop();
                return !invalidBackground
                    || !invalidBase
                    || !invalidIcon;

            }
        );

    }

    public record SpriteData(Identifier backgroundId, Identifier baseId, Identifier iconId) {

        public static final SerializableData DATA = new SerializableData()
            .add("background", SerializableDataTypes.IDENTIFIER)
            .add("base", SerializableDataTypes.IDENTIFIER)
            .add("icon", SerializableDataTypes.IDENTIFIER);

        public static final SerializableDataType<SpriteData> DATA_TYPE = SerializableDataType.compound(
            SpriteData.class,
            DATA,
            SpriteData::fromData,
            (serializableData, spriteData) -> spriteData.toData(serializableData)
        );

        public SerializableData.Instance toData(SerializableData serializableData) {

            SerializableData.Instance data = serializableData.new Instance();

            data.set("background", this.backgroundId());
            data.set("base", this.baseId());
            data.set("icon", this.iconId());

            return data;

        }

        public static SpriteData fromData(SerializableData.Instance data) {
            return new SpriteData(data.get("background"), data.get("base"), data.get("icon"));
        }

    }

}
