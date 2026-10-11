package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Enemies {
    public Rectangle bounds;
    public float velocityX;
    private float stateTime;

    public Enemies(float x, float y, float width, float height, float speed) {
        bounds = new Rectangle(x, y, width, height);
        velocityX = speed;
        stateTime = 0f;
    }

    public void update(float delta) {
        stateTime += delta;
        bounds.x += velocityX * delta;
    }

    public void reverseDirection() {
        velocityX = -velocityX;
    }

    public void draw(SpriteBatch batch, Animation<TextureRegion> animation) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        boolean movingLeft = velocityX < 0;

        // Draw flipped by negating width without permanently modifying the TextureRegion
        if (movingLeft) {
            batch.draw(currentFrame, bounds.x + bounds.width, bounds.y, -bounds.width, bounds.height);
        } else {
            batch.draw(currentFrame, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}
