package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/** The cat: its position, jump and gravity, collisions with walls and ground, and the timed liquid form. */
public class Player {

    static final float CAT_W = 76f;
    static final float CAT_H = 90f;
    static final float PUDDLE_W = 82f;
    static final float PUDDLE_H = 30f;

    private static final float NORMAL_SPEED = 280f;
    private static final float LIQUID_SPEED = 220f;
    private static final float GRAVITY = 1500f;
    private static final float JUMP_SPEED = 620f;
    private static final float START_X = 100f;

    // Long enough to reach the later tall wall.
    private static final float LIQUID_DURATION = 5f;

    private final Rectangle bounds = new Rectangle();
    private float velocityY = 0f;
    private float liquidTimer = 0f;
    private boolean isLiquidState = false;
    private boolean onGround = true;
    private boolean hasSpell = false;

    /** Puts the cat at the start of a freshly loaded stage, without the spell. */
    public void reset() {
        isLiquidState = false;
        hasSpell = false;
        liquidTimer = 0f;
        velocityY = 0f;
        onGround = true;

        bounds.set(START_X, MyGdxGame.GROUND_Y, CAT_W, CAT_H);
    }

    /** Puts the cat back at the start after losing a life; a collected spell is kept. */
    public void respawn() {
        bounds.set(START_X, MyGdxGame.GROUND_Y, CAT_W, CAT_H);

        velocityY = 0f;
        onGround = true;

        isLiquidState = false;
        liquidTimer = 0f;
    }

    /** Turns into liquid if the spell is owned and the cat is not liquid already. */
    public void tryBecomeLiquid() {
        if (hasSpell && !isLiquidState) {
            isLiquidState = true;
            liquidTimer = LIQUID_DURATION;

            velocityY = 0f;
            bounds.y = MyGdxGame.GROUND_Y;
            onGround = true;
        }
    }

    /** Horizontal speed of the current form, in pixels per second. */
    public float speed() {
        return isLiquidState ? LIQUID_SPEED : NORMAL_SPEED;
    }

    /** Sets the hitbox to the size of the current form. */
    public void applyFormSize() {
        bounds.width = isLiquidState ? PUDDLE_W : CAT_W;
        bounds.height = isLiquidState ? PUDDLE_H : CAT_H;
    }

    /** Moves by {@code moveX}, stopping at walls in normal form, and keeps the cat inside the stage. */
    public void moveHorizontally(float moveX, Array<Rectangle> walls, float levelWidth) {
        bounds.x += moveX;

        // Normal Cat is blocked by walls.
        // Liquid Cat can pass through every wall.
        if (!isLiquidState) {
            for (Rectangle wall : walls) {
                if (bounds.overlaps(wall)) {
                    if (moveX > 0f) {
                        bounds.x = wall.x - bounds.width;
                    } else if (moveX < 0f) {
                        bounds.x = wall.x + wall.width;
                    }
                }
            }
        }

        bounds.x = Math.max(
            0f,
            Math.min(bounds.x, levelWidth - bounds.width)
        );
    }

    /** Jumps if standing on something; jumping is only available in normal form. */
    public void tryJump() {
        if (onGround && !isLiquidState) {
            velocityY = JUMP_SPEED;
            onGround = false;
        }
    }

    /** Applies gravity for one frame and lands on top of walls in normal form. */
    public void applyGravity(float delta, Array<Rectangle> walls) {
        velocityY -= GRAVITY * delta;
        bounds.y += velocityY * delta;
        onGround = false;

        // Land on top of walls in normal form.
        if (!isLiquidState) {
            for (Rectangle wall : walls) {
                boolean horizontalOverlap =
                    bounds.x + bounds.width > wall.x
                        && bounds.x < wall.x + wall.width;

                boolean crossedWallTop =
                    bounds.y <= wall.y + wall.height
                        && bounds.y + bounds.height
                        >= wall.y + wall.height;

                if (horizontalOverlap
                    && velocityY <= 0f
                    && crossedWallTop) {

                    bounds.y = wall.y + wall.height;
                    velocityY = 0f;
                    onGround = true;
                }
            }
        }
    }

    /** Stands on the ground unless the cat is over a hole. */
    public void landOnGround(boolean overHole) {
        // The ground is solid except over holes.
        if (!overHole && bounds.y <= MyGdxGame.GROUND_Y
            && velocityY <= 0f) {
            bounds.y = MyGdxGame.GROUND_Y;
            velocityY = 0f;
            onGround = true;
        }
    }

    /** Counts down the liquid form and turns back into a cat when it runs out. */
    public void tickLiquid(float delta) {
        // Liquid form expires automatically.
        if (isLiquidState) {
            liquidTimer -= delta;

            if (liquidTimer <= 0f) {
                isLiquidState = false;
                liquidTimer = 0f;
                bounds.width = CAT_W;
                bounds.height = CAT_H;
            }
        }
    }

    /** Collects the spell without transforming yet. */
    public void collectSpell() {
        hasSpell = true;
    }

    /** True once the cat has dropped deep enough into a hole to lose a life. */
    public boolean fellIntoHole() {
        return bounds.y + bounds.height < MyGdxGame.GROUND_Y - 55f;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isLiquid() {
        return isLiquidState;
    }

    public boolean hasSpell() {
        return hasSpell;
    }

    public float getLiquidTimer() {
        return liquidTimer;
    }
}
