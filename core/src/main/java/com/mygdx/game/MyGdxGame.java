
package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class MyGdxGame extends ApplicationAdapter {

    private static final float VIEW_W = 960f;
    private static final float VIEW_H = 540f;
    static final float GROUND_Y = 60f;

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

    private Music bgMusic;
    private Sound jumpSound;
    private Sound deadSound;
    private Sound spellSound;
    private Sound victorySound;
    private Sound transformSound;

    private enum GameState {
        MENU,
        PLAYING,
        PAUSED,
        LEVEL_SELECT,
        GAME_OVER,
        VICTORY
    }

    private GameState state = GameState.MENU;

    private Player player;
    private Level stage;

    private int currentLevel = 1;
    private int lives = 3;
    private int menuSelection = 0;
    private int levelSelection = 0;

    // Spells of this stage that have not been picked up yet.
    private final Array<Rectangle> spellsLeft = new Array<>();

    @Override
    public void create() {
        batch = new SpriteBatch();
        shape = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEW_W, VIEW_H);

        font = new BitmapFont();
        font.getData().setScale(1.7f);

        bgTexture = new Texture("background.png");
        catTexture = new Texture("dash1.png");
        liquidCatTexture = new Texture("liquidCat1.png");
        dogTexture = new Texture("dog4.png");
        wallTexture = new Texture("wall3.png");
        spellTexture = new Texture("spell1.png");
        exitTexture = new Texture("exit2.png");

        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("bgm.wav"));
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.4f);
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
        deadSound = Gdx.audio.newSound(Gdx.files.internal("dead.wav"));
        spellSound = Gdx.audio.newSound(Gdx.files.internal("spell.wav"));
        victorySound = Gdx.audio.newSound(Gdx.files.internal("victory.wav"));
        transformSound = Gdx.audio.newSound(Gdx.files.internal("transform.wav"));

        player = new Player();

        loadLevel(1);
    }

    // ==================================================
    // LEVEL SETUP
    // ==================================================

    private void loadLevel(int level) {
        currentLevel = level;
        stage = Level.forStage(level);

        player.reset();

        // Stage 1 has no spells; stages 2 and 3 have one before each tall wall.
        spellsLeft.clear();
        spellsLeft.addAll(stage.getSpells());

        updateCamera();
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

        // Background music plays everywhere except the pause menu
        // and the final win screen, where the victory fanfare plays.
        if (state == GameState.PAUSED || state == GameState.VICTORY) {
            bgMusic.pause();
        } else if (!bgMusic.isPlaying()) {
            bgMusic.play();
        }

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

        // Press P to use the held spell and become Liquid Cat; each spell works once.
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (player.tryBecomeLiquid()) {
                transformSound.play(0.8f);
            }
        }

        float speed = player.speed();
        player.applyFormSize();

        float moveX = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX -= speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)
            || Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX += speed * delta;
        }

        player.moveHorizontally(moveX, stage.getWalls(), stage.getWidth());

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (player.tryJump()) {
                jumpSound.play(0.6f);
            }
        }

        player.applyGravity(delta, stage.getWalls());

        Rectangle body = player.getBounds();
        player.landOnGround(stage.overlapsHole(body.x, body.width));

        player.tickLiquid(delta);

        // Collect a spell without transforming yet; the cat can hold only one.
        if (!player.hasSpell()) {
            for (Rectangle spell : spellsLeft) {
                if (player.getBounds().overlaps(spell)) {
                    spellsLeft.removeValue(spell, true);
                    player.collectSpell();
                    spellSound.play(0.7f);
                    break;
                }
            }
        }

        stage.updateDogs(delta);

        // Dogs only hurt the cat in normal form.
        if (player.getForm().isHurtByDogs()) {
            for (Dog dog : stage.getDogs()) {
                if (player.getBounds().overlaps(dog.getBounds())) {
                    loseLife();
                    return;
                }
            }
        }

        stage.updateDogs(delta);

        if (player.getForm().isHurtByDogs()) {
        for (Dog dog : stage.getDogs()) {
            if (player.getBounds().overlaps(dog.getBounds())) {
                loseLife();
                return;
            }
        }
    }

        // Holes are dangerous in both forms.
        if (player.fellIntoHole()) {
            loseLife();
            return;
        }

        if (player.getBounds().overlaps(stage.getExit())) {
            victorySound.play(0.8f);
            if (currentLevel < 3) {
                loadLevel(currentLevel + 1);
            } else {
                state = GameState.VICTORY;
            }
            return;
        }

        updateCamera();
    }

    private void loseLife() {
        deadSound.play(0.8f);
        lives--;

        if (lives <= 0) {
            state = GameState.GAME_OVER;
            return;
        }

        player.respawn();

        // A held spell is kept, and every spell pickup comes back,
        // so a spell used before dying cannot leave a tall wall impassable.
        spellsLeft.clear();
        spellsLeft.addAll(stage.getSpells());
        updateCamera();
    }

    private void updateCamera() {
        if (camera == null || player == null) {
            return;
        }

        Rectangle body = player.getBounds();
        float cameraX = body.x + body.width / 2f;

        camera.position.set(
            Math.max(
                VIEW_W / 2f,
                Math.min(cameraX, stage.getWidth() - VIEW_W / 2f)
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
                "Jump for a spell, P: use it (once)",
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
        for (Rectangle wall : stage.getWalls()) {
            batch.draw(
                wallTexture,
                wall.x, wall.y,
                wall.width, wall.height
            );
        }

        // Spell pickups only appear in Stages 2 and 3.
        for (Rectangle spell : spellsLeft) {
            batch.draw(
                spellTexture,
                spell.x, spell.y,
                spell.width, spell.height
            );
        }

        // Exit.
        Rectangle exit = stage.getExit();
        batch.draw(
            exitTexture,
            exit.x, exit.y,
            exit.width, exit.height
        );

        // Dogs.
        for (Dog dog : stage.getDogs()) {
            Rectangle dogBody = dog.getBounds();
            batch.draw(
                dogTexture,
                dogBody.x, dogBody.y,
                dogBody.width, dogBody.height
            );
        }

        // Player.
        Rectangle body = player.getBounds();
        if (player.isLiquid()) {
            batch.draw(
                liquidCatTexture,
                body.x, body.y,
                body.width, body.height
            );
        } else {
            batch.draw(
                catTexture,
                body.x, body.y,
                body.width, body.height
            );
        }

        batch.end();

        // Draw a green ground with black holes.
        // ShapeRenderer draws these in world coordinates.
        shape.begin(ShapeRenderer.ShapeType.Filled);

        shape.setColor(0.15f, 0.65f, 0.20f, 1f);

        float groundStart = 0f;

        for (Rectangle hole : stage.getHoles()) {
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

        if (groundStart < stage.getWidth()) {
            shape.rect(
                groundStart,
                0f,
                stage.getWidth() - groundStart,
                GROUND_Y
            );
        }

        shape.setColor(Color.BLACK);

        for (Rectangle hole : stage.getHoles()) {
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

        if (player.isLiquid()) {
            font.draw(
                batch,
                "Liquid: " + (int) Math.ceil(player.getLiquidTimer()) + "s",
                left + 20f,
                VIEW_H - 80f
            );
        } else if (player.hasSpell()) {
            font.draw(
                batch,
                "Spell ready: P",
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

        bgMusic.dispose();
        jumpSound.dispose();
        deadSound.dispose();
        spellSound.dispose();
        victorySound.dispose();
        transformSound.dispose();
    }
}
