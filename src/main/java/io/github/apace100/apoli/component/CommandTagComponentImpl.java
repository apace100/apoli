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

    private final Set<String> commandTags;
    private final Entity provider;

    public CommandTagComponentImpl(Entity provider) {
        this.commandTags = new HashSet<>();
        this.provider = provider;
    }

    @Override
    public Set<String> getTags() {
        return commandTags;
    }

    @Override
    public void setTags(Set<String> commandTags) {
        this.commandTags.clear();
        this.commandTags.addAll(commandTags);
    }

    @Override
    public boolean addTag(String commandTag) {
        return this.commandTags.add(commandTag);
    }

    @Override
    public boolean removeTag(String commandTag) {
        return this.commandTags.remove(commandTag);
    }

    @Override
    public void loadServerside() {

        Set<String> originalCommandTags = ((EntityAccessor) provider).getOriginalCommandTags();

        if (!commandTags.equals(originalCommandTags)) {
            this.setTags(originalCommandTags);
            CommandTagComponent.KEY.sync(provider);
        }

    }

    @Override
    public void readFromNbt(NbtCompound tag) {

        NbtList commandTagsNbt = tag.getList("Tags", NbtElement.STRING_TYPE);
        this.commandTags.clear();

        for (int i = 0; i < commandTagsNbt.size(); ++i) {
            this.commandTags.add(commandTagsNbt.getString(i));
        }

    }

    @Override
    public void writeToNbt(NbtCompound tag) {

        NbtList commandTagsNbt = new NbtList();
        this.commandTags.stream()
            .map(NbtString::of)
            .forEach(commandTagsNbt::add);

        tag.put("Tags", commandTagsNbt);

    }

}
