package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * The cat: its position, jump and gravity, collisions with walls and ground, and the timed liquid form.
 * What differs between the forms is asked of the current {@link PlayerForm}.
 */
public class Player {

    private static final PlayerForm NORMAL = new NormalForm();
    private static final PlayerForm LIQUID = new LiquidForm();

    private static final float GRAVITY = 1500f;
    private static final float JUMP_SPEED = 620f;
    private static final float START_X = 100f;

    // Long enough to reach the later tall wall.
    private static final float LIQUID_DURATION = 5f;

    private final Rectangle bounds = new Rectangle();
    private float velocityY = 0f;
    private float liquidTimer = 0f;
    private PlayerForm form = NORMAL;
    private boolean onGround = true;
    private boolean hasSpell = false;

    /** Puts the cat at the start of a freshly loaded stage, without the spell. */
    public void reset() {
        form = NORMAL;
        hasSpell = false;
        liquidTimer = 0f;
        velocityY = 0f;
        onGround = true;

        bounds.set(START_X, MyGdxGame.GROUND_Y, NORMAL.width(), NORMAL.height());
    }

    /** Puts the cat back at the start after losing a life; a collected spell is kept. */
    public void respawn() {
        bounds.set(START_X, MyGdxGame.GROUND_Y, NORMAL.width(), NORMAL.height());

        velocityY = 0f;
        onGround = true;

        form = NORMAL;
        liquidTimer = 0f;
    }

    /** Turns into liquid if a spell is held and the cat is not liquid already; this uses up the spell. */
    public void tryBecomeLiquid() {
        if (hasSpell && form != LIQUID) {
            form = LIQUID;
            liquidTimer = LIQUID_DURATION;
            hasSpell = false;

            velocityY = 0f;
            bounds.y = MyGdxGame.GROUND_Y;
            onGround = true;
        }
    }

    /** Horizontal speed of the current form, in pixels per second. */
    public float speed() {
        return form.speed();
    }

    /** Sets the hitbox to the size of the current form. */
    public void applyFormSize() {
        bounds.width = form.width();
        bounds.height = form.height();
    }

    /** Moves by {@code moveX}, stopping at walls in normal form, and keeps the cat inside the stage. */
    public void moveHorizontally(float moveX, Array<Rectangle> walls, float levelWidth) {
        bounds.x += moveX;

        // Normal Cat is blocked by walls.
        // Liquid Cat can pass through every wall.
        if (form.isBlockedByWalls()) {
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
        if (onGround && form.canJump()) {
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
        if (form.isBlockedByWalls()) {
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
        if (form == LIQUID) {
            liquidTimer -= delta;

            if (liquidTimer <= 0f) {
                form = NORMAL;
                liquidTimer = 0f;
                bounds.width = NORMAL.width();
                bounds.height = NORMAL.height();
            }
        }
    }

    /** Collects a spell without transforming yet; the cat holds at most one. */
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
        return form == LIQUID;
    }

    /** The form the cat is in right now. */
    public PlayerForm getForm() {
        return form;
    }

    public boolean hasSpell() {
        return hasSpell;
    }

    public float getLiquidTimer() {
        return liquidTimer;
    }
}
