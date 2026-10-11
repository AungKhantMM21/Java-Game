package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private BitmapFont font;

    // Player
    private Rectangle player;
    private Texture playerTex;
    private Texture playerLiquidTex;
    private float playerVelX = 0;
    private float playerVelY = 0;
    private boolean isLiquid = false;
    private boolean isGrounded = false;
    private final float GRAVITY = -1000f;
    private final float JUMP_POWER = 500f;
    private final float MOVE_SPEED = 250f;
    private final float FLOOR_Y = 50f;

    // Entities
    private ArrayList<Enemies> enemiesList;
    private ArrayList<Traps> trapsList;
    private ArrayList<Walls> wallsList;

    // Animation Assets
    private Texture dogWalkSheet;
    private Animation<TextureRegion> enemyWalkAnim;

    // Environment
    private Rectangle prize;
    private Texture prizeTex;
    private boolean prizeCollected = false;
    private Texture bgTex;

    // Game State
    private boolean isGameOver = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2f);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);

        player = new Rectangle(100, 100, 40, 40);
        if (Gdx.files.internal("player.png").exists()) playerTex = new Texture("player.png");
        if (Gdx.files.internal("player_liquid.png").exists()) playerLiquidTex = new Texture("player_liquid.png");

        prize = new Rectangle(1500, FLOOR_Y, 30, 30);
        if (Gdx.files.internal("prize.png").exists()) prizeTex = new Texture("prize.png");
        if (Gdx.files.internal("background.png").exists()) bgTex = new Texture("background.png");


        setupEnemyAnimation();
        setupLevel();
    }

    private void setupEnemyAnimation() {
        if (Gdx.files.internal("dog_walk.png").exists()) {
            dogWalkSheet = new Texture("dog_walk.png");

            // Adjust these coordinates to perfectly frame your sprite sheet without the background
            int[][] frameCoords = {
                {0, 0, 45, 50},
                {48, 0, 52, 50},
                {102, 0, 44, 50},
                {148, 0, 46, 50},
                {196, 0, 48, 50}
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

        // Add some walls (now act as deadly obstacles)
        wallsList.add(new Walls(300, FLOOR_Y, 50, 100));
        wallsList.add(new Walls(500, FLOOR_Y, 150, 40));
        wallsList.add(new Walls(900, FLOOR_Y, 100, 80));

        // Add some traps
        trapsList.add(new Traps(400, FLOOR_Y, 40, 20));
        trapsList.add(new Traps(700, FLOOR_Y, 60, 20));

        // Spawn 3-8 random enemies
        int numEnemies = MathUtils.random(3, 8);
        for (int i = 0; i < numEnemies; i++) {
            float startX = MathUtils.random(200f, 1400f);
            float startY = FLOOR_Y + 150f;
            enemiesList.add(new Enemies(startX, startY, 40, 40, 100f));
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        if (!isGameOver) {
            updateGame(delta);
        }

        camera.position.x = player.x + player.width / 2;
        camera.update();

        ScreenUtils.clear(0.5f, 0.7f, 1f, 1f);

        // Draw Textures
        batch.setProjectionMatrix(camera.combined);
        batch.enableBlending(); // Explicitly enables transparent PNG backgrounds
        batch.begin();

        if (bgTex != null) batch.draw(bgTex, camera.position.x - 400, 0, 800, 600);

        if (!prizeCollected) {
            if (prizeTex != null) batch.draw(prizeTex, prize.x, prize.y, prize.width, prize.height);
        }

        for (Walls wall : wallsList) wall.draw(batch);
        for (Traps trap : trapsList) trap.draw(batch);
        for (Enemies enemy : enemiesList) {
            if (enemyWalkAnim != null) enemy.draw(batch, enemyWalkAnim);
        }

        Texture currentTex = isLiquid ? playerLiquidTex : playerTex;
        if (currentTex != null) {
            batch.draw(currentTex, player.x, player.y, player.width, player.height);
        }

        if (isGameOver) {
            font.draw(batch, "GAME OVER", camera.position.x - 80, camera.position.y);
        } else if (prizeCollected) {
            font.draw(batch, "YOU WIN!", camera.position.x - 60, camera.position.y);
        }

        batch.end();

        // Draw ShapeRenderer Fallbacks (Only draws if PNG is missing)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(camera.position.x - 1000, 0, 2000, FLOOR_Y); // Safe floor

        for (Walls wall : wallsList) wall.drawFallback(shapeRenderer);
        for (Traps trap : trapsList) trap.drawFallback(shapeRenderer);
        for (Enemies enemy : enemiesList) {
            if (enemyWalkAnim == null) {
                shapeRenderer.setColor(Color.ORANGE);
                shapeRenderer.rect(enemy.bounds.x, enemy.bounds.y, enemy.bounds.width, enemy.bounds.height);
            }
        }

        if (currentTex == null) {
            shapeRenderer.setColor(isLiquid ? Color.CYAN : Color.GREEN);
            shapeRenderer.rect(player.x, player.y, player.width, player.height);
        }
        if (!prizeCollected && prizeTex == null) {
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(prize.x, prize.y, prize.width, prize.height);
        }

        shapeRenderer.end();
    }

    private void updateGame(float delta) {
        // Player Input
        isLiquid = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

        playerVelX = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) playerVelX = -MOVE_SPEED;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) playerVelX = MOVE_SPEED;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && isGrounded) {
            playerVelY = JUMP_POWER;
            isGrounded = false;
        }

        playerVelY += GRAVITY * delta;
        player.x += playerVelX * delta;
        player.y += playerVelY * delta;

        isGrounded = false;

        // Safe Floor Collision
        if (player.y <= FLOOR_Y) {
            player.y = FLOOR_Y;
            playerVelY = 0;
            isGrounded = true;
        }

        if (!prizeCollected && player.overlaps(prize)) {
            prizeCollected = true;
        }

        // --- DEATH / LIQUID LOGIC (Enemy, Trap, Wall ONLY) ---

        // 1. Walls
        for (int i = wallsList.size() - 1; i >= 0; i--) {
            Walls wall = wallsList.get(i);
            if (player.overlaps(wall.bounds)) {
                if (isLiquid) {
                    wallsList.remove(i);
                } else {
                    isGameOver = true;
                }
            }
        }

        // 2. Traps
        for (int i = trapsList.size() - 1; i >= 0; i--) {
            Traps trap = trapsList.get(i);
            if (player.overlaps(trap.bounds)) {
                if (isLiquid) {
                    trapsList.remove(i);
                } else {
                    isGameOver = true;
                }
            }
        }

        // 3. Enemies
        for (int i = enemiesList.size() - 1; i >= 0; i--) {
            Enemies enemy = enemiesList.get(i);
            enemy.update(delta);

            if (enemy.bounds.y > FLOOR_Y) {
                enemy.bounds.y += GRAVITY * delta;
            } else {
                enemy.bounds.y = FLOOR_Y;
            }

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
                if (isLiquid) {
                    enemiesList.remove(i);
                } else {
                    isGameOver = true;
                }
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        if (playerTex != null) playerTex.dispose();
        if (playerLiquidTex != null) playerLiquidTex.dispose();
        if (prizeTex != null) prizeTex.dispose();
        if (bgTex != null) bgTex.dispose();
        if (dogWalkSheet != null) dogWalkSheet.dispose();
        for (Walls wall : wallsList) wall.dispose();
        for (Traps trap : trapsList) trap.dispose();
    }
}
