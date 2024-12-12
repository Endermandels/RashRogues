package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.max;

public class AnimationHandler {

    private final int PLAYER_NUM_ROWS = 5;
    private final int PLAYER_NUM_COLS = 18;
    private final int ARCHER_NUM_ROWS = 6;
    private final int ARCHER_NUM_COLS = 14;
    private final int BOMBER_NUM_ROWS = 4;
    private final int BOMBER_NUM_COLS = 19;
    private final int SWORDSMAN_NUM_ROWS = 4;
    private final int SWORDSMAN_NUM_COLS = 16;
    private final int DRAGON_NUM_ROWS = 3;
    private final int DRAGON_NUM_COLS = 13;
    private final int KING_NUM_ROWS = 4;
    private final int KING_NUM_COLS = 7;
    private final int BOMBER_BOMB_NUM_ROWS = 1;
    private final int BOMBER_BOMB_NUM_COLS = 3;
    private final int BOMBER_EXPLOSION_NUM_ROWS = 1;
    private final int BOMBER_EXPLOSION_NUM_COLS = 4;
    private final int CHEST_NUM_ROWS = 1;
    private final int CHEST_NUM_COLS = 5;
    private final int DOOR_NUM_ROWS = 1;
    private final int DOOR_NUM_COLS = 6;
    private final int MERCHANT_NUM_ROWS = 3;
    private final int MERCHANT_NUM_COLS = 4;
    private final int HEALTH_BAR_NUM_ROWS = 50;
    private final int HEALTH_BAR_NUM_COLS = 1;



    public Map<AnimationActor, Map<AnimationAction, AnimationInfo>> animations;

    /**
     * AnimationHandler handles everything for animations; only entities are animated, but not all entities are animated.
     * All individual entities need to do is call entity's functions for changing the current animation.
     * Every AnimationActor (entity that uses animation) has particular AnimationActions, but not all of them.
     * Every AnimationActor has a Default AnimationAction that is generally assigned to Idle.
     * Some items, like doors, do not have an Idle AnimationAction; indeed, their 'Idle' might be considered a static image.
     * In these cases, the Default Animation is set to a single frame. Usually the 'activated' state, so that an animation
     * can be triggered to open, Default Animation is of the thing opened, and then after the close animation, animations
     * can be turned off and will revert back to whatever the sprite used to initialize them was.
     * */

