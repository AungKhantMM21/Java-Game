package com.mygdx.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/** One stage's layout: its width, walls, holes, patrolling dogs, the optional spell and the exit. */
public class Level {

    private final float width;
    private final Array<Rectangle> walls = new Array<>();
    private final Array<Rectangle> holes = new Array<>();
    private final Array<Dog> dogs = new Array<>();
    private Rectangle spell;
    private Rectangle exit;

    private Level(float width) {
        this.width = width;
    }

    /** Builds the layout of stage 1, 2 or 3; any higher number builds stage 3. */
    public static Level forStage(int level) {
        Level stage;

        if (level == 1) {
            stage = new Level(3000f);

            stage.addWall(500, 60, 50);
            stage.addHole(850, 75);
            stage.addWall(1150, 65, 55);

            stage.addHole(2250, 80);

            stage.addDog(280, 200, 420, 100);
            stage.addDog(2450, 2350, 2650, 110);

            stage.addExit(2800);
        }

        else if (level == 2) {
            stage = new Level(3600f);

            stage.addWall(500, 60, 50);
            stage.addHole(850, 75);
            stage.addWall(1150, 65, 55);

            // Spell pickup for Stage 2.
            stage.addSpell(1400);

            stage.addWall(1850, 70, 170);
            stage.addHole(2250, 80);
            stage.addWall(2600, 70, 55);

            // Tall, unjumpable wall near the ending.
            stage.addWall(2950, 70, 170);

            stage.addDog(280, 200, 420, 100);
            stage.addDog(1600, 1500, 1750, 120);
            stage.addDog(3250, 3100, 3400, 120);

            stage.addExit(3410);
        }

        else {
            stage = new Level(4200f);

            stage.addWall(500, 60, 50);
            stage.addHole(850, 75);
            stage.addWall(1150, 65, 55);

            // Spell pickup for Stage 3.
            stage.addSpell(1400);

            stage.addWall(1850, 70, 170);
            stage.addHole(2250, 80);
            stage.addWall(2600, 70, 55);
            stage.addWall(2950, 70, 170);

            stage.addHole(3450, 85);

            // Final tall, unjumpable wall.
            stage.addWall(3750, 70, 170);

            stage.addDog(280, 200, 420, 100);
            stage.addDog(1600, 1500, 1750, 120);
            stage.addDog(2500, 2400, 2700, 120);
            stage.addDog(3500, 3300, 3600, 130);

            stage.addExit(4020);
        }

        return stage;
    }

    private void addWall(float x, float width, float height) {
        walls.add(new Rectangle(x, MyGdxGame.GROUND_Y, width, height));
    }

    private void addHole(float x, float width) {
        holes.add(new Rectangle(x, 0f, width, MyGdxGame.GROUND_Y));
    }

    private void addDog(
        float x, float minX, float maxX, float speed
    ) {
        dogs.add(new Dog(x, minX, maxX, speed));
    }

    private void addSpell(float x) {
        spell = new Rectangle(
            x, MyGdxGame.GROUND_Y + 55f, 38f, 38f
        );
    }

    private void addExit(float x) {
        exit = new Rectangle(
            x, MyGdxGame.GROUND_Y, 55f, 100f
        );
    }

    /** Moves every dog for one frame. */
    public void updateDogs(float delta) {
        for (Dog dog : dogs) {
            dog.update(delta);
        }
    }

    /** True if a body spanning {@code x} to {@code x + width} is above a hole. */
    public boolean overlapsHole(float x, float width) {
        for (Rectangle hole : holes) {
            if (x + width > hole.x
                && x < hole.x + hole.width) {
                return true;
            }
        }
        return false;
    }

    public float getWidth() {
        return width;
    }

    public Array<Rectangle> getWalls() {
        return walls;
    }

    public Array<Rectangle> getHoles() {
        return holes;
    }

    public Array<Dog> getDogs() {
        return dogs;
    }

    /** The spell pickup, or {@code null} on a stage without one. */
    public Rectangle getSpell() {
        return spell;
    }

    public Rectangle getExit() {
        return exit;
    }
}
