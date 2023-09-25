package io.github.apace100.apoli.access;

import io.github.apace100.apoli.power.GameEventListenerPower;

import java.util.Optional;

public interface PowerLinkedListenerData {
    Optional<GameEventListenerPower> apoli$getPower();
    void apoli$setPower(GameEventListenerPower power);
}
