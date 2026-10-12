
package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Array;

public class MyGdxGame extends ApplicationAdapter {

    private static final float VIEW_W = 960f;
    private static final float VIEW_H = 540f;
    private static final float GROUND_Y = 60f;

    private static final float CAT_W = 76f;
    private static final float CAT_H = 90f;
    private static final float PUDDLE_W = 82f;
    private static final float PUDDLE_H = 30f;

    private static final float DOG_W = 60f;
    private static final float DOG_H = 48f;

    private static final float NORMAL_SPEED = 280f;
    private static final float LIQUID_SPEED = 220f;
    private static final float GRAVITY = 1500f;
    private static final float JUMP_SPEED = 620f;



    // Long enough to reach the later tall wall.
    private static final float LIQUID_DURATION = 15f;

    private SpriteBatch batch;
    private ShapeRenderer shape;
    private OrthographicCamera camera;
    private BitmapFont font;

    private Texture bgTexture;
    private Texture catTexture;
    private Texture liquidCatTexture;
    private Texture dogTexture;
    private Texture wallTexture;
    private Texture spellTexture;
    private Texture exitTexture;

    private enum GameState {
        MENU,
        PLAYING,
        PAUSED,
        LEVEL_SELECT,
        GAME_OVER,
        VICTORY
    }

    private GameState state = GameState.MENU;

    private Rectangle player;
    private Rectangle spell;
    private Rectangle exit;

    private final Array<Rectangle> walls = new Array<>();
    private final Array<Rectangle> holes = new Array<>();
    private final Array<Dog> dogs = new Array<>();

    private static class Dog {
        Rectangle bounds;
        float minX;
        float maxX;
        float speed;
        int direction = 1;

        Dog(float x, float minX, float maxX, float speed) {
            bounds = new Rectangle(x, GROUND_Y, DOG_W, DOG_H);
            this.minX = minX;
            this.maxX = maxX;
            this.speed = speed;
        }
    }

    private int currentLevel = 1;
    private int lives = 3;
    private int menuSelection = 0;
    private int levelSelection = 0;

    private float velocityY = 0f;
    private float liquidTimer = 0f;
    private float levelWidth = 3000f;

