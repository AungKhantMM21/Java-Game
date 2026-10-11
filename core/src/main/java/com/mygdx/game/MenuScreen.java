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

public class MenuScreen implements Screen {
    private SpriteBatch batch;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private ShapeRenderer shape;
    private MenuCallback callback;
    
    private int selectedOption = 0;
    private Rectangle[] buttonRects;
    private String[] buttonLabels;
    private boolean[] buttonEnabled;
    
    private static final int BUTTON_WIDTH = 300;
    private static final int BUTTON_HEIGHT = 60;
    private static final int BUTTON_SPACING = 80;
    private static final int BUTTON_START_X = 80;
    private static final Color SELECTED_COLOR = new Color(1f, 0.84f, 0f, 1f);      // Gold
    private static final Color SELECTED_OUTLINE = new Color(1f, 0.65f, 0f, 1f);   // Dark gold
    private static final Color NORMAL_COLOR = new Color(0.3f, 1f, 0.4f, 1f);      // Green
    private static final Color NORMAL_OUTLINE = new Color(0.2f, 0.8f, 0.3f, 1f);  // Dark green
    private static final Color DISABLED_COLOR = new Color(0.4f, 0.4f, 0.4f, 1f);  // Gray
    private static final Color BUTTON_BG_SELECTED = new Color(0.2f, 0.3f, 0.15f, 1f);
    private static final Color BUTTON_BG_NORMAL = new Color(0.1f, 0.15f, 0.08f, 1f);
    private static final Color BG_COLOR = new Color(0.05f, 0.05f, 0.1f, 1f);
    
    private float pulseAnimation = 0;
    
    public interface MenuCallback {
        void onPlay();
        void onResume();
        void onRestart();
        void onQuit();
    }
    
    public MenuScreen(SpriteBatch batch, MenuCallback callback, boolean canResume) {
        this.batch = batch;
        this.callback = callback;
        
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(3.5f);
        
        this.buttonFont = new BitmapFont();
        this.buttonFont.getData().setScale(2f);
        
        this.shape = new ShapeRenderer();
        
        if (canResume) {
            buttonLabels = new String[]{"PLAY", "RESUME", "RESTART", "QUIT"};
            buttonEnabled = new boolean[]{true, true, true, true};
        } else {
            buttonLabels = new String[]{"PLAY", "RESTART", "QUIT"};
            buttonEnabled = new boolean[]{true, true, true};
        }
        
        initializeButtons();
    }
    
