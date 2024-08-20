package io.github.apace100.apoli.power.type;

import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.factory.PowerTypeFactory;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLong;

public class CooldownPowerType extends PowerType implements HudRendered {

    protected long lastUseTime;

    public final int cooldownDuration;
    private final HudRender hudRender;

    public CooldownPowerType(Power power, LivingEntity entity, int cooldownDuration, HudRender hudRender) {
        super(power, entity);
        this.cooldownDuration = cooldownDuration;
        this.hudRender = hudRender;
    }

    public boolean canUse() {
        return entity.getEntityWorld().getTime() >= lastUseTime + cooldownDuration && isActive();
    }

    public void use() {
        lastUseTime = entity.getEntityWorld().getTime();
        PowerHolderComponent.syncPower(entity, this.power);
    }

    public float getProgress() {
        float time = entity.getEntityWorld().getTime() - lastUseTime;
        return Math.min(1F, Math.max(time / (float)cooldownDuration, 0F));
    }

    public int getRemainingTicks() {
        return (int)Math.max(0, cooldownDuration - (entity.getEntityWorld().getTime() - lastUseTime));
    }

    public void modify(int changeInTicks){
        this.lastUseTime += changeInTicks;
        long currentTime = entity.getEntityWorld().getTime();
        if(this.lastUseTime > currentTime) {
            lastUseTime = currentTime;
        }
    }

    public void setCooldown(int cooldownInTicks) {
        long currentTime = entity.getEntityWorld().getTime();
        this.lastUseTime = currentTime - Math.min(cooldownInTicks, cooldownDuration);
    }

    @Override
    public NbtElement toTag() {
        return NbtLong.of(lastUseTime);
    }

    @Override
    public void fromTag(NbtElement tag) {
        lastUseTime = ((NbtLong)tag).longValue();
    }

    @Override
    public HudRender getRenderSettings() {
        return hudRender;
    }

    @Override
    public float getFill() {
        return getProgress();
    }

    @Override
    public boolean shouldRender() {
        return (entity.getEntityWorld().getTime() - lastUseTime) <= cooldownDuration;
    }

    public static PowerTypeFactory<?> getFactory() {
        return new PowerTypeFactory<>(
            Apoli.identifier("cooldown"),
            new SerializableData()
                .add("cooldown", SerializableDataTypes.INT)
                .add("hud_render", ApoliDataTypes.HUD_RENDER, HudRender.DONT_RENDER),
            data -> (power, entity) -> new CooldownPowerType(power, entity,
                data.get("cooldown"),
                data.get("hud_render")
            )
        ).allowCondition();
    }

}
