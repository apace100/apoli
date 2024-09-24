package io.github.apace100.apoli.util.hud_render;

import com.google.common.collect.ImmutableList;
import io.github.apace100.apoli.util.HudRender;
import net.minecraft.entity.Entity;

import java.util.Collection;
import java.util.Optional;

public class ParentHudRender extends HudRender {

    private final ImmutableList<HudRender> children;

    public ParentHudRender(HudRender parent, Collection<HudRender> children) {
        super(parent.getCondition(), parent.getSpriteLocation(), parent.shouldRender(), parent.isInverted(), parent.getBarIndex(), parent.getIconIndex(), parent.getOrder());

        ImmutableList.Builder<HudRender> childrenBuilder = ImmutableList.builder();
        childrenBuilder.add(parent);

        children.stream()
            .map(child -> child.withOrder(parent.getOrder()))
            .forEach(childrenBuilder::add);

        this.children = childrenBuilder.build();

    }

    @Override
    public void validate() throws Exception {

        for (HudRender child : children) {
            child.validate();
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