    AnimationHandler() {
        animations = new HashMap<AnimationActor, Map<AnimationAction, AnimationInfo>>();

        // players
        setPlayerAnimations(RRGame.RSC_ROGUE1_SHEET, AnimationActor.PLAYER1);
        setPlayerAnimations(RRGame.RSC_ROGUE2_SHEET, AnimationActor.PLAYER2);
        setPlayerAnimations(RRGame.RSC_ROGUE3_SHEET, AnimationActor.PLAYER3);
        setPlayerAnimations(RRGame.RSC_ROGUE4_SHEET, AnimationActor.PLAYER4);

        // archer
        Texture archerSheet = new Texture(RRGame.RSC_ARCHER_SHEET);
        TextureRegion[][] archerFrames = TextureRegion.split(archerSheet,
                archerSheet.getWidth() / ARCHER_NUM_COLS,
                archerSheet.getHeight() / ARCHER_NUM_ROWS);
        animations.put(AnimationActor.ARCHER, new HashMap<AnimationAction, AnimationInfo>(ARCHER_NUM_ROWS));
        animations.get(AnimationActor.ARCHER).put(AnimationAction.IDLE, new AnimationInfo(archerFrames, 0, 6, 0.1f));
        animations.get(AnimationActor.ARCHER).put(AnimationAction.MOVE, new AnimationInfo(archerFrames, 1, 12, 0.3f));
        animations.get(AnimationActor.ARCHER).put(AnimationAction.OPEN, new AnimationInfo(archerFrames, 2, 3, 0.1f));
        animations.get(AnimationActor.ARCHER).put(AnimationAction.ATTACK, new AnimationInfo(archerFrames, 3, 14, 0.7f));
        animations.get(AnimationActor.ARCHER).put(AnimationAction.CLOSE, new AnimationInfo(archerFrames, 4, 3, 0.1f));
        animations.get(AnimationActor.ARCHER).put(AnimationAction.DIE, new AnimationInfo(archerFrames, 5, 8, RRGame.STANDARD_DEATH_DURATION));
        animations.get(AnimationActor.ARCHER).put(AnimationAction.DEFAULT, animations.get(AnimationActor.ARCHER).get(AnimationAction.IDLE));

        // bomber
        Texture bomberSheet = new Texture(RRGame.RSC_BOMBER_SHEET);
        TextureRegion[][] bomberFrames = TextureRegion.split(bomberSheet,
                bomberSheet.getWidth() / BOMBER_NUM_COLS,
                bomberSheet.getHeight() / BOMBER_NUM_ROWS);
        animations.put(AnimationActor.BOMBER, new HashMap<AnimationAction, AnimationInfo>(BOMBER_NUM_ROWS));
        animations.get(AnimationActor.BOMBER).put(AnimationAction.IDLE, new AnimationInfo(bomberFrames, 0, 8, 0.2f));
        animations.get(AnimationActor.BOMBER).put(AnimationAction.MOVE, new AnimationInfo(bomberFrames, 1, 9, 0.5f));
        animations.get(AnimationActor.BOMBER).put(AnimationAction.ATTACK, new AnimationInfo(bomberFrames, 2, 19, 1.2f));
        animations.get(AnimationActor.BOMBER).put(AnimationAction.DIE, new AnimationInfo(bomberFrames, 3, 4, RRGame.STANDARD_DEATH_DURATION));
        animations.get(AnimationActor.BOMBER).put(AnimationAction.DEFAULT, animations.get(AnimationActor.BOMBER).get(AnimationAction.IDLE));

        // swordsman
        Texture swordsmanSheet = new Texture(RRGame.RSC_SWORDSMAN_SHEET);
        TextureRegion[][] swordsmanFrames = TextureRegion.split(swordsmanSheet,
                swordsmanSheet.getWidth() / SWORDSMAN_NUM_COLS,
                swordsmanSheet.getHeight() / SWORDSMAN_NUM_ROWS);
        animations.put(AnimationActor.SWORDSMAN, new HashMap<AnimationAction, AnimationInfo>(SWORDSMAN_NUM_ROWS));
        animations.get(AnimationActor.SWORDSMAN).put(AnimationAction.IDLE, new AnimationInfo(swordsmanFrames, 0, 10, 0.2f));
        animations.get(AnimationActor.SWORDSMAN).put(AnimationAction.MOVE, new AnimationInfo(swordsmanFrames, 1, 16, 1f));
        animations.get(AnimationActor.SWORDSMAN).put(AnimationAction.ATTACK, new AnimationInfo(swordsmanFrames, 2, 14, 1f));
        animations.get(AnimationActor.SWORDSMAN).put(AnimationAction.DIE, new AnimationInfo(swordsmanFrames, 3, 4, RRGame.STANDARD_DEATH_DURATION));
        animations.get(AnimationActor.SWORDSMAN).put(AnimationAction.DEFAULT, animations.get(AnimationActor.SWORDSMAN).get(AnimationAction.IDLE));

        // dragon
        Texture dragonSheet = new Texture(RRGame.RSC_DRAGON_SHEET);
        TextureRegion[][] dragonFrames = TextureRegion.split(dragonSheet,
                dragonSheet.getWidth() / DRAGON_NUM_COLS,
                dragonSheet.getHeight() / DRAGON_NUM_ROWS);
        animations.put(AnimationActor.DRAGON, new HashMap<AnimationAction, AnimationInfo>(DRAGON_NUM_ROWS));
        animations.get(AnimationActor.DRAGON).put(AnimationAction.MOVE, new AnimationInfo(dragonFrames, 0, 4, 0.1f));
        animations.get(AnimationActor.DRAGON).put(AnimationAction.ATTACK, new AnimationInfo(dragonFrames, 1, 13, 0.6f));
        animations.get(AnimationActor.DRAGON).put(AnimationAction.DIE, new AnimationInfo(dragonFrames, 2, 4, RRGame.STANDARD_DEATH_DURATION));
        animations.get(AnimationActor.DRAGON).put(AnimationAction.DEFAULT, animations.get(AnimationActor.DRAGON).get(AnimationAction.MOVE));


        // king
        Texture kingSheet = new Texture(RRGame.RSC_KING_SHEET);
        TextureRegion[][] kingFrames = TextureRegion.split(kingSheet,
                kingSheet.getWidth() / KING_NUM_COLS,
                kingSheet.getHeight() / KING_NUM_ROWS);
        animations.put(AnimationActor.KING, new HashMap<AnimationAction, AnimationInfo>(KING_NUM_ROWS));
        animations.get(AnimationActor.KING).put(AnimationAction.CLOSE, new AnimationInfo(kingFrames, 0, 5, 0.1f));
        animations.get(AnimationActor.KING).put(AnimationAction.OPEN, new AnimationInfo(kingFrames, 1, 3, 0.6f));
        animations.get(AnimationActor.KING).put(AnimationAction.IDLE, new AnimationInfo(kingFrames, 2, 4, 1f));
        animations.get(AnimationActor.KING).put(AnimationAction.DIE, new AnimationInfo(kingFrames, 3, 7, RRGame.STANDARD_DEATH_DURATION));
        animations.get(AnimationActor.KING).put(AnimationAction.DEFAULT, animations.get(AnimationActor.KING).get(AnimationAction.IDLE));


        // bomber bomb
        Texture bomberBombSheet = new Texture(RRGame.RSC_BOMBER_BOMB_SHEET);
        TextureRegion[][] bomberBombFrames = TextureRegion.split(bomberBombSheet,
                bomberBombSheet.getWidth() / BOMBER_BOMB_NUM_COLS,
                bomberBombSheet.getHeight() / BOMBER_BOMB_NUM_ROWS);
        animations.put(AnimationActor.BOMBER_BOMB, new HashMap<AnimationAction, AnimationInfo>(BOMBER_BOMB_NUM_ROWS));
        animations.get(AnimationActor.BOMBER_BOMB).put(AnimationAction.ATTACK, new AnimationInfo(bomberBombFrames, 0, 3, 0.2f));
        animations.get(AnimationActor.BOMBER_BOMB).put(AnimationAction.DEFAULT, new AnimationInfo(bomberBombFrames, 0, 1, 0.1f));

        // bomber explosion
        Texture bomberExplosionSheet = new Texture(RRGame.RSC_BOMBER_EXPLOSION_SHEET);
        TextureRegion[][] bomberExplosionFrames = TextureRegion.split(bomberExplosionSheet,
                bomberExplosionSheet.getWidth() / BOMBER_EXPLOSION_NUM_COLS,
                bomberExplosionSheet.getHeight() / BOMBER_EXPLOSION_NUM_ROWS);
        animations.put(AnimationActor.BOMBER_EXPLOSION, new HashMap<AnimationAction, AnimationInfo>(BOMBER_EXPLOSION_NUM_ROWS));
        animations.get(AnimationActor.BOMBER_EXPLOSION).put(AnimationAction.ATTACK, new AnimationInfo(bomberExplosionFrames, 0, 4, RRGame.BOMBER_BOMB_EXPLOSION_DURATION));
        animations.get(AnimationActor.BOMBER_EXPLOSION).put(AnimationAction.DEFAULT, new AnimationInfo(bomberExplosionFrames, 0, 1, 0.1f));

        // chest
        Texture chestSheet = new Texture(RRGame.RSC_CHEST_SHEET);
        TextureRegion[][] chestFrames = TextureRegion.split(chestSheet,
                chestSheet.getWidth() / CHEST_NUM_COLS,
                chestSheet.getHeight() / CHEST_NUM_ROWS);
        animations.put(AnimationActor.CHEST, new HashMap<AnimationAction, AnimationInfo>(CHEST_NUM_ROWS));
        animations.get(AnimationActor.CHEST).put(AnimationAction.OPEN, new AnimationInfo(chestFrames, 0, 5, 0.2f));
        animations.get(AnimationActor.CHEST).put(AnimationAction.CLOSE, new AnimationInfo(chestFrames, 0, 5, 0.2f, true));
        animations.get(AnimationActor.CHEST).put(AnimationAction.DEFAULT, new AnimationInfo(chestFrames, 0, 1, 0.1f, 4));

        // door
        Texture doorSheet = new Texture(RRGame.RSC_DOOR_SHEET);
        TextureRegion[][] doorFrames = TextureRegion.split(doorSheet,
                doorSheet.getWidth() / DOOR_NUM_COLS,
                doorSheet.getHeight() / DOOR_NUM_ROWS);
        animations.put(AnimationActor.DOOR, new HashMap<AnimationAction, AnimationInfo>(DOOR_NUM_ROWS));
        animations.get(AnimationActor.DOOR).put(AnimationAction.OPEN, new AnimationInfo(doorFrames, 0, 6, 0.8f));
        animations.get(AnimationActor.DOOR).put(AnimationAction.CLOSE, new AnimationInfo(doorFrames, 0, 6, 0.8f, true));
        animations.get(AnimationActor.DOOR).put(AnimationAction.DEFAULT, new AnimationInfo(doorFrames, 0, 1, 0.1f, 5));

        // merchant
        Texture merchantSheet = new Texture(RRGame.RSC_MERCHANT_SHEET);
        TextureRegion[][] merchantFrames = TextureRegion.split(merchantSheet,
                merchantSheet.getWidth() / MERCHANT_NUM_COLS,
                merchantSheet.getHeight() / MERCHANT_NUM_ROWS);
        animations.put(AnimationActor.MERCHANT, new HashMap<AnimationAction, AnimationInfo>(MERCHANT_NUM_ROWS));
        animations.get(AnimationActor.MERCHANT).put(AnimationAction.IDLE, new AnimationInfo(merchantFrames, 0, 4, 0.2f));
        animations.get(AnimationActor.MERCHANT).put(AnimationAction.OPEN, new AnimationInfo(merchantFrames, 1, 3, 0.5f));
        animations.get(AnimationActor.MERCHANT).put(AnimationAction.CLOSE, new AnimationInfo(merchantFrames, 2, 3, 0.5f));
        animations.get(AnimationActor.MERCHANT).put(AnimationAction.DEFAULT, animations.get(AnimationActor.MERCHANT).get(AnimationAction.IDLE));

        // health bar (different from others)
        Texture healthBarSheet = new Texture(RRGame.RSC_HEALTH_BAR);
        TextureRegion[][] healthBarFrames = TextureRegion.split(healthBarSheet,
                healthBarSheet.getWidth() / HEALTH_BAR_NUM_COLS,
                healthBarSheet.getHeight() / HEALTH_BAR_NUM_ROWS);
        setHealthBarAnimations(healthBarFrames, AnimationActor.HEALTH_BAR_0, 0);
        setHealthBarAnimations(healthBarFrames, AnimationActor.HEALTH_BAR_1, 1);
        setHealthBarAnimations(healthBarFrames, AnimationActor.HEALTH_BAR_2, 2);
        setHealthBarAnimations(healthBarFrames, AnimationActor.HEALTH_BAR_3, 3);
        setHealthBarAnimations(healthBarFrames, AnimationActor.HEALTH_BAR_4, 4);
        setHealthBarAnimations(healthBarFrames, AnimationActor.HEALTH_BAR_5, 5);
        setHealthBarAnimations(healthBarFrames, AnimationActor.HEALTH_BAR_6, 6);
        setHealthBarAnimations(healthBarFrames, AnimationActor.HEALTH_BAR_7, 7);
        animations.put(AnimationActor.HEALTH_BAR_8, new HashMap<AnimationAction, AnimationInfo>(HEALTH_BAR_NUM_ROWS));
        animations.get(AnimationActor.HEALTH_BAR_8).put(AnimationAction.DEFAULT, new AnimationInfo(healthBarFrames,1, 1, 0.1f, 0));

        // things not finished yet
//        animations.put(AnimationActor.SMOKE_BOMB, null);
//        animations.put(AnimationActor.SMOKE_BOMB_EXPLOSION, null);
//        animations.put(AnimationActor.SWORDSMAN_SWING, null);
    }


