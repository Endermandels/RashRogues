package io.github.RashRogues;

import Networking.Network;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

import java.util.HashSet;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RRGame extends Game {
    public static AssetManager am = new AssetManager();
    SpriteBatch batch;
    SpriteBatch hudBatch;
    SpriteBatch debugBatch;
    ShapeRenderer shapeRenderer;
    static LaggingCamera playerCam;

    public Network network;
    public HashSet<Entity> globalEntities;

    public static Globals globals = new Globals();

    // constants
    public static final float WORLD_WIDTH = 80;
    public static final int PLAYER_SPAWN_X = 40;
    public static final int PLAYER_SPAWN_Y = 290;
    public static final float CAMERA_SIZE = 30;
    public static final float PLAYER_SIZE = 2;
    public static final float STANDARD_ENEMY_SIZE = 5;
    public static final float DOOR_SIZE = 10;
    public static final float CHEST_SIZE = 4;
    public static final float KEY_SIZE = 4;
    public static final float ARROW_SIZE = 2;
    public static final float THROWING_KNIFE_SIZE = 2;
    public static final float SMOKE_BOMB_SIZE = 4;
    public static final float SMOKE_BOMB_FUSE_DURATION = 2f;
    public static final float SMOKE_BOMB_EXPLOSION_SIZE = 10;
    public static final float SMOKE_BOMB_EXPLOSION_DURATION = 8;
    public static final float BOMBER_BOMB_SIZE = 4;
    public static final float BOMBER_BOMB_FUSE_DURATION = 2f;
    public static final float BOMBER_BOMB_EXPLOSION_SIZE = 8;
    public static final float BOMBER_BOMB_EXPLOSION_DURATION = 1;
    public static final float SWORDSMAN_SWING_SIZE = 8;
    public static final float STANDARD_PROJECTILE_SPEED = 20;
    public static final float STANDARD_PROJECTILE_DISTANCE = 40;
    public static final float STANDARD_MELEE_DURATION = 0.5f;
    public static final float STANDARD_DEATH_DURATION = 0.7f;
    public static final int HEALTH_POTION_HEAL_AMOUNT = 300;

    public static final String RSC_MONO_FONT_FILE = "Fonts/JetBrainsMono-Regular.ttf";
    public static final String RSC_MONO_FONT = "JBM.ttf";
    public static final String RSC_MONO_FONT_LARGE = "JBM_Large.ttf";
    public static final String RSC_MONO_FONT_WIN = "JBM_Win.ttf";

    // entity sprites (players, enemies, projectiles)
    public static final String RSC_ROGUE_IMG = "DefaultImages/rogue.png";
    public static final String RSC_ARCHER_IMG = "DefaultImages/archer.png";
    public static final String RSC_BOMBER_IMG = "DefaultImages/bomber.png";
    public static final String RSC_SWORDSMAN_IMG = "DefaultImages/swordsman.png";
    public static final String RSC_ARROW_IMG = "DefaultImages/arrow.png";
    public static final String RSC_THROWING_KNIFE_IMG = "DefaultImages/throwing_knife.png";
    public static final String RSC_SMOKE_BOMB_IMG = "DefaultImages/bomb.png";
    public static final String RSC_SMOKE_BOMB_EXPLOSION_IMG = "DefaultImages/explosion.png";
    public static final String RSC_SWORDSMAN_SWING_IMG = "DefaultImages/sword_swing.png";

    // entity animation sheets
    public static final String RSC_ROGUE1_SHEET = "Images/rogue sprite sheet.png";
    public static final String RSC_ROGUE2_SHEET = "Images/rogue sprite sheet 2.png";
    public static final String RSC_ROGUE3_SHEET = "Images/rogue sprite sheet 3.png";
    public static final String RSC_ROGUE4_SHEET = "Images/rogue sprite sheet 4.png";
    public static final String RSC_ARCHER_SHEET = "Images/archer sprite sheet.png";
    public static final String RSC_BOMBER_SHEET = "Images/bomber sprite sheet.png";
    public static final String RSC_SWORDSMAN_SHEET = "Images/swordsman sprite sheet.png";
    public static final String RSC_DRAGON_SHEET = "Images/dragon sprite sheet.png";
    public static final String RSC_KING_SHEET = "Images/king sprite sheet.png";
    public static final String RSC_MERCHANT_SHEET = "Images/merchant sprite sheet.png";
    public static final String RSC_BOMBER_BOMB_SHEET = "Images/bomberBomb sprite sheet.png";
    public static final String RSC_BOMBER_EXPLOSION_SHEET = "Images/bomberExplosion sprite sheet.png";
    public static final String RSC_CHEST_SHEET = "Images/chest sprite sheet.png";
    public static final String RSC_DOOR_SHEET = "Images/door sprite sheet.png";
    public static final String RSC_BOMB_GUI_SHEET = "Images/bomb GUI sprite sheet.png";
    public static final String RSC_CLOAK_GUI_SHEET = "Images/cloak GUI sprite sheet.png";

    // item/background sprites
    public static final String RSC_ROOM1_IMG = "DefaultImages/room1.png";
    public static final String RSC_ROOM2_IMG = "DefaultImages/room2.png";
    public static final String RSC_KEY_IMG = "DefaultImages/key.png";
    public static final String RSC_DOOR_IMG = "DefaultImages/door.png";
    public static final String RSC_CHEST_IMG = "DefaultImages/chest.png";
    public static final String RSC_COIN_IMG = "DefaultImages/coin.png";
    public static final String RSC_HEALTH_POTION_IMG = "DefaultImages/health_potion.png";

    // debug tools
    public static final String RSC_NET_VIEWER = "Menu/net_viewer.png";

    // particle effects
    public static final String RSC_SMOKE_PARTICLE_IMG = "Particles/particle-cloud.png";
    public static final String RSC_SMOKE_PARTICLE_EFFECT = "Particles/smoke_particles.p";

    // sounds
    public static final String RSC_HURT_SFX = "SFX/sounds/hitHurt.wav";
    public static final String RSC_HURT_ENEMY_SFX = "SFX/sounds/hitHurtEnemy.wav";
    public static final String RSC_SHOOT_SFX = "SFX/sounds/shoot.wav";
    public static final String RSC_EXPLOSION_SFX = "SFX/sounds/explosion.wav";
    public static final String RSC_DOOR_OPEN_SFX = "SFX/sounds/doorOpen.wav";
    public static final String RSC_PICK_UP_KEY_SFX = "SFX/sounds/pickupKey.wav";
    public static final String RSC_SWORD_SWIPE_SFX = "SFX/sounds/swordSwipe.wav";
    public static final String RSC_DASH_SFX = "SFX/sounds/dash.wav";

    // music
    public static final String RSC_ROOM1_MUSIC = "SFX/music/on the road to the 80s.mp3";
    public static final String RSC_ROOM2_MUSIC = "SFX/music/Stealth Surge.mp3";
    public Music room1Music;
    public Music room2Music;

    // ui
    public static final String RSC_BTN_HOST = "Buttons/host.png";
    public static final String RSC_BTN_JOIN = "Buttons/join.png";
    public static final String RSC_BTN_START_GAME = "Buttons/play.png";
    public static final String RSC_GAME_LIST = "Menu/game_list_background.png";
    public static final String RSC_GAME_LIST_ITEM = "Menu/game_list_item.png";
    public static final String RSC_HEALTH_BAR = "GUI/health_bar.png";

    @Override
    public void create() {
        FileHandleResolver resolver = new InternalFileHandleResolver();
        am.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        am.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        FreetypeFontLoader.FreeTypeFontLoaderParameter myFont = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFont.fontFileName = RSC_MONO_FONT_FILE;
        myFont.fontParameters.size = 14;
        am.load(RSC_MONO_FONT, BitmapFont.class, myFont);

        FreetypeFontLoader.FreeTypeFontLoaderParameter myFontLarge = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFontLarge.fontFileName = RSC_MONO_FONT_FILE;
        myFontLarge.fontParameters.size = 24;
        am.load(RSC_MONO_FONT_LARGE, BitmapFont.class, myFontLarge);

        FreetypeFontLoader.FreeTypeFontLoaderParameter myFontWin = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        myFontWin.fontFileName = RSC_MONO_FONT_FILE;
        myFontWin.fontParameters.size = 128;
        myFontWin.fontParameters.shadowOffsetX = 8;
        myFontWin.fontParameters.shadowOffsetY = 8;
        myFontWin.fontParameters.color = new Color(1f,0f,0f,1f);
        myFontWin.fontParameters.shadowColor = new Color(0.3f,0f,0.3f,1f);
        am.load(RSC_MONO_FONT_WIN, BitmapFont.class, myFontWin);

        am.load(RSC_ROGUE_IMG, Texture.class);
        am.load(RSC_ARCHER_IMG, Texture.class);
        am.load(RSC_BOMBER_IMG, Texture.class);
        am.load(RSC_SWORDSMAN_IMG, Texture.class);
        am.load(RSC_ARROW_IMG, Texture.class);
        am.load(RSC_THROWING_KNIFE_IMG, Texture.class);
        am.load(RSC_SMOKE_BOMB_IMG, Texture.class);
        am.load(RSC_SMOKE_BOMB_EXPLOSION_IMG, Texture.class);
        am.load(RSC_SWORDSMAN_SWING_IMG, Texture.class);

        am.load(RSC_ROGUE1_SHEET, Texture.class);
        am.load(RSC_ROGUE2_SHEET, Texture.class);
        am.load(RSC_ROGUE3_SHEET, Texture.class);
        am.load(RSC_ROGUE4_SHEET, Texture.class);
        am.load(RSC_ARCHER_SHEET, Texture.class);
        am.load(RSC_BOMBER_SHEET, Texture.class);
        am.load(RSC_SWORDSMAN_SHEET, Texture.class);
        am.load(RSC_DRAGON_SHEET, Texture.class);
        am.load(RSC_KING_SHEET, Texture.class);
        am.load(RSC_MERCHANT_SHEET, Texture.class);
        am.load(RSC_CHEST_SHEET, Texture.class);
        am.load(RSC_DOOR_SHEET, Texture.class);
        am.load(RSC_BOMB_GUI_SHEET, Texture.class);
        am.load(RSC_BOMBER_BOMB_SHEET, Texture.class);
        am.load(RSC_BOMBER_EXPLOSION_SHEET, Texture.class);
        am.load(RSC_CLOAK_GUI_SHEET, Texture.class);

        am.load(RSC_ROOM1_IMG, Texture.class);
        am.load(RSC_ROOM2_IMG, Texture.class);
        am.load(RSC_KEY_IMG, Texture.class);
        am.load(RSC_DOOR_IMG, Texture.class);
        am.load(RSC_CHEST_IMG, Texture.class);
        am.load(RSC_COIN_IMG, Texture.class);
        am.load(RSC_HEALTH_POTION_IMG, Texture.class);

        am.load(RSC_NET_VIEWER, Texture.class);

        am.load(RSC_SMOKE_PARTICLE_IMG, Texture.class);
        am.load(RSC_SMOKE_PARTICLE_EFFECT, ParticleEffect.class);

        am.load(RSC_GAME_LIST_ITEM, Texture.class);
        am.load(RSC_BTN_START_GAME, Texture.class);
        am.load(RSC_HEALTH_BAR, Texture.class);
        am.load(RSC_GAME_LIST, Texture.class);
        am.load(RSC_BTN_HOST, Texture.class);
        am.load(RSC_BTN_JOIN, Texture.class);

        am.load(RSC_SWORD_SWIPE_SFX, Sound.class);
        am.load(RSC_PICK_UP_KEY_SFX, Sound.class);
        am.load(RSC_HURT_ENEMY_SFX, Sound.class);
        am.load(RSC_EXPLOSION_SFX, Sound.class);
        am.load(RSC_DOOR_OPEN_SFX, Sound.class);
        am.load(RSC_SHOOT_SFX, Sound.class);
        am.load(RSC_HURT_SFX, Sound.class);
        am.load(RSC_DASH_SFX, Sound.class);

        room1Music = Gdx.audio.newMusic(Gdx.files.internal(RSC_ROOM1_MUSIC));
        room1Music.setLooping(true);
        room1Music.setVolume(0.05f);

        room2Music = Gdx.audio.newMusic(Gdx.files.internal(RSC_ROOM2_MUSIC));
        room2Music.setLooping(true);
        room2Music.setVolume(0.2f);

        batch = new SpriteBatch();
        hudBatch = new SpriteBatch();
        debugBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        Globals.network = new Network();
        network = Globals.network;

        globalEntities = new HashSet<>();

        float h = Gdx.graphics.getHeight();
        float w = Gdx.graphics.getWidth();
        playerCam = new LaggingCamera(CAMERA_SIZE, CAMERA_SIZE * (h/w));
        playerCam.center();
        playerCam.update();
        setScreen(new LoadScreen(this));
    }

    @Override
    public void resize(int width, int height) {
        playerCam.viewportWidth = CAMERA_SIZE;
        playerCam.viewportHeight = CAMERA_SIZE * ((float) height/width);
        playerCam.update();
        this.screen.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        hudBatch.dispose();
        am.dispose();
        this.network.dispose();
        RRGame.globals.currentScreen.dispose();
    }
}
