package io.github.RashRogues;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class PlayScreen extends ScreenAdapter {

    private RRGame game;
    private Player player;
    private ArrayList<Room> rooms;
    private Room currentRoom;

    public PlayScreen(RRGame game) {
        this.game = game;
        this.player = new Player(game.am.get(RRGame.RSC_ROGUE_IMG), RRGame.PLAYER_SPAWN_X, RRGame.PLAYER_SPAWN_Y, RRGame.PLAYER_SIZE);
        loadRooms();
        setNextRoom();
    }

    @Override
    public void show() {
        Gdx.app.log("PlayScreen", "show");
    }

    public void update(float delta) {

        // update room/objects

        // update player(s)
        player.takeInput();
        player.update(delta);
        game.playerCam.moveToPlayer(player.getX()+player.getWidth()/2f, player.getY()+player.getHeight()/2f, delta);

        // update enemies

        // update projectiles

        // update misc

    }

    @Override
    public void render(float delta) {
        update(delta);
        game.playerCam.update();
        game.batch.setProjectionMatrix(game.playerCam.combined);
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        game.batch.begin();

        // draw room/objects
        currentRoom.draw(game.batch);

        // draw player(s)
        player.draw(game.batch);

        // draw enemies

        // draw projectiles

        // draw misc

        game.batch.end();
    }

    private void loadRooms() {
        this.rooms = new ArrayList<>();
        rooms.add(new Room(game.am.get(RRGame.RSC_ROOM1_IMG)));
        // other rooms will go below here
    }

    private void setNextRoom() {
        if (currentRoom == null) {
            // first room
            currentRoom = rooms.get(0);
        }
        else if (rooms.indexOf(currentRoom) >= rooms.size() - 1) {
            // last room
            // win screen?
            // this will crash the game for now most likely
            return;
        }
        else {
            currentRoom = rooms.get(rooms.indexOf(currentRoom) + 1);
        }
        game.playerCam.changeWorldSize(currentRoom.roomWidth, currentRoom.roomHeight);
    }
}
