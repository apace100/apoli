package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.type.GameEventListenerPowerType;

import java.util.Optional;

public interface PowerLinkedListenerData {
    Optional<GameEventListenerPowerType> apoli$getPower();
    void apoli$setPower(GameEventListenerPowerType power);
}
