package com.mygdx.game;

/** How the cat behaves in one of its forms: its size, its speed, and what can stop or hurt it. */
public interface PlayerForm {

    /** Horizontal speed in pixels per second. */
    float speed();

    /** Hitbox width in this form. */
    float width();

    /** Hitbox height in this form. */
    float height();

    /** True if walls block this form and it can stand on top of them. */
    boolean isBlockedByWalls();

    /** True if this form can jump. */
    boolean canJump();

    /** True if touching a dog costs a life in this form. */
    boolean isHurtByDogs();
}