    // just to reduce code and make sure we only have to change one number for all player animation durations:
    private void setPlayerAnimations(String sheetName, AnimationActor playerNum) {
        Texture playerSheet = new Texture(sheetName);
        TextureRegion[][] playerFrames = TextureRegion.split(playerSheet,
                playerSheet.getWidth() / PLAYER_NUM_COLS,
                playerSheet.getHeight() / PLAYER_NUM_ROWS);
        animations.put(playerNum, new HashMap<AnimationAction, AnimationInfo>(PLAYER_NUM_ROWS));
        animations.get(playerNum).put(AnimationAction.IDLE, new AnimationInfo(playerFrames, 0, 4, 0.2f));
        animations.get(playerNum).put(AnimationAction.MOVE, new AnimationInfo(playerFrames, 1, 18, 0.5f));
        animations.get(playerNum).put(AnimationAction.ATTACK, new AnimationInfo(playerFrames, 2, 17, 0.1f));
        animations.get(playerNum).put(AnimationAction.HURT, new AnimationInfo(playerFrames, 3, 4, 0.1f));
        animations.get(playerNum).put(AnimationAction.DIE, new AnimationInfo(playerFrames, 4, 14, RRGame.STANDARD_DEATH_DURATION));
        animations.get(playerNum).put(AnimationAction.DEFAULT, animations.get(playerNum).get(AnimationAction.IDLE));

    }

