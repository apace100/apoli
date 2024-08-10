package io.github.apace100.apoli.util.hud_render;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Optional;

public class ParentHudRender extends HudRender {

    private final ImmutableList<HudRender> children;

    protected ParentHudRender(Collection<HudRender> children, ConditionTypeFactory<Entity>.Instance condition, Identifier spriteLocation, boolean shouldRender, boolean inverted, int barIndex, int iconIndex, int order) {
        super(condition, spriteLocation, shouldRender, inverted, barIndex, iconIndex, order);

        ImmutableList.Builder<HudRender> childrenBuilder = ImmutableList.builder();

        childrenBuilder.add(this);
        children
            .stream()
            .map(child -> child.withOrder(order))
            .forEach(childrenBuilder::add);

        this.children = childrenBuilder.build();

    }

    public ParentHudRender(HudRender parent, Collection<HudRender> children) {
        this(children, parent.getCondition(), parent.getSpriteLocation(), parent.shouldRender(), parent.isInverted(), parent.getBarIndex(), parent.getIconIndex(), parent.getOrder());
    }

    @Override
    public void validate() throws Exception {

        for (HudRender child : children) {
            STRICT_DATA_TYPE.toData(child).validate();
        }

    }

    @Override
    public Optional<HudRender> getActive(Entity viewer) {
        return children.stream()
            .filter(child -> child.shouldRender(viewer))
            .findFirst();
    }

    public ImmutableList<HudRender> getChildren() {
        return children;
    }

}
