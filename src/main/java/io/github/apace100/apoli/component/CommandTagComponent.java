package io.github.apace100.apoli.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.load.ServerLoadAwareComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.apace100.apoli.Apoli;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

public interface CommandTagComponent extends AutoSyncedComponent, ServerLoadAwareComponent {

    ComponentKey<CommandTagComponent> KEY = ComponentRegistry.getOrCreate(Apoli.identifier("command_tags"), CommandTagComponent.class);

    Set<String> getTags();

    @ApiStatus.Internal
    void setTags(Set<String> commandTags);

    @ApiStatus.Internal
    boolean addTag(String commandTag);

    @ApiStatus.Internal
    boolean removeTag(String commandTag);

}
