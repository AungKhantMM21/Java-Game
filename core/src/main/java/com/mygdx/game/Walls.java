package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Walls {
    public Rectangle bounds;
    private Texture texture;

    public Walls(float x, float y, float width, float height) {
        bounds = new Rectangle(x, y, width, height);
        if (Gdx.files.internal("Wall.png").exists()) {
            texture = new Texture("Wall.png");
        }
    }

    public void draw(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public void drawFallback(ShapeRenderer shapeRenderer) {
        if (texture == null) {
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f); // Dark gray for walls
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
