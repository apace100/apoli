package io.github.apace100.apoli.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.apace100.apoli.Apoli;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@SuppressWarnings("UnusedReturnValue")
public interface CommandTagComponent extends AutoSyncedComponent {

    ComponentKey<CommandTagComponent> KEY = ComponentRegistry.getOrCreate(Apoli.identifier("command_tags"), CommandTagComponent.class);

    Set<String> getCommandTags();
    @ApiStatus.Internal
    void setCommandTags(Set<String> commandTags);

    @ApiStatus.Internal
    boolean addCommandTag(String commandTag, boolean originallyAdded);
    @ApiStatus.Internal
    boolean removeCommandTag(String commandTag);

    void sync(boolean force);

}
