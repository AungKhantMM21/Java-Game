package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Traps {
    public Rectangle bounds;
    private Texture texture;

    public Traps(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);
        if (Gdx.files.internal("SpikeTrap.png").exists()) {
            texture = new Texture("SpikeTrap.png");
        }
    }

    public void draw(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public void drawFallback(ShapeRenderer shapeRenderer) {
        if (texture == null) {
            shapeRenderer.setColor(1f, 0f, 0f, 1f); // Red for dangerous traps
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
