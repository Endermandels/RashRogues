package io.github.RashRogues;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class LaggingCamera extends OrthographicCamera {

    private final float LERP = 10f;
    private final float MIN_DIST_FROM_CAM_EDGE = 3f;

    public float roomWidth;
    public float roomHeight;

    public LaggingCamera(float viewportWidth, float viewportHeight) {
        super(viewportWidth, viewportHeight);
        this.roomWidth = RRGame.WORLD_WIDTH;
        this.roomHeight = RRGame.WORLD_WIDTH; // this will change after about 5 milliseconds but needs to be here
    }

    public void changeWorldSize(float roomWidth, float roomHeight) {
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
    }

    public void moveToPlayer(float playerCenterX, float playerCenterY, float delta) {
        Vector3 pos = this.position;
        pos.x += (playerCenterX - pos.x) * LERP * delta;
        if (pos.x + this.viewportWidth/2f - MIN_DIST_FROM_CAM_EDGE < playerCenterX) {
            pos.x = playerCenterX - this.viewportWidth/2f + MIN_DIST_FROM_CAM_EDGE;
        }
        else if (pos.x - this.viewportWidth/2f + MIN_DIST_FROM_CAM_EDGE > playerCenterX) {
            pos.x = playerCenterX + this.viewportWidth/2f - MIN_DIST_FROM_CAM_EDGE ;
        }
        pos.y += (playerCenterY - pos.y) * LERP * delta;
        if (pos.y + this.viewportHeight/2f - MIN_DIST_FROM_CAM_EDGE < playerCenterY) {
            pos.y = playerCenterY - this.viewportHeight/2f + MIN_DIST_FROM_CAM_EDGE;
        }
        else if (pos.y - this.viewportHeight/2f + MIN_DIST_FROM_CAM_EDGE > playerCenterY) {
            pos.y = playerCenterY + this.viewportHeight/2f - MIN_DIST_FROM_CAM_EDGE;
        }
        this.position.set(pos);
        applyBounds();
    }

    private void applyBounds() {
        this.zoom = MathUtils.clamp(this.zoom, 0.1f, this.roomWidth/this.viewportWidth);

        float effectiveViewportWidth = this.viewportWidth * this.zoom;
        float effectiveViewportHeight = this.viewportHeight * this.zoom;

        this.position.x = MathUtils.clamp(this.position.x, effectiveViewportWidth/2f, this.roomWidth-effectiveViewportWidth/2f);
        this.position.y = MathUtils.clamp(this.position.y, effectiveViewportHeight/2f, this.roomHeight-effectiveViewportHeight/2f);
    }

    // useful info for any sort of input things to translate from pixel/world units
//    float x = Gdx.input.getX();
//    float y = Gdx.input.getY();
//    camera.unproject(new Vector3(x, y, 0));//this converts a Vector3 position of pixels to a Vector3 position of units
//    camera.project(new Vector3(10,10,0));//this converts a Vector3 position of units to a Vector3 position of pixels
}
