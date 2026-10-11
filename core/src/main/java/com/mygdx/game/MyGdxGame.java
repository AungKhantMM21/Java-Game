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

public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture bgTexture;

    ShapeRenderer shape;
    Rectangle player;
    Rectangle powerUpPrize;

    float velocityY = 0;
    boolean isLiquidState = false;
    boolean prizeActive = true;

    @Override
    public void create () {
        batch = new SpriteBatch();
        // Loads background.png from the assets folder
        bgTexture = new Texture("background.png");

        shape = new ShapeRenderer();
        // Standard Animal State: Tall and narrow hitbox
        player = new Rectangle(50, 50, 40, 80);
        powerUpPrize = new Rectangle(500, 50, 30, 30);
    }

    @Override
    public void render () {
        ScreenUtils.clear(0, 0, 0, 1);

        // --- 1. DRAW BACKGROUND IMAGE ---
        batch.begin();
        batch.draw(bgTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        // --- 2. GAMEPLAY: MOVEMENT & PHYSICS ---
        float speed = isLiquidState ? 150f : 300f;

        if (Gdx.input.isKeyPressed(Keys.A)) player.x -= speed * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Keys.D)) player.x += speed * Gdx.graphics.getDeltaTime();

        // Basic Jumping Logic
        if (Gdx.input.isKeyJustPressed(Keys.SPACE) && player.y <= 50) {
            velocityY = 450f; // Jump force
        }

        // Basic Gravity
        player.y += velocityY * Gdx.graphics.getDeltaTime();
        if (player.y > 50) {
            velocityY -= 1200f * Gdx.graphics.getDeltaTime(); // Gravity pulling down
        } else {
            player.y = 50; // Lock to floor level
            velocityY = 0;
        }

        // --- 3. CORE LOGIC: THE STATE TRANSITION ---
        if (prizeActive && player.overlaps(powerUpPrize)) {
            isLiquidState = true;
            prizeActive = false; // Consume the prize

            // Adjust dynamic hitbox and behavior for the liquid state
            player.width = 80;  // Becomes wide
            player.height = 20; // Becomes short (puddle)
        }

        // --- 4. RENDERING SHAPES ---
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Draw the floor
        shape.setColor(Color.DARK_GRAY);
        shape.rect(0, 0, Gdx.graphics.getWidth(), 50);

        // Draw the Power-Up Prize (Yellow Square)
        if (prizeActive) {
            shape.setColor(Color.YELLOW);
            shape.rect(powerUpPrize.x, powerUpPrize.y, powerUpPrize.width, powerUpPrize.height);
        }

        // Draw the Player
        if (isLiquidState) {
            shape.setColor(Color.GREEN); // Visually demonstrate the Liquid State
        } else {
            shape.setColor(Color.RED);   // Standard Animal State
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
