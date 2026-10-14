package com.mygdx.game;

/** The liquid cat: a low, slower puddle that flows through walls and slips past dogs, but cannot jump. */
public class LiquidForm implements PlayerForm {

    @Override
    public float speed() {
        return 220f;
    }

    @Override
    public float width() {
        return 82f;
    }

    @Override
    public float height() {
        return 30f;
    }

    @Override
    public boolean isBlockedByWalls() {
        return false;
    }

    @Override
    public boolean canJump() {
        return false;
    }

    @Override
    public boolean isHurtByDogs() {
        return false;
    }
}
