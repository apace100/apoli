package io.github.apace100.apoli.access;

public interface MovingEntity {

    boolean apoli$isMoving();

    boolean apoli$isMovingVertically();
    boolean apoli$isMovingHorizontally();

    double apoli$getVerticalMovementValue();
    double apoli$getHorizontalMovementValue();

}
