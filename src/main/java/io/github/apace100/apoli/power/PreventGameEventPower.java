package io.github.apace100.apoli.power;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import net.minecraft.world.event.GameEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PreventGameEventPower extends Power {

    private final Tag<GameEvent> tag;
    private final List<GameEvent> list;
    private final Consumer<Entity> entityAction;

    public PreventGameEventPower(PowerType<?> type, LivingEntity entity, Tag<GameEvent> tag, List<GameEvent> list, Consumer<Entity> entityAction) {
        super(type, entity);
        this.tag = tag;
        this.list = list;
        this.entityAction = entityAction;
    }

    public void executeAction(Entity entity) {
        if(entityAction != null) {
            entityAction.accept(entity);
        }
    }

    public boolean doesPrevent(GameEvent event) {
        if(tag != null && tag.contains(event)) {
            return true;
        }
        if(list != null && list.contains(event)) {
            return true;
        }
        return false;
    }
}
