package com.mygdx.game;

/** The cat's normal form: tall, fast, can jump, stopped by walls and hurt by dogs. */
public class NormalForm implements PlayerForm {

    @Override
    public float speed() {
        return 280f;
    }

    @Override
    public float width() {
        return 76f;
    }

    @Override
    public float height() {
        return 90f;
    }

    @Override
    public boolean isBlockedByWalls() {
        return true;
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public boolean isHurtByDogs() {
        return true;
    }
}
