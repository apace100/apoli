package io.github.apace100.apoli.component;

import io.github.apace100.apoli.mixin.EntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.HashSet;
import java.util.Set;

public class CommandTagComponentImpl implements CommandTagComponent {

    private final Set<String> syncedCommandTags;
    private final Entity entity;

    private boolean dirty;

    public CommandTagComponentImpl(Entity entity) {
        this.syncedCommandTags = new HashSet<>();
        this.entity = entity;
    }

    @Override
    public Set<String> getCommandTags() {
        return syncedCommandTags;
    }

    @Override
    public void setCommandTags(Set<String> commandTags) {

        if (this.syncedCommandTags.equals(commandTags)) {
            return;
        }

        this.syncedCommandTags.clear();
        this.syncedCommandTags.addAll(commandTags);

        this.dirty = true;

    }

    @Override
    public boolean addCommandTag(String commandTag, boolean originallyAdded) {
        return originallyAdded
            && this.syncedCommandTags.add(commandTag)
            && (this.dirty = true);
    }

    @Override
    public boolean removeCommandTag(String commandTag) {
        return this.syncedCommandTags.remove(commandTag)
            && (this.dirty = true);
    }

    @Override
    public void sync(boolean force) {
        if (force || dirty) {
            this.dirty = false;
            CommandTagComponent.KEY.sync(entity);
        }
    }

    @Override
    public void readFromNbt(NbtCompound tag) {

        NbtList commandTagsNbt = tag.getList("Tags", NbtElement.STRING_TYPE);
        this.syncedCommandTags.clear();

        for (int i = 0; i < commandTagsNbt.size(); i++) {
            this.syncedCommandTags.add(commandTagsNbt.getString(i));
        }

    }

    @Override
    public void writeToNbt(NbtCompound tag) {

        NbtList commandTagsNbt = new NbtList();
        ((EntityAccessor) entity).getOriginalCommandTags()
            .stream()
            .map(NbtString::of)
            .forEach(commandTagsNbt::add);

        tag.put("Tags", commandTagsNbt);

    }

}