    private void setHealthBarAnimations(TextureRegion[][] healthBarFrames, AnimationActor healthBarActor, int healthBarNum) {
        animations.put(healthBarActor, new HashMap<AnimationAction, AnimationInfo>(HEALTH_BAR_NUM_COLS));

        TextureRegion[][] validHealthBarFrames = new TextureRegion[1][6];
        for (int ii = 0; ii < 6; ii++) {
            validHealthBarFrames[0][ii] = healthBarFrames[HEALTH_BAR_NUM_ROWS-6*(healthBarNum+1)+ii][0];
        }
        animations.get(healthBarActor).put(AnimationAction.HURT, new AnimationInfo(validHealthBarFrames,0, 6, 0.8f));
        animations.get(healthBarActor).put(AnimationAction.DEFAULT, new AnimationInfo(validHealthBarFrames,0, 1, 0.1f, 5));
    }

}

class AnimationInfo {

    private Animation<TextureRegion> animation;

    public AnimationInfo(TextureRegion[][] totalFrames, int row, int numFrames, float totalTime, boolean reversed) {
        if (reversed) {
            this.animation = new Animation<TextureRegion>(totalTime/numFrames, setReverseTextureRegion(totalFrames, row, numFrames));
        }
        else {
            this.animation = new Animation<TextureRegion>(totalTime/numFrames, setTextureRegion(totalFrames, row, numFrames));
        }
    }

