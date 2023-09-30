package io.github.apace100.apoli.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class HudRender {

    public static final HudRender DONT_RENDER = new HudRender(false, 0, 0, Apoli.identifier("textures/gui/resource_bar.png"), null, false, 0);

    private final List<Inner> innerHudRenderList = new ArrayList<>();
    private final int order;

    public HudRender(int order) {
        this.order = order;
    }

    public HudRender(boolean shouldRender, int barIndex, int iconIndex, Identifier spriteLocation, ConditionFactory<LivingEntity>.Instance condition, boolean inverted, int order) {
        this.order = order;
        addRender(new Inner(shouldRender, barIndex, iconIndex, spriteLocation, condition, inverted));
    }

    public int order() {
        return this.order;
    }

    public void send(PacketByteBuf buf) {
        buf.writeInt(this.order());
        ApoliDataTypes.INNER_HUD_RENDERS.send(buf, innerHudRenderList);
    }

    public static HudRender receive(PacketByteBuf buf) {
        HudRender hudRender = new HudRender(buf.readInt());
        ApoliDataTypes.INNER_HUD_RENDERS.receive(buf).forEach(hudRender::addRender);
        return hudRender;
    }

    public static HudRender read(JsonElement json) {
        HudRender hudRender;
        if (json.isJsonObject() && json.getAsJsonObject().has("order")) {
            int order = JsonHelper.getInt((JsonObject) json, "order", 0);
            hudRender = new HudRender(order);
            if (!((JsonObject) json).has("hud_render")) {
                throw new RuntimeException("Hud render with `order` field does not have a `hud_render` field. No hud renders will be present.");
            }
            ApoliDataTypes.INNER_HUD_RENDERS.read(((JsonObject) json).get("hud_render")).forEach(hudRender::addRender);
            return hudRender;
        } else {
            hudRender = new HudRender(0);
            ApoliDataTypes.INNER_HUD_RENDERS.read(json).forEach(hudRender::addRender);
        }
        return hudRender;
    }

    public void addRender(Inner innerHudRender) {
        innerHudRenderList.add(innerHudRender);
    }

    public Stream<Inner> filter() {
        return innerHudRenderList.stream().filter(Inner::shouldRender);
    }

    public Stream<Inner> filter(PlayerEntity player) {
        return innerHudRenderList.stream().filter(render -> render.shouldRender() && (render.playerCondition() == null || render.playerCondition().test(player)));
    }

    public record Inner(boolean shouldRender, int barIndex, int iconIndex, Identifier spriteLocation, ConditionFactory<LivingEntity>.Instance playerCondition, boolean inverted) {}
}