    private boolean isLiquidState = false;
    private boolean prizeActive = false;
    private boolean onGround = true;
    private boolean hasSpell = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shape = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_W, VIEW_H);

        font = new BitmapFont();
        font.getData().setScale(1.7f);

        bgTexture = new Texture("background.png");
        catTexture = new Texture("dash.png");
        liquidCatTexture = new Texture("liquidCat1.png");
        dogTexture = new Texture("dog3.png");
        wallTexture = new Texture("wall1.png");
        spellTexture = new Texture("spell.png");
        exitTexture = new Texture("exit.png");

        player = new Rectangle();

        loadLevel(1);
    }

    // ==================================================
    // LEVEL SETUP
    // ==================================================

    private void loadLevel(int level) {
        currentLevel = level;

        walls.clear();
        holes.clear();
        dogs.clear();

        isLiquidState = false;
        hasSpell = false;
        liquidTimer = 0f;
        velocityY = 0f;
        onGround = true;

        player.set(100f, GROUND_Y, CAT_W, CAT_H);

        // Stage 1: no spell.
        // Stages 2 and 3: spell is available.
        prizeActive = level >= 2;
        spell = null;

        if (level == 1) {
            levelWidth = 3000f;

            addWall(500, 60, 50);
            addHole(850, 75);
            addWall(1150, 65, 55);

            addHole(2250, 80);

            addDog(280, 200, 420, 100);
            addDog(2450, 2350, 2650, 110);

            addExit(2800);
        }

        else if (level == 2) {
            levelWidth = 3600f;

            addWall(500, 60, 50);
            addHole(850, 75);
            addWall(1150, 65, 55);

            // Spell pickup for Stage 2.
            addSpell(1400);

            addWall(1850, 70, 170);
            addHole(2250, 80);
            addWall(2600, 70, 55);

            // Tall, unjumpable wall near the ending.
            addWall(2950, 70, 170);

            addDog(280, 200, 420, 100);
            addDog(1600, 1500, 1750, 120);
            addDog(3250, 3100, 3400, 120);

            addExit(3410);
        }

        else {
            levelWidth = 4200f;

            addWall(500, 60, 50);
            addHole(850, 75);
            addWall(1150, 65, 55);

            // Spell pickup for Stage 3.
            addSpell(1400);

            addWall(1850, 70, 170);
            addHole(2250, 80);
            addWall(2600, 70, 55);
            addWall(2950, 70, 170);

            addHole(3450, 85);

            // Final tall, unjumpable wall.
            addWall(3750, 70, 170);

            addDog(280, 200, 420, 100);
            addDog(1600, 1500, 1750, 120);
            addDog(2500, 2400, 2700, 120);
            addDog(3500, 3300, 3600, 130);

            addExit(4020);
        }

        addExitRectangle();
        updateCamera();
    }

    private void addWall(float x, float width, float height) {
        walls.add(new Rectangle(x, GROUND_Y, width, height));
    }

    private void addHole(float x, float width) {
        holes.add(new Rectangle(x, 0f, width, GROUND_Y));
    }

    private void addDog(
        float x, float minX, float maxX, float speed
    ) {
        dogs.add(new Dog(x, minX, maxX, speed));
    }

    private void addSpell(float x) {
        spell = new Rectangle(
            x, GROUND_Y + 55f, 38f, 38f
        );
    }

    private void addExit(float x) {
        exit = new Rectangle(
            x, GROUND_Y, 55f, 100f
        );
    }

    private void addExitRectangle() {
        // Exit is set by addExit() in each stage.
        // This method is intentionally left empty.
    }

    // ==================================================
    // INPUT
    // ==================================================

    @Override
    public void render() {
        float delta = Math.min(
            Gdx.graphics.getDeltaTime(), 0.033f
        );

        handleMenuInput();

        // No game updates while paused or in a menu.
        if (state == GameState.PLAYING) {
            updateGame(delta);
        }

        drawScreen();
    }

    private void handleMenuInput() {
        if (state == GameState.PLAYING) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                state = GameState.PAUSED;
                menuSelection = 0;
            }
            return;
        }

        if (state == GameState.MENU) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                lives = 3;
                loadLevel(1);
                state = GameState.PLAYING;
            }
            return;
        }

        if (state == GameState.PAUSED) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                menuSelection--;
                if (menuSelection < 0) {
                    menuSelection = 2;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                menuSelection++;
                if (menuSelection > 2) {
                    menuSelection = 0;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                state = GameState.PLAYING;
                return;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (menuSelection == 0) {
                    // Resume exact current game state.
                    state = GameState.PLAYING;
                } else if (menuSelection == 1) {
                    // Restart current stage.
                    lives = 3;
                    loadLevel(currentLevel);
                    state = GameState.PLAYING;
                } else {
                    // Open level selection.
                    levelSelection = currentLevel - 1;
                    state = GameState.LEVEL_SELECT;
                }
            }
            return;
        }

        if (state == GameState.LEVEL_SELECT) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                levelSelection--;
                if (levelSelection < 0) {
                    levelSelection = 2;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                levelSelection++;
                if (levelSelection > 2) {
                    levelSelection = 0;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                state = GameState.PAUSED;
                return;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                lives = 3;
                loadLevel(levelSelection + 1);
                state = GameState.PLAYING;
            }
            return;
        }

        if (state == GameState.GAME_OVER
            || state == GameState.VICTORY) {

            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (state == GameState.VICTORY && currentLevel < 3) {
                    loadLevel(currentLevel + 1);
                } else {
                    loadLevel(currentLevel);
                }

                lives = 3;
                state = GameState.PLAYING;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                state = GameState.MENU;
                menuSelection = 0;
            }
        }
    }

    // ==================================================
    // GAME UPDATE
    // ==================================================

    private void updateGame(float delta) {


        // Press P to activate Liquid Cat whenever the spell is owned.
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)
            && hasSpell
            && !isLiquidState) {

            isLiquidState = true;
            liquidTimer = LIQUID_DURATION;

            velocityY = 0f;
            player.y = GROUND_Y;
            onGround = true;
        }

        float speed = isLiquidState
            ? LIQUID_SPEED : NORMAL_SPEED;
        float playerWidth = isLiquidState ? PUDDLE_W : CAT_W;
        float playerHeight = isLiquidState ? PUDDLE_H : CAT_H;

        player.width = playerWidth;
        player.height = playerHeight;

        float moveX = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX -= speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)
            || Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX += speed * delta;
        }

        player.x += moveX;

        // Normal Cat is blocked by walls.
        // Liquid Cat can pass through every wall.
        if (!isLiquidState) {
            for (Rectangle wall : walls) {
                if (player.overlaps(wall)) {
                    if (moveX > 0f) {
                        player.x = wall.x - player.width;
                    } else if (moveX < 0f) {
                        player.x = wall.x + wall.width;
                    }
                }
            }
        }

        player.x = Math.max(
            0f,
            Math.min(player.x, levelWidth - player.width)
        );

        // Jumping is only available in normal form.
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            && onGround && !isLiquidState) {
            velocityY = JUMP_SPEED;
            onGround = false;
        }

        // Gravity.
        velocityY -= GRAVITY * delta;
        player.y += velocityY * delta;
        onGround = false;

        // Land on top of walls in normal form.
        if (!isLiquidState) {
            for (Rectangle wall : walls) {
                boolean horizontalOverlap =
                    player.x + player.width > wall.x
                        && player.x < wall.x + wall.width;

                boolean crossedWallTop =
                    player.y <= wall.y + wall.height
                        && player.y + player.height
                        >= wall.y + wall.height;

                if (horizontalOverlap
                    && velocityY <= 0f
                    && crossedWallTop) {

                    player.y = wall.y + wall.height;
                    velocityY = 0f;
                    onGround = true;
                }
            }
        }

        boolean overHole = overlapsHole(
            player.x, player.width
        );

        // The ground is solid except over holes.
        if (!overHole && player.y <= GROUND_Y
            && velocityY <= 0f) {
            player.y = GROUND_Y;
            velocityY = 0f;
            onGround = true;
        }

        // Liquid form expires automatically.
        if (isLiquidState) {
            liquidTimer -= delta;

            if (liquidTimer <= 0f) {
                isLiquidState = false;
                liquidTimer = 0f;
                player.width = CAT_W;
                player.height = CAT_H;
            }
        }


        // Collect the spell without transforming yet.
        if (prizeActive && spell != null
            && player.overlaps(spell)) {

            prizeActive = false;
            hasSpell = true;
        }

        updateDogs(delta);

        // Dogs only hurt the cat in normal form.
        if (!isLiquidState) {
            for (Dog dog : dogs) {
                if (player.overlaps(dog.bounds)) {
                    loseLife();
                    return;
                }
            }
        }

        updateDogs(delta);

        if (!isLiquidState) {
        for (Dog dog : dogs) {
            if (player.overlaps(dog.bounds)) {
                loseLife();
                return;
            }
        }
    }

        // Holes are dangerous in both forms.
        if (player.y + player.height < GROUND_Y - 55f) {
            loseLife();
            return;
        }

        if (player.overlaps(exit)) {
            if (currentLevel < 3) {
                loadLevel(currentLevel + 1);
            } else {
                state = GameState.VICTORY;
            }
            return;
        }

        updateCamera();
    }

    private void updateDogs(float delta) {
        for (Dog dog : dogs) {
            dog.bounds.x += dog.speed * dog.direction * delta;

            if (dog.bounds.x <= dog.minX) {
                dog.bounds.x = dog.minX;
                dog.direction = 1;
            }

            if (dog.bounds.x >= dog.maxX) {
                dog.bounds.x = dog.maxX;
                dog.direction = -1;
            }
        }
    }

    private boolean overlapsHole(float x, float width) {
        for (Rectangle hole : holes) {
            if (x + width > hole.x
                && x < hole.x + hole.width) {
                return true;
            }
        }
        return false;
    }

    private void loseLife() {
        lives--;

        if (lives <= 0) {
            state = GameState.GAME_OVER;
            return;
        }

        player.set(100f, GROUND_Y, CAT_W, CAT_H);

        velocityY = 0f;
        onGround = true;

        isLiquidState = false;
        liquidTimer = 0f;

        // Spell remains collected after pickup.
        // If it was not collected, it remains available.
        updateCamera();
    }

    private void updateCamera() {
        if (camera == null || player == null) {
            return;
        }

        float cameraX = player.x + player.width / 2f;

        camera.position.set(
            Math.max(
                VIEW_W / 2f,
                Math.min(cameraX, levelWidth - VIEW_W / 2f)
            ),
            VIEW_H / 2f,
            0f
        );

        camera.update();
    }

    // ==================================================
    // DRAWING
    // ==================================================

    private void drawScreen() {
        ScreenUtils.clear(0.08f, 0.10f, 0.14f, 1f);

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shape.setProjectionMatrix(camera.combined);

        drawWorld();

        if (state == GameState.PLAYING) {
            drawHUD();
        }

        if (state == GameState.MENU) {
            drawMenu(
                "PAWS & PUDDLES",
                "Press ENTER to Play",
                "A / D or Arrow Keys: Move",
                "SPACE: Jump",
                "Collect the spell in Stage 2 and 3",
                "M: Pause"
            );
        } else if (state == GameState.PAUSED) {
            drawPauseMenu();
        } else if (state == GameState.LEVEL_SELECT) {
            drawLevelSelect();
        } else if (state == GameState.GAME_OVER) {
            drawMenu(
                "GAME OVER",
                "Press ENTER to restart Stage " + currentLevel,
                "Press M to return to the main menu",
                "", "", ""
            );
        } else if (state == GameState.VICTORY) {
            drawMenu(
                "YOU WIN!",
                currentLevel < 3
                    ? "Press ENTER for the next stage"
                    : "You completed all 3 stages!",
                "Press M to return to the main menu",
                "", "", ""
            );
        }
    }

    private void drawWorld() {
        float left = camera.position.x - VIEW_W / 2f;

        batch.begin();

        // Background.
        batch.draw(
            bgTexture,
            left, 0f,
            VIEW_W, VIEW_H
        );

        // Walls.
        for (Rectangle wall : walls) {
            batch.draw(
                wallTexture,
                wall.x, wall.y,
                wall.width, wall.height
            );
        }

        // Spell pickup only appears in Stages 2 and 3.
        if (prizeActive && spell != null) {
            batch.draw(
                spellTexture,
                spell.x, spell.y,
                spell.width, spell.height
            );
        }

        // Exit.
        batch.draw(
            exitTexture,
            exit.x, exit.y,
            exit.width, exit.height
        );

        // Dogs.
        for (Dog dog : dogs) {
            batch.draw(
                dogTexture,
                dog.bounds.x, dog.bounds.y,
                dog.bounds.width, dog.bounds.height
            );
        }

        // Player.
        if (isLiquidState) {
            batch.draw(
                liquidCatTexture,
                player.x, player.y,
                player.width, player.height
            );
        } else {
            batch.draw(
                catTexture,
                player.x, player.y,
                player.width, player.height
            );
        }

        batch.end();

        // Draw a green ground with black holes.
        // ShapeRenderer draws these in world coordinates.
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(0.15f, 0.65f, 0.20f, 1f);

        float groundStart = 0f;

        for (Rectangle hole : holes) {
            if (hole.x > groundStart) {
                shape.rect(
                    groundStart,
                    0f,
                    hole.x - groundStart,
                    GROUND_Y
                );
            }

            groundStart = hole.x + hole.width;
        }

        if (groundStart < levelWidth) {
            shape.rect(
                groundStart,
                0f,
                levelWidth - groundStart,
                GROUND_Y
            );
        }

        shape.setColor(Color.BLACK);

        for (Rectangle hole : holes) {
            shape.rect(
                hole.x, 0f,
                hole.width, GROUND_Y
            );
        }

        shape.end();
    }

    private void drawHUD() {
        batch.begin();

        font.setColor(Color.WHITE);

        float left = camera.position.x - VIEW_W / 2f;

        font.draw(
            batch,
            "Stage: " + currentLevel,
            left + 20f,
            VIEW_H - 20f
        );

        font.draw(
            batch,
            "Lives: " + lives,
            left + 20f,
            VIEW_H - 50f
        );

        if (isLiquidState) {
            font.draw(
                batch,
                "Liquid: " + (int) Math.ceil(liquidTimer) + "s",
                left + 20f,
                VIEW_H - 80f
            );
        }

        font.draw(
            batch,
            "M: Menu",
            left + VIEW_W - 140f,
            VIEW_H - 20f
        );

        batch.end();
    }

    private void drawOverlay() {
        // Enable alpha blending for the transparent dark overlay.
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.78f);

        shape.rect(
            camera.position.x - VIEW_W / 2f,
            0f,
            VIEW_W,
            VIEW_H
        );

        shape.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawMenu(
        String title,
        String line1,
        String line2,
        String line3,
        String line4,
        String line5
    ) {
        drawOverlay();

        batch.begin();

        font.setColor(Color.WHITE);
        font.getData().setScale(2.5f);

        font.draw(
            batch, title,
            camera.position.x - 220f,
            430f
        );

        font.getData().setScale(1.35f);

        font.draw(batch, line1,
            camera.position.x - 270f, 340f);
        font.draw(batch, line2,
            camera.position.x - 270f, 290f);
        font.draw(batch, line3,
            camera.position.x - 270f, 250f);
        font.draw(batch, line4,
            camera.position.x - 270f, 210f);
        font.draw(batch, line5,
            camera.position.x - 270f, 170f);

        batch.end();

        font.getData().setScale(1.7f);
    }

    private void drawPauseMenu() {
        drawOverlay();

        String[] options = {
            "Resume",
            "Restart",
            "Level Select"
        };

        batch.begin();

        font.getData().setScale(2.5f);
        font.setColor(Color.WHITE);

        font.draw(
            batch,
            "PAUSED",
            camera.position.x - 100f,
            440f
        );

        font.getData().setScale(1.8f);

        for (int i = 0; i < options.length; i++) {
            float y = 350f - i * 65f;

            if (i == menuSelection) {
                font.setColor(Color.YELLOW);
                font.draw(
                    batch,
                    "> " + options[i] + " <",
                    camera.position.x - 120f,
                    y
                );
            } else {
                font.setColor(Color.WHITE);
                font.draw(
                    batch,
                    options[i],
                    camera.position.x - 90f,
                    y
                );
            }
        }

        font.getData().setScale(1.1f);
        font.setColor(Color.LIGHT_GRAY);

        font.draw(
            batch,
            "UP / DOWN: Select   ENTER: Confirm   ESC: Back",
            camera.position.x - 310f,
            100f
        );

        batch.end();

        font.getData().setScale(1.7f);
    }

    private void drawLevelSelect() {
        drawOverlay();

        batch.begin();

        font.getData().setScale(2.3f);
        font.setColor(Color.WHITE);

        font.draw(
            batch,
            "SELECT STAGE",
            camera.position.x - 180f,
            440f
        );

        font.getData().setScale(1.8f);

        for (int i = 0; i < 3; i++) {
            String label = "Stage " + (i + 1);
            float y = 350f - i * 65f;

            if (i == levelSelection) {
                font.setColor(Color.YELLOW);
                label = "> " + label + " <";
            } else {
                font.setColor(Color.WHITE);
            }

            font.draw(
                batch,
                label,
                camera.position.x - 100f,
                y
            );
        }

        font.getData().setScale(1.1f);
        font.setColor(Color.LIGHT_GRAY);

        font.draw(
            batch,
            "UP / DOWN: Select   ENTER: Play   ESC: Back",
            camera.position.x - 310f,
            100f
        );

        batch.end();

        font.getData().setScale(1.7f);
    }

    // ==================================================
    // RESIZE AND CLEANUP
    // ==================================================

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, VIEW_W, VIEW_H);
        updateCamera();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        font.dispose();

        bgTexture.dispose();
        catTexture.dispose();
        liquidCatTexture.dispose();
        dogTexture.dispose();
        wallTexture.dispose();
        spellTexture.dispose();
        exitTexture.dispose();
    }
}
