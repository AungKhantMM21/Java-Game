package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Enemies {
    public Rectangle hitbox;
    public float speed = 75f;
    public boolean movingRight = true;

    // NEW: We track where the dog spawns so it can patrol a specific area
    private float startX;
    private float patrolDistance = 250f; // Walks 250 pixels left and right from its spawn

    public Enemies(float x, float y, float width, float height) {
        hitbox = new Rectangle(x, y, width, height);
        startX = x; // Remember starting position

        if (Math.random() > 0.5) {
            movingRight = false;
        }
    }

    // Tightened collision box so the player doesn't die touching invisible empty space
    public Rectangle getCollisionBox() {
        return new Rectangle(
            hitbox.x + 15,
            hitbox.y,
            hitbox.width - 30,
            hitbox.height - 10
        );
    }

    public void move(float deltaTime) {
        if (movingRight) {
            hitbox.x += speed * deltaTime;
        } else {
            hitbox.x -= speed * deltaTime;
        }

        // Reverse direction based on their local patrol area, not the screen edge
        if (hitbox.x <= startX - patrolDistance) {
            hitbox.x = startX - patrolDistance;
            movingRight = true;
        } else if (hitbox.x >= startX + patrolDistance) {
            hitbox.x = startX + patrolDistance;
            movingRight = false;
        }
    }

    public void draw(SpriteBatch batch, Texture texture) {
        boolean flipX = !movingRight;
        batch.draw(texture,
            hitbox.x, hitbox.y, hitbox.width, hitbox.height,
            0, 0, texture.getWidth(), texture.getHeight(),
            flipX, false);
    }
}