    private void initializeButtons() {
        buttonRects = new Rectangle[buttonLabels.length];
        float startY = Gdx.graphics.getHeight() - 350;
        
        for (int i = 0; i < buttonLabels.length; i++) {
            buttonRects[i] = new Rectangle(
                BUTTON_START_X,
                startY - (i * BUTTON_SPACING),
                BUTTON_WIDTH,
                BUTTON_HEIGHT
            );
        }
    }
    
    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, BG_COLOR.a);
        
        pulseAnimation += delta;
        
        handleInput();
        drawBackground();
        drawMenu();
    }
    
    private void drawBackground() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw gradient-like background with horizontal stripes
        for (int i = 0; i < Gdx.graphics.getHeight(); i += 20) {
            if ((i / 20) % 2 == 0) {
                shape.setColor(new Color(0.08f, 0.08f, 0.12f, 0.3f));
            } else {
                shape.setColor(new Color(0.05f, 0.05f, 0.1f, 0.3f));
            }
            shape.rect(0, i, Gdx.graphics.getWidth(), 20);
        }
        
        shape.end();
    }
    
    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.UP)) {
            selectedOption = (selectedOption - 1 + buttonLabels.length) % buttonLabels.length;
        }
        if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            selectedOption = (selectedOption + 1) % buttonLabels.length;
        }
        if (Gdx.input.isKeyJustPressed(Keys.ENTER)) {
            if (buttonEnabled[selectedOption]) {
                selectOption();
            }
        }
    }
    
    private void selectOption() {
        String label = buttonLabels[selectedOption];
        
        switch (label) {
            case "PLAY":
                callback.onPlay();
                break;
            case "RESUME":
                callback.onResume();
                break;
            case "RESTART":
                callback.onRestart();
                break;
            case "QUIT":
                callback.onQuit();
                break;
        }
    }
    
    private void drawMenu() {
        // Draw title at top center
        batch.begin();
        titleFont.setColor(SELECTED_COLOR);
        titleFont.getData().setScale(4.5f);
        
        // Position title at top center with approximate centering
        float centerX = Gdx.graphics.getWidth() / 2;
        float titleY = Gdx.graphics.getHeight() - 80;
        
        // Draw "LIQUID" - approximately centered
        titleFont.draw(batch, "LIQUID", centerX - 180, titleY);
        // Draw "RUNNER" - approximately centered
        titleFont.draw(batch, "RUNNER", centerX - 180, titleY - 70);
        
        titleFont.getData().setScale(3.5f);
        batch.end();
        
        // Draw buttons on the left side (below the top area)
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < buttonRects.length; i++) {
            Rectangle btn = buttonRects[i];
            boolean isSelected = (i == selectedOption);
            
            // Button background
            if (isSelected) {
                shape.setColor(BUTTON_BG_SELECTED);
            } else {
                shape.setColor(BUTTON_BG_NORMAL);
            }
            shape.rect(btn.x, btn.y, btn.width, btn.height);
        }
        shape.end();
        
        // Draw button outlines with thicker lines
        shape.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < buttonRects.length; i++) {
            Rectangle btn = buttonRects[i];
            boolean isSelected = (i == selectedOption);
            
            if (!buttonEnabled[i]) {
                shape.setColor(DISABLED_COLOR);
            } else {
                shape.setColor(isSelected ? SELECTED_OUTLINE : NORMAL_OUTLINE);
            }
            shape.rect(btn.x, btn.y, btn.width, btn.height);
            
            // Draw extra outline for selected button
            if (isSelected && buttonEnabled[i]) {
                shape.rect(btn.x - 2, btn.y - 2, btn.width + 4, btn.height + 4);
            }
        }
        shape.end();
        
        // Draw button text with left alignment
        batch.begin();
        buttonFont.getData().setScale(2f);
        for (int i = 0; i < buttonLabels.length; i++) {
            Rectangle btn = buttonRects[i];
            boolean isSelected = (i == selectedOption);
            
            // Calculate text position (left-aligned with padding)
            float textX = btn.x + 30;
            float textY = btn.y + btn.height / 2 + 12;
            
            // Text shadow
            buttonFont.setColor(0, 0, 0, 0.6f);
            buttonFont.draw(batch, buttonLabels[i], textX + 2, textY - 2);
            
            // Main text
            if (!buttonEnabled[i]) {
                buttonFont.setColor(DISABLED_COLOR);
            } else {
                buttonFont.setColor(isSelected ? Color.WHITE : NORMAL_COLOR);
            }
            
            buttonFont.draw(batch, buttonLabels[i], textX, textY);
            
            // Glow effect for selected
            if (isSelected && buttonEnabled[i]) {
                float glowAlpha = 0.4f + 0.3f * (float) Math.sin(pulseAnimation * 3);
                buttonFont.setColor(SELECTED_COLOR.r, SELECTED_COLOR.g, SELECTED_COLOR.b, glowAlpha);
                buttonFont.draw(batch, buttonLabels[i], textX, textY);
            }
        }
        
        // Draw info text at the bottom
        buttonFont.setColor(new Color(0.5f, 0.8f, 0.6f, 0.7f));
        buttonFont.getData().setScale(1f);
        buttonFont.draw(batch, "Navigate: UP/DOWN  |  Select: ENTER", BUTTON_START_X, 25);
        buttonFont.getData().setScale(2f);
        batch.end();
    }
    
    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (shape != null) shape.dispose();
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
