package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;

/** A guard dog that patrols back and forth on the ground between two x positions. */
public class Dog {

    private static final float DOG_W = 96f;
    private static final float DOG_H = 64f;

    private final Rectangle bounds;
    private final float minX;
    private final float maxX;
    private final float speed;
    private int direction = 1;

    /** Places a dog at {@code x}, patrolling between {@code minX} and {@code maxX} at {@code speed}. */
    public Dog(float x, float minX, float maxX, float speed) {
        bounds = new Rectangle(x, MyGdxGame.GROUND_Y, DOG_W, DOG_H);
        this.minX = minX;
        this.maxX = maxX;
        this.speed = speed;
    }

    /** Walks for one frame and turns around at either end of the patrol. */
    public void update(float delta) {
        bounds.x += speed * direction * delta;

        if (bounds.x <= minX) {
            bounds.x = minX;
            direction = 1;
        }

        if (bounds.x >= maxX) {
            bounds.x = maxX;
            direction = -1;
        }
    }

    public Rectangle getBounds() {
        return bounds;
    }
}
