package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.ArrayList;

public class MyGdxGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shape;
    private OrthographicCamera camera;
    private BitmapFont font;

    // Player & Mechanics
    private Rectangle player;
    private Texture playerTex;
    private Texture playerLiquidTex;
    private float velocityY = 0;
    private boolean isLiquidState = false;
    private boolean isGrounded = false;
    private final float FLOOR_Y = 50f;

    // Entities
    private ArrayList<Enemies> enemiesList;
    private ArrayList<Traps> trapsList;
    private ArrayList<Walls> wallsList;

    // Animation Assets
    private Texture dogWalkSheet;
    private Animation<TextureRegion> enemyWalkAnim;

    // Environment
    private Rectangle powerUpPrize;
    private Texture prizeTex;
    private boolean prizeActive = true;
    private Texture bgTexture;

    // Game State
    private boolean isGameOver = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);

        // Standard Animal State: Tall and narrow hitbox
        player = new Rectangle(100, FLOOR_Y, 40, 80);
        if (Gdx.files.internal("player.png").exists()) playerTex = new Texture("player.png");
        if (Gdx.files.internal("player_liquid.png").exists()) playerLiquidTex = new Texture("player_liquid.png");

        powerUpPrize = new Rectangle(1500, FLOOR_Y, 30, 30);
        if (Gdx.files.internal("prize.png").exists()) prizeTex = new Texture("prize.png");
        if (Gdx.files.internal("background.png").exists()) bgTexture = new Texture("background.png");

        setupEnemyAnimation();
        setupLevel();
    }

    private void setupEnemyAnimation() {
        if (Gdx.files.internal("dog_walk.png").exists()) {
            dogWalkSheet = new Texture("dog_walk.png");
            int[][] frameCoords = {
                {0, 0, 45, 50}, {48, 0, 52, 50}, {102, 0, 44, 50},
                {148, 0, 46, 50}, {196, 0, 48, 50}
            };

            TextureRegion[] walkFrames = new TextureRegion[frameCoords.length];
            for (int i = 0; i < frameCoords.length; i++) {
                walkFrames[i] = new TextureRegion(dogWalkSheet, frameCoords[i][0], frameCoords[i][1], frameCoords[i][2], frameCoords[i][3]);
            }
            enemyWalkAnim = new Animation<>(0.15f, walkFrames);
        }
    }

    private void setupLevel() {
        wallsList = new ArrayList<>();
        trapsList = new ArrayList<>();
        enemiesList = new ArrayList<>();

        // Add some walls (Solid obstacles)
        wallsList.add(new Walls(300, FLOOR_Y, 50, 100));
        wallsList.add(new Walls(500, FLOOR_Y, 150, 40));
        wallsList.add(new Walls(900, FLOOR_Y + 80, 100, 20));

        // Add some traps (Deadly)
        trapsList.add(new Traps(400, FLOOR_Y, 40, 20));
        trapsList.add(new Traps(700, FLOOR_Y, 60, 20));

        // Spawn enemies
        int numEnemies = MathUtils.random(3, 8);
        for (int i = 0; i < numEnemies; i++) {
            float startX = MathUtils.random(200f, 1400f);
            enemiesList.add(new Enemies(startX, FLOOR_Y + 150f, 40, 40, 100f));
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        if (!isGameOver) {
            updateGame(delta);
        }

        // Camera follow player
        camera.position.x = player.x + player.width / 2;
        camera.update();

        ScreenUtils.clear(0, 0, 0, 1);

        // --- DRAW TEXTURES ---
        batch.setProjectionMatrix(camera.combined);
        batch.enableBlending(); // Ensures transparent PNGs draw without background boxes
        batch.begin();

        if (bgTexture != null) batch.draw(bgTexture, camera.position.x - 400, 0, 800, 600);

        if (prizeActive && prizeTex != null) {
            batch.draw(prizeTex, powerUpPrize.x, powerUpPrize.y, powerUpPrize.width, powerUpPrize.height);
        }

        for (Walls wall : wallsList) wall.draw(batch);
        for (Traps trap : trapsList) trap.draw(batch);
        for (Enemies enemy : enemiesList) {
            if (enemyWalkAnim != null) enemy.draw(batch, enemyWalkAnim);
        }

        Texture currentTex = isLiquidState ? playerLiquidTex : playerTex;
        if (currentTex != null) {
            batch.draw(currentTex, player.x, player.y, player.width, player.height);
        }

        if (isGameOver) {
            font.setColor(Color.RED);
            font.draw(batch, "GAME OVER", camera.position.x - 80, camera.position.y);
        } else if (!prizeActive) { // Won condition could be changed
            font.setColor(Color.WHITE);
            font.draw(batch, "LIQUID STATE ACTIVE", camera.position.x - 120, camera.position.y + 150);
        }

        batch.end();

        // --- DRAW SHAPERENDERER FALLBACKS ---
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Floor
        shape.setColor(Color.DARK_GRAY);
        shape.rect(camera.position.x - 1000, 0, 2000, FLOOR_Y);

        for (Walls wall : wallsList) wall.drawFallback(shape);
        for (Traps trap : trapsList) trap.drawFallback(shape);
        for (Enemies enemy : enemiesList) {
            if (enemyWalkAnim == null) {
                shape.setColor(Color.ORANGE);
                shape.rect(enemy.bounds.x, enemy.bounds.y, enemy.bounds.width, enemy.bounds.height);
            }
        }

        if (currentTex == null) {
            shape.setColor(isLiquidState ? Color.GREEN : Color.RED);
            shape.rect(player.x, player.y, player.width, player.height);
        }
        if (prizeActive && prizeTex == null) {
            shape.setColor(Color.YELLOW);
            shape.rect(powerUpPrize.x, powerUpPrize.y, powerUpPrize.width, powerUpPrize.height);
        }

        shape.end();
    }

    private void updateGame(float delta) {
        // --- MOVEMENT & PHYSICS ---
        float speed = isLiquidState ? 150f : 300f;
        float velocityX = 0;

        if (Gdx.input.isKeyPressed(Keys.A)) velocityX = -speed;
        if (Gdx.input.isKeyPressed(Keys.D)) velocityX = speed;

        // Basic Jumping Logic
        if (Gdx.input.isKeyJustPressed(Keys.SPACE) && isGrounded) {
            velocityY = 450f;
            isGrounded = false;
        }

        // Apply Gravity
        velocityY -= 1200f * delta;

        // Horizontal Movement & Wall Collision
        player.x += velocityX * delta;
        for (Walls wall : wallsList) {
            if (player.overlaps(wall.bounds)) {
                if (velocityX > 0) player.x = wall.bounds.x - player.width;
                else if (velocityX < 0) player.x = wall.bounds.x + wall.bounds.width;
                velocityX = 0;
            }
        }

        // Vertical Movement & Wall/Floor Collision
        player.y += velocityY * delta;
        isGrounded = false;

        // Floor Collision
        if (player.y <= FLOOR_Y) {
            player.y = FLOOR_Y;
            velocityY = 0;
            isGrounded = true;
        }

        // Wall Vertical Collision (Solid Platforms)
        for (Walls wall : wallsList) {
            if (player.overlaps(wall.bounds)) {
                if (velocityY > 0) { // Hit ceiling
                    player.y = wall.bounds.y - player.height;
                    velocityY = 0;
                } else if (velocityY < 0) { // Landed on wall
                    player.y = wall.bounds.y + wall.bounds.height;
                    velocityY = 0;
                    isGrounded = true;
                }
            }
        }

        // --- CORE LOGIC: THE STATE TRANSITION ---
        if (prizeActive && player.overlaps(powerUpPrize)) {
            isLiquidState = true;
            prizeActive = false; // Consume the prize

            // Adjust dynamic hitbox and behavior for the liquid state
            player.width = 80;  // Becomes wide
            player.height = 20; // Becomes short (puddle)
        }

        // --- DEATH & LIQUID COMBAT LOGIC ---

        // Traps vs Player
        for (int i = trapsList.size() - 1; i >= 0; i--) {
            Traps trap = trapsList.get(i);
            if (player.overlaps(trap.bounds)) {
                if (isLiquidState) trapsList.remove(i); // Liquid destroys traps
                else isGameOver = true; // Normal dies
            }
        }

        // Enemies vs Player
        for (int i = enemiesList.size() - 1; i >= 0; i--) {
            Enemies enemy = enemiesList.get(i);
            enemy.update(delta);

            // Enemy Floor gravity
            if (enemy.bounds.y > FLOOR_Y) enemy.bounds.y -= 1200f * delta;
            else enemy.bounds.y = FLOOR_Y;

            // Enemies bounce off walls to patrol
            for (Walls wall : wallsList) {
                if (enemy.bounds.overlaps(wall.bounds)) {
                    enemy.reverseDirection();
                    if (enemy.velocityX > 0) enemy.bounds.x = wall.bounds.x + wall.bounds.width;
                    else enemy.bounds.x = wall.bounds.x - enemy.bounds.width;
                    break;
                }
            }

            if (player.overlaps(enemy.bounds)) {
                if (isLiquidState) enemiesList.remove(i); // Liquid destroys enemies
                else isGameOver = true; // Normal dies
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();
        if (playerTex != null) playerTex.dispose();
        if (playerLiquidTex != null) playerLiquidTex.dispose();
        if (prizeTex != null) prizeTex.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (dogWalkSheet != null) dogWalkSheet.dispose();
        for (Walls wall : wallsList) wall.dispose();
        for (Traps trap : trapsList) trap.dispose();
    }
}
