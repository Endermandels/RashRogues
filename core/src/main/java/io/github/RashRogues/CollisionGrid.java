package io.github.RashRogues;

import UI.Button;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class CollisionGrid {

    // if hitbox too big then uh oh
    private final int COLLISION_GRID_ROWS = 8;
    private final int COLLISION_GRID_COLS = 8;
    private int collisionGridRowSize = (int) (RRGame.WORLD_WIDTH / COLLISION_GRID_ROWS); // these change immediately
    private int collisionGridColSize = (int) (RRGame.WORLD_WIDTH / COLLISION_GRID_COLS);
    private LinkedHashSet<HitBox>[][] collisionGridHitBoxes;
    private LinkedHashSet<HurtBox>[][] collisionGridHurtBoxes;

    CollisionGrid() {
        collisionGridHitBoxes = new LinkedHashSet[COLLISION_GRID_ROWS][COLLISION_GRID_COLS];
        collisionGridHurtBoxes = new LinkedHashSet[COLLISION_GRID_ROWS][COLLISION_GRID_COLS];
        for (int i = 0; i < COLLISION_GRID_ROWS; i++) {
            for (int j = 0; j < COLLISION_GRID_COLS; j++) {
                collisionGridHitBoxes[i][j] = new LinkedHashSet<HitBox>();
                collisionGridHurtBoxes[i][j] = new LinkedHashSet<HurtBox>();
            }
        }
    }

    protected void updateCollisionGridRoomValues(int roomWidth, int roomHeight) {
        collisionGridRowSize = roomHeight / COLLISION_GRID_ROWS;
        collisionGridColSize = roomWidth / COLLISION_GRID_COLS;
    }

    protected void populateCollisionGrid(HashSet<Entity> localEntities) {
        // first, clear the grid
        for (int i = 0; i < COLLISION_GRID_ROWS; i++) {
            for (int j = 0; j < COLLISION_GRID_COLS; j++) {
                collisionGridHitBoxes[i][j] = new LinkedHashSet<HitBox>();
                collisionGridHurtBoxes[i][j] = new LinkedHashSet<HurtBox>();
            }
        }

        for (Entity e : localEntities) {
            if (e instanceof Button) { continue; }

            // populate the hitBoxes
            int leftSideColNum = clampedCollisionColNum((int) (e.hitBox.x) / collisionGridColSize);
            int rightSideColNum = clampedCollisionColNum((int) (e.hitBox.x + e.hitBox.width) / collisionGridColSize);
            int bottomSideRowNum = clampedCollisionRowNum((int) (e.hitBox.y) / collisionGridRowSize);
            int topSideRowNum = clampedCollisionRowNum((int) (e.hitBox.y + e.hitBox.height) / collisionGridRowSize);
            collisionGridHitBoxes[bottomSideRowNum][leftSideColNum].add(e.hitBox);
            collisionGridHitBoxes[bottomSideRowNum][rightSideColNum].add(e.hitBox);
            collisionGridHitBoxes[topSideRowNum][leftSideColNum].add(e.hitBox);
            collisionGridHitBoxes[topSideRowNum][rightSideColNum].add(e.hitBox);

            // populate the hurtBoxes if not a projectile
            if (e instanceof Player) {
                Player player = (Player) e;
                leftSideColNum = clampedCollisionColNum((int) (player.hurtBox.x) / collisionGridColSize);
                rightSideColNum = clampedCollisionColNum((int) (player.hurtBox.x + player.hurtBox.width) / collisionGridColSize);
                bottomSideRowNum = clampedCollisionRowNum((int) (player.hurtBox.y) / collisionGridRowSize);
                topSideRowNum = clampedCollisionRowNum((int) (player.hurtBox.y + player.hurtBox.height) / collisionGridRowSize);
                collisionGridHurtBoxes[bottomSideRowNum][leftSideColNum].add(player.hurtBox);
                collisionGridHurtBoxes[bottomSideRowNum][rightSideColNum].add(player.hurtBox);
                collisionGridHurtBoxes[topSideRowNum][leftSideColNum].add(player.hurtBox);
                collisionGridHurtBoxes[topSideRowNum][rightSideColNum].add(player.hurtBox);
            }
            else if (e instanceof Enemy) {
                Enemy enemy = (Enemy) e;
                leftSideColNum = clampedCollisionColNum((int) (enemy.hurtBox.x) / collisionGridColSize);
                rightSideColNum = clampedCollisionColNum((int) (enemy.hurtBox.x + enemy.hurtBox.width) / collisionGridColSize);
                bottomSideRowNum = clampedCollisionRowNum((int) (enemy.hurtBox.y) / collisionGridRowSize);
                topSideRowNum = clampedCollisionRowNum((int) (enemy.hurtBox.y + enemy.hurtBox.height) / collisionGridRowSize);
                collisionGridHurtBoxes[bottomSideRowNum][leftSideColNum].add(enemy.hurtBox);
                collisionGridHurtBoxes[bottomSideRowNum][rightSideColNum].add(enemy.hurtBox);
                collisionGridHurtBoxes[topSideRowNum][leftSideColNum].add(enemy.hurtBox);
                collisionGridHurtBoxes[topSideRowNum][rightSideColNum].add(enemy.hurtBox);
            }
        }
    }

    private int clampedCollisionColNum(int col) {
        return Math.max(0, Math.min(COLLISION_GRID_COLS - 1, col));
    }

    private int clampedCollisionRowNum(int row) {
        return Math.max(0, Math.min(COLLISION_GRID_ROWS - 1, row));
    }

    protected void calculateCollisions() {
        // absolutely horrid looking nested for loops, but it has to be done and this is better than O(n*m)
        // more hitboxes than hurtboxes, so calculate if each hitbox hits a hurtbox
        for (int i = 0; i < COLLISION_GRID_ROWS; i++) {
            for (int j = 0; j < COLLISION_GRID_COLS; j++) {
                LinkedHashSet<HitBox> hitBoxes = collisionGridHitBoxes[i][j];
                LinkedHashSet<HurtBox> hurtBoxes = collisionGridHurtBoxes[i][j];
                for (Iterator<HitBox> hitBoxIterator = hitBoxes.iterator(); hitBoxIterator.hasNext();) {
                    HitBox hitBox = hitBoxIterator.next();
                    for (Iterator<HurtBox> hurtBoxIterator = hurtBoxes.iterator(); hurtBoxIterator.hasNext();) {
                        HurtBox hurtBox = hurtBoxIterator.next();
                        if (hitBox.overlaps(hurtBox)) {
                            if (RRGame.globals.pid == 0){   //only apply collision damage on the server.
                                hitBox.hitHurtBox(hurtBox);
                            }
                        }
                    }
                }
            }
        }
    }
}
