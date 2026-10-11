package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Enemies {
    private Texture texture;
    public Rectangle bounds;
    public float velocityX;
    private float stateTime;

    public Enemies(float x, float y, float width, float height, float speed) {
        bounds = new Rectangle(x, y, width, height);
        if (Gdx.files.internal("Guard_Dog.png").exists()) {
            texture = new Texture("Guard_Dog.png");
        }
    }

    public Enemies(Texture texture) {
        this.texture = texture;
    }

    public void update(float delta) {
        stateTime += delta;
        bounds.x += velocityX * delta;
    }

    public void reverseDirection() {
        velocityX = -velocityX;
    }

    public void draw(SpriteBatch batch, Animation<TextureRegion> animation) {
        // getKeyFrame takes stateTime and a boolean for looping
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
