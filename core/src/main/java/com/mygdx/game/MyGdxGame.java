package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.graphics.OrthographicCamera;
import java.util.ArrayList;

public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture bgTexture;
    Texture enemyTexture;
    BitmapFont font;
    ShapeRenderer shape;

    OrthographicCamera camera;

    Rectangle player;
    Rectangle powerUpPrize;
    ArrayList<Enemies> enemiesList;

    float velocityY = 0;
    boolean isLiquidState = false;
    boolean prizeActive = true;
    boolean isGameOver = false;

    @Override
    public void create () {
        batch = new SpriteBatch();
        bgTexture = new Texture("background.png");
        enemyTexture = new Texture("Guard_Dog.png");

        font = new BitmapFont();
        font.getData().setScale(3f);

        shape = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        player = new Rectangle(50, 50, 40, 80);
        powerUpPrize = new Rectangle(800, 50, 30, 30);

        enemiesList = new ArrayList<>();
        int enemyCount = MathUtils.random(5, 10); // Spawning slightly more for the larger level

        for (int i = 0; i < enemyCount; i++) {
            // Spread enemies across the new wider level bounds
            float randomX = MathUtils.random(400f, 3000f);
            Enemies newEnemy = new Enemies(randomX, 43, 90, 70);
            enemiesList.add(newEnemy);
        }
    }

    @Override
    public void render () {
        ScreenUtils.clear(0, 0, 0, 1);
        float deltaTime = Gdx.graphics.getDeltaTime();

        // Update camera position to follow the player
        if (!isGameOver) {
            camera.position.x = player.x + 200;
            camera.update();
        }

        // --- BACKGROUND TILING ---
        float bgRatio = (float) bgTexture.getWidth() / bgTexture.getHeight();
        float drawHeight = Gdx.graphics.getHeight();
        float drawWidth = drawHeight * bgRatio;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw the background 5 times side-by-side to cover the scrolling level
        for (int i = 0; i < 5; i++) {
            batch.draw(bgTexture, -500 + (i * drawWidth), 0, drawWidth, drawHeight);
        }

        // --- GAME OVER CHECK ---
        if (isGameOver) {
            font.setColor(Color.WHITE);
            // Draw text relative to the camera's center position so it stays on screen
            font.draw(batch, "GAME OVER", camera.position.x - 150, camera.position.y);
            batch.end();
            return;
        }

        // --- ENEMY RENDERING & MOVEMENT ---
        for (Enemies enemy : enemiesList) {
            enemy.move(deltaTime);
            enemy.draw(batch, enemyTexture);
        }

        batch.end();

        // --- GAMEPLAY & PHYSICS ---
        float speed = isLiquidState ? 200f : 500f;

        if (Gdx.input.isKeyPressed(Keys.A)) player.x -= speed * deltaTime;
        if (Gdx.input.isKeyPressed(Keys.D)) player.x += speed * deltaTime;

        // Keep player from walking left off the beginning of the world
        if (player.x < 0) player.x = 0;

        if (Gdx.input.isKeyJustPressed(Keys.SPACE) && player.y <= 50) {
            velocityY = 450f;
        }

        player.y += velocityY * deltaTime;
        if (player.y > 50) {
            velocityY -= 1200f * deltaTime;
        } else {
            player.y = 50;
            velocityY = 0;
        }

        // --- STATE TRANSITION ---
        if (prizeActive && player.overlaps(powerUpPrize)) {
            isLiquidState = true;
            prizeActive = false;

            player.width = 80;
            player.height = 20;
        }

        // --- ENEMY COLLISION LOGIC ---
        for (int i = enemiesList.size() - 1; i >= 0; i--) {
            Enemies currentEnemy = enemiesList.get(i);

            // Using the tighter getCollisionBox() to prevent unfair deaths
            if (player.overlaps(currentEnemy.getCollisionBox())) {
                if (isLiquidState) {
                    enemiesList.remove(i);
                } else {
                    isGameOver = true;
                }
            }
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
        enemyTexture.dispose();
        font.dispose();
        shape.dispose();
    }
}
