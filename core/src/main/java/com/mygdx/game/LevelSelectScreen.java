package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class LevelSelectScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont levelFont;
    private ShapeRenderer shapeRenderer;
    private LevelCallback callback;
    
    private int selectedLevel = 1;
    private Rectangle[] levelRects;
    private int maxLevels = 5;
    
    private static final int LEVEL_BUTTON_SIZE = 90;
    private static final int LEVEL_SPACING = 110;
    private static final Color SELECTED_COLOR = new Color(0.2f, 1f, 0.4f, 1f);    // Green
    private static final Color SELECTED_OUTLINE = new Color(0.1f, 0.7f, 0.2f, 1f);
    private static final Color NORMAL_COLOR = new Color(0.5f, 0.7f, 1f, 1f);      // Light blue
    private static final Color NORMAL_OUTLINE = new Color(0.3f, 0.5f, 0.9f, 1f);
    private static final Color BUTTON_BG = new Color(0.1f, 0.1f, 0.2f, 1f);
    private static final Color BG_COLOR = new Color(0.05f, 0.05f, 0.1f, 1f);
    
    private float pulseAnimation = 0;
    
    public interface LevelCallback {
        void onLevelSelected(int level);
        void onBack();
    }
    
    public LevelSelectScreen(SpriteBatch batch, LevelCallback callback) {
        this.batch = batch;
        this.callback = callback;
        
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3.5f);
        
        this.levelFont = new BitmapFont();
        this.levelFont.getData().setScale(2.5f);
        
        this.shapeRenderer = new ShapeRenderer();
        initializeLevelButtons();
    }
    
    private void initializeLevelButtons() {
        levelRects = new Rectangle[maxLevels];
        float totalWidth = (maxLevels * LEVEL_SPACING);
        float startX = (Gdx.graphics.getWidth() - totalWidth) / 2;
        float startY = Gdx.graphics.getHeight() / 2 - 45;
        
        for (int i = 0; i < maxLevels; i++) {
            levelRects[i] = new Rectangle(
                startX + (i * LEVEL_SPACING),
                startY,
                LEVEL_BUTTON_SIZE,
                LEVEL_BUTTON_SIZE
            );
        }
    }
    
    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, BG_COLOR.a);
        
        pulseAnimation += delta;
        
        handleInput();
        drawBackground();
        drawLevelSelect();
    }
    
    private void drawBackground() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Gradient background
        for (int i = 0; i < Gdx.graphics.getHeight(); i += 20) {
            if ((i / 20) % 2 == 0) {
                shapeRenderer.setColor(new Color(0.08f, 0.08f, 0.12f, 0.3f));
            } else {
                shapeRenderer.setColor(new Color(0.05f, 0.05f, 0.1f, 0.3f));
            }
            shapeRenderer.rect(0, i, Gdx.graphics.getWidth(), 20);
        }
        
        shapeRenderer.end();
    }
    
    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.LEFT)) {
            selectedLevel = Math.max(1, selectedLevel - 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.RIGHT)) {
            selectedLevel = Math.min(maxLevels, selectedLevel + 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            callback.onLevelSelected(selectedLevel);
        }
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            callback.onBack();
        }
    }
    
    private void drawLevelSelect() {
        // Draw title
        batch.begin();
        titleFont.setColor(SELECTED_COLOR);
        titleFont.draw(batch, "SELECT LEVEL", 100f, Gdx.graphics.getHeight() - 80f);
        batch.end();
        
        // Draw level buttons
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < levelRects.length; i++) {
            Rectangle btn = levelRects[i];
            
            // Button background
            shapeRenderer.setColor(BUTTON_BG);
            shapeRenderer.rect(btn.x, btn.y, btn.width, btn.height);
        }
        shapeRenderer.end();
        
        // Draw button outlines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < levelRects.length; i++) {
            Rectangle btn = levelRects[i];
            boolean isSelected = (i + 1 == selectedLevel);
            
            shapeRenderer.setColor(isSelected ? SELECTED_OUTLINE : NORMAL_OUTLINE);
            shapeRenderer.rect(btn.x, btn.y, btn.width, btn.height);
        }
        shapeRenderer.end();
        
        // Draw level numbers
        batch.begin();
        for (int i = 0; i < levelRects.length; i++) {
            Rectangle btn = levelRects[i];
            boolean isSelected = (i + 1 == selectedLevel);
            
            String levelNum = String.valueOf(i + 1);
            
            // Shadow
            levelFont.setColor(0f, 0f, 0f, 0.5f);
            float shadowX = btn.x + (btn.width - 30) / 2 + 2;
            float shadowY = btn.y + btn.height / 2 + 20;
            levelFont.draw(batch, levelNum, shadowX, shadowY);
            
            // Main text
            levelFont.setColor(isSelected ? SELECTED_COLOR : NORMAL_COLOR);
            float textX = btn.x + (btn.width - 30) / 2;
            float textY = btn.y + btn.height / 2 + 22;
            levelFont.draw(batch, levelNum, textX, textY);
            
            // Glow effect
            if (isSelected) {
                float glowAlpha = 0.3f + 0.2f * (float) Math.sin(pulseAnimation * 3);
                levelFont.setColor(SELECTED_COLOR.r, SELECTED_COLOR.g, SELECTED_COLOR.b, glowAlpha);
                levelFont.draw(batch, levelNum, textX, textY);
            }
        }
        
        // Draw difficulty info
        levelFont.getData().setScale(1.2f);
        levelFont.setColor(new Color(0.7f, 0.7f, 0.7f, 0.8f));
        
        String difficultyText = "LEVEL " + selectedLevel;
        String[] difficulties = {"EASY", "NORMAL", "HARD", "EXTREME", "INSANE"};
        if (selectedLevel >= 1 && selectedLevel <= difficulties.length) {
            difficultyText += " - " + difficulties[selectedLevel - 1];
        }
        
        levelFont.draw(batch, difficultyText, 50f, 120f);
        levelFont.draw(batch, "Use LEFT/RIGHT to select | ENTER to start | ESC to go back", 10f, 40f);
        
        levelFont.getData().setScale(2.5f);
        batch.end();
    }
    
    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (levelFont != null) levelFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
    
    @Override
    public void resize(int width, int height) {}
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void show() {}
}