    public AnimationInfo(TextureRegion[][] totalFrames, int row, int numFrames, float totalTime) {
        this(totalFrames, row, numFrames, totalTime, false);
    }

    public AnimationInfo(TextureRegion[][] totalFrames, int row, int numFrames, float totalTime, int frameNum) {
        this.animation = new Animation<TextureRegion>(totalTime/numFrames, setSingleFrameTextureRegion(
                totalFrames, row, numFrames, frameNum));
    }

    private TextureRegion[] setTextureRegion(TextureRegion[][] totalFrames, int row, int numFramesInRow) {
        TextureRegion[] animationFrames = new TextureRegion[numFramesInRow];
        int index = 0;
        for (int i = 0; i < numFramesInRow; i++) {
            animationFrames[index++] = totalFrames[row][i];
        }
        return animationFrames;
    }

    private TextureRegion[] setReverseTextureRegion(TextureRegion[][] totalFrames, int row, int numFramesInRow) {
        TextureRegion[] animationFrames = new TextureRegion[numFramesInRow];
        int index = 0;
        for (int i = numFramesInRow-1; i > -1; i--) {
            animationFrames[index++] = totalFrames[row][i];
        }
        return animationFrames;
    }

    private TextureRegion[] setSingleFrameTextureRegion(TextureRegion[][] totalFrames, int row, int numFramesInRow, int frameNum) {
        TextureRegion[] animationFrames = new TextureRegion[numFramesInRow];
        animationFrames[0] = totalFrames[row][frameNum];
        return animationFrames;
    }

    public TextureRegion getCurrentFrame(float animationTimer) { return animation.getKeyFrame(animationTimer); }
    public boolean isAnimationFinished(float animationTimer) { return animation.isAnimationFinished(animationTimer); }
}

enum AnimationActor {
    PLAYER1,
    PLAYER2,
    PLAYER3,
    PLAYER4,
    ARCHER,
    BOMBER,
    SWORDSMAN,
    DRAGON,
    KING,
    BOMBER_BOMB,
    BOMBER_EXPLOSION,
    BOMB_GUI,
    CHEST,
    DOOR,
    MERCHANT,
    HEALTH_BAR_0,
    HEALTH_BAR_1,
    HEALTH_BAR_2,
    HEALTH_BAR_3,
    HEALTH_BAR_4,
    HEALTH_BAR_5,
    HEALTH_BAR_6,
    HEALTH_BAR_7,
    HEALTH_BAR_8,
    // these don't exist yet:
    SMOKE_BOMB,
    SMOKE_BOMB_EXPLOSION,
    SWORDSMAN_SWING,
}

enum AnimationAction {
    DEFAULT,
    IDLE,
    MOVE,
    ATTACK,
    HURT,
    DIE,
    OPEN,
    CLOSE
}