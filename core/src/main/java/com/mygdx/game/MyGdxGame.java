package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture bgTexture;
    ShapeRenderer shape;

    OrthographicCamera camera;

    Rectangle player;
    Rectangle powerUpPrize;

    float velocityY = 0;
    boolean isLiquidState = false;
    boolean prizeActive = true;

    @Override
    public void create () {
        batch = new SpriteBatch();
        bgTexture = new Texture("background.png");
        shape = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        player = new Rectangle(50, 50, 40, 80);
        powerUpPrize = new Rectangle(800, 50, 30, 30);
    }

    @Override
    public void render () {
        ScreenUtils.clear(0, 0, 0, 1);

        // Update camera position to follow the player
        camera.position.x = player.x + 200;
        camera.update();

        // --- BACKGROUND TILING FIX ---

        float bgRatio = (float) bgTexture.getWidth() / bgTexture.getHeight();
        float drawHeight = Gdx.graphics.getHeight();
        float drawWidth = drawHeight * bgRatio;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // Draw the background 5 times side-by-side to cover the scrolling level
        for (int i = 0; i < 5; i++) {
            batch.draw(bgTexture, -500 + (i * drawWidth), 0, drawWidth, drawHeight);
        }
        batch.end();

        // --- GAMEPLAY & PHYSICS ---
        float speed = isLiquidState ? 200f : 500f;

        if (Gdx.input.isKeyPressed(Keys.A)) player.x -= speed * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.D)) player.x += speed * Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyJustPressed(Keys.SPACE) && player.y <= 50) {
            velocityY = 450f;
        }

        player.y += velocityY * Gdx.graphics.getDeltaTime();
        if (player.y > 50) {
            velocityY -= 1200f * Gdx.graphics.getDeltaTime();
        } else {
            player.y = 50;
            velocityY = 0;
        }

        // --- STATE TRANSITION
        if (prizeActive && player.overlaps(powerUpPrize)) {
            isLiquidState = true;
            prizeActive = false;

            player.width = 80;
            player.height = 20;
        }

        // --- DRAW SHAPES ---
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(Color.DARK_GRAY);
        shape.rect(-1000, 0, 6000, 50);

        if (prizeActive) {
            shape.setColor(Color.YELLOW);
            shape.rect(powerUpPrize.x, powerUpPrize.y, powerUpPrize.width, powerUpPrize.height);
        }

        if (isLiquidState) {
            shape.setColor(Color.GREEN);
        } else {
            shape.setColor(Color.RED);
        }
        shape.rect(player.x, player.y, player.width, player.height);

        shape.end();
    }

    @Override
    public void dispose () {
        batch.dispose();
        bgTexture.dispose();
        shape.dispose();
    }
}
