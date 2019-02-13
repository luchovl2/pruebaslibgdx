package com.prueba.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.prueba.game.utilities.Hud;

import static com.prueba.game.utilities.Constants.PPM;

public class Screen3 extends ScreenBase
{
    private final SpriteBatch batch;
    private final PruebaGame pruebaGame;

    private Viewport viewport;

    private Hud hud;

    private World world;

    private Box2DDebugRenderer debugRenderer;
    private Body blackHole;

    private Body planet;

    public Screen3(PruebaGame pruebaGame, SpriteBatch batch)
    {
        this.batch = batch;
        this.pruebaGame = pruebaGame;
    }

    @Override
    public void show()
    {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        viewport = new FitViewport(width/PPM, height/PPM, new OrthographicCamera());
        OrthographicCamera cam = (OrthographicCamera)viewport.getCamera();
        cam.zoom += 1.8f;

        hud = new Hud(this, batch);

        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();

        Gdx.input.setInputProcessor(hud.stage);

        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(1f, 1f);
        blackHole = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape circle = new CircleShape();
        circle.setRadius(2f);
        fdef.shape = circle;
        blackHole.createFixture(fdef);

        bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(new Vector2(15f, 3f));
        bdef.linearVelocity.set(new Vector2(0.2f, -3.7f));
        planet = world.createBody(bdef);

        fdef = new FixtureDef();
        circle.setRadius(0.4f);
        fdef.shape = circle;
        planet.createFixture(fdef);
        circle.dispose();
    }

    @Override
    public void render(float delta)
    {
        update(delta);

        planet.applyForceToCenter(blackHole.getPosition().cpy()
                        .sub(planet.getPosition()).scl(0.4f),
                true);

        world.step(1/60f, 6, 2);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.act(delta);
        hud.stage.draw();
//        batch.setProjectionMatrix(viewport.getCamera().combined);

        debugRenderer.render(world, viewport.getCamera().combined);
    }

    private void update(float delta)
    {

        OrthographicCamera cam = (OrthographicCamera) viewport.getCamera();

        if(Gdx.input.isKeyJustPressed(Input.Keys.PLUS))
        {
            cam.zoom -= 0.2f;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.MINUS))
        {
            cam.zoom += 0.2f;
        }
    }

    @Override
    public void switchScreen()
    {
        pruebaGame.switchToScreen();
    }

    @Override
    public void resize(int width, int height)
    {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose()
    {
        world.dispose();
        debugRenderer.dispose();
        hud.stage.dispose();

        super.dispose();
    }
}
