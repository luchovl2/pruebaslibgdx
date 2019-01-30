package com.prueba.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.prueba.game.utilities.Constants;
import com.prueba.game.utilities.Events;
import com.prueba.game.utilities.Hud;
import com.prueba.game.utilities.PruebaContactListener;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

import static com.prueba.game.utilities.Constants.PPM;

public class Screen extends ScreenBase implements InputProcessor
{
    private final SpriteBatch batch;
    private final PruebaGame pruebaGame;

    private Viewport viewport;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Body body;
    private Body ball;
    private Body block;
    private Body aro;

    private Sprite ballSprite;
    private Sprite blockSprite;

    private Hud hud;

    private long timeClickStart;
    private Vector2 forceDir;
    private boolean charging = false;
    private int goals;

    private Socket socket;

    public Screen(PruebaGame pruebaGame, SpriteBatch batch)
    {
        this.batch = batch;
        this.pruebaGame = pruebaGame;
    }

    @Override
    public void show()
    {
        try
        {
            socket = IO.socket("http://localhost:" + Constants.PORT);
            socket.on(Socket.EVENT_CONNECT, args -> {
                Gdx.app.log("socket", "connected with Id: " + socket.id());
                socket.emit(Events.MYID.name(), socket.id());
            });

            socket.on(Events.SHOT.name(), args -> {
                //viene el id (string)
                // y luego shot data (json/string)
                String id = (String) args[0];
//                Gdx.app.log("socket", "tiro event de Id " + id + ", shot data: " + args[1]);

                if(!id.equals(socket.id())) //si son datos de otro usuario
                {
                    ShotData data = new Json().fromJson(ShotData.class, (String)args[1]);

                    ball.setTransform(data.getPosition(), ball.getAngle());
                    ball.setLinearVelocity(data.getVelocity());
                    makeShot(data.getForce(), false);
                }
            });

            Gdx.app.log("socket", "connecting to server");
            socket.connect();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        timeClickStart = 0L;
        goals = 0;

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        viewport = new FitViewport(width/PPM, height/PPM, new OrthographicCamera());
        OrthographicCamera cam = (OrthographicCamera)viewport.getCamera();
        cam.zoom += 0.4f;

        hud = new Hud(this, batch);

        ballSprite = new Sprite(new Texture("basketball.png"));
        ballSprite.setOriginCenter();

        blockSprite = new Sprite(new Texture("brick_wall2.png"));
        blockSprite.setOriginCenter();

        world = new World(new Vector2(0, -9.8f), false);
        debugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new PruebaContactListener(this));

        InputMultiplexer mux = new InputMultiplexer(this, hud.stage);
        Gdx.input.setInputProcessor(mux);

        FixtureDef fixDef = new FixtureDef();
        BodyDef bodyDef = new BodyDef();

        //creando piso con edge
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(0.5f, Constants.PISO_ALTURA));
        body = world.createBody(bodyDef);

        ChainShape chain = new ChainShape();
        float[] points = {-2, 5,
                0, 0,
                10, 0,
                12, -0.2f,
                15, 0.1f,
                20, 6
                };
        chain.createChain(points);
        fixDef.shape = chain;
        fixDef.friction = 0.5f;
        body.createFixture(fixDef);
        chain.dispose();

        //creando bloque estático (línea de tiro libre)
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(Constants.ARO_X-4.5f, 0));
        block = world.createBody(bodyDef);
        PolygonShape shape2 = new PolygonShape();
        shape2.setAsBox(0.1f, 0.1f); //son width/2 y height/2
        fixDef.shape = shape2;
        block.createFixture(fixDef);
        shape2.dispose();

        //creando bloque estático (línea de triple)
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(Constants.ARO_X-7f, 0));
        block = world.createBody(bodyDef);
        shape2 = new PolygonShape();
        shape2.setAsBox(0.1f, 0.1f); //son width/2 y height/2
        fixDef.shape = shape2;
        block.createFixture(fixDef);
        shape2.dispose();

        //creando bola
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(Constants.PELOTA_SPAWN);
        bodyDef.fixedRotation = false;
        ball = world.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        shape.setRadius(Constants.PELOTA_RADIO);
        fixDef.shape = shape;
        fixDef.restitution = Constants.PELOTA_RESTITUCION;
        fixDef.density = 1f;
        ball.createFixture(fixDef);
        MassData massData = ball.getMassData();
        massData.mass = Constants.PELOTA_MASA;
        ball.setMassData(massData);
        ball.setUserData("ball");
        shape.dispose();

        //creando bloque
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(3, 2));
        bodyDef.fixedRotation = false;
        block = world.createBody(bodyDef);
        shape2 = new PolygonShape();
        shape2.setAsBox(0.7f, 0.9f); //son width/2 y height/2
        fixDef.shape = shape2;
        fixDef.density = 1f;
        fixDef.restitution = 0.1f;
        block.createFixture(fixDef);
        shape2.dispose();

        //creando aro
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(Constants.ARO_X,
                                    Constants.ARO_Y+Constants.PISO_ALTURA));
        aro = world.createBody(bodyDef);
        shape2 = new PolygonShape();
        //corte derecho del aro
        shape2.setAsBox(Constants.ARO_SEMIGROSOR,
                Constants.ARO_SEMIGROSOR,
                new Vector2(Constants.ARO_RADIO+Constants.ARO_SEMIGROSOR, 0), 0f);
        fixDef.shape = shape2;
        fixDef.restitution = 0.0f;
        aro.createFixture(fixDef);
        //corte izquierdo del aro
        shape2.setAsBox(Constants.ARO_SEMIGROSOR,
                Constants.ARO_SEMIGROSOR,
                new Vector2(-Constants.ARO_RADIO-Constants.ARO_SEMIGROSOR, 0), 0f);
        fixDef.shape = shape2;
        aro.createFixture(fixDef);
        //tablero
        shape2.setAsBox(Constants.ARO_SEMIGROSOR*2, Constants.TABLERO_ALTO/2,
                Constants.TABLERO_POS, 0f);
        fixDef.shape = shape2;
        aro.createFixture(fixDef);
        //sensor superior
        shape2.setAsBox(0.15f, 0.07f, new Vector2(0, 0.1f), 0f);
        fixDef.shape = shape2;
        fixDef.isSensor = true;
        aro.createFixture(fixDef).setUserData("sensor_up");
        //sensor inferior
        shape2.setAsBox(0.15f, 0.07f, new Vector2(0, -0.1f), 0f);
        fixDef.shape = shape2;
        fixDef.isSensor = true;
        aro.createFixture(fixDef).setUserData("sensor_down");
        shape2.dispose();
    }

    public void scoreGoal()
    {
        this.goals++;
        hud.updatePoints(goals);
    }

    @Override
    public void render(float delta)
    {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.act(delta);
        hud.stage.draw();

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        debugRenderer.render(world, viewport.getCamera().combined);

        batch.begin();
        ballSprite.setOriginCenter();
        ballSprite.setSize(Constants.PELOTA_RADIO*2, Constants.PELOTA_RADIO*2);
        ballSprite.setCenter(ball.getPosition().x, ball.getPosition().y);
        ballSprite.setRotation(ball.getAngle()*180/ MathUtils.PI);
        ballSprite.draw(batch);

        blockSprite.setOriginCenter();
        blockSprite.setCenter(block.getPosition().x, block.getPosition().y);
        blockSprite.setSize(1.4f, 1.8f);
        blockSprite.setRotation(block.getAngle()*180/MathUtils.PI);
        blockSprite.draw(batch);

        batch.end();
    }

    public void update(float delta)
    {
        world.step(1 / 60f, 6, 2);

        float camY = viewport.getCamera().position.y;
        float camZ = viewport.getCamera().position.z;

        if(charging)    //si está "cargando" fuerza (click presionado)
        {
            hud.updateGauge(TimeUtils.timeSinceMillis(timeClickStart));
        }

        viewport.getCamera().position.set(ball.getPosition().x, camY, camZ);

        handleInput();
    }

    @Override
    public void switchScreen()
    {
        pruebaGame.switchToScreen2();
    }

    private void handleInput()
    {
        OrthographicCamera cam;
        cam = (OrthographicCamera)viewport.getCamera();

        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
        {
            ball.applyAngularImpulse(-0.2f, true);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
        {
            ball.applyAngularImpulse(0.2f, true);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.UP))
        {
            ball.applyLinearImpulse(new Vector2(0, 2), ball.getPosition(), true);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.PLUS))
        {
            cam.zoom -= 0.2f;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.MINUS))
        {
            cam.zoom += 0.2f;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.P))
        {
            ball.setGravityScale(0.1f);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.L))
        {
            ball.setGravityScale(1f);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.R))
        {
            ball.setTransform(Constants.PELOTA_SPAWN, 0);
            ball.setLinearVelocity(0, 0);
        }
    }

    private void sendMessage(String message)
    {
//        try(OutputStream out = socket.getOutputStream())
//        {
//            out.write(message.getBytes());
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
    }

    @Override
    public void dispose ()
    {
        Gdx.app.log("screen", "disposing screen");
        world.dispose();
        debugRenderer.dispose();
        hud.stage.dispose();
        socket.disconnect();
    }

    @Override
    public void resize(int width, int height)
    {
        viewport.update(width, height, true);
    }

    @Override
    public boolean keyDown(int keycode)
    {
        return false;
    }

    @Override
    public boolean keyUp(int keycode)
    {
        return false;
    }

    @Override
    public boolean keyTyped(char character)
    {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button)
    {
        if(button == Input.Buttons.LEFT)
        {
            if(forceDir == null)
                forceDir = new Vector2();

            charging = true; //indica que está cargando fuerza para mostrar en HUD

            //posición del click convertido a coordenadas del world ("y" hacia arriba)
            Vector2 clickPos = viewport.unproject(new Vector2(screenX, screenY));

            timeClickStart = System.currentTimeMillis();

            //fuerza en la dirección que une la posición actual de la pelota
            // y la posición del click
            forceDir = clickPos.cpy().sub(ball.getPosition()).setLength(1f);

            return false;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button)
    {
        if(button == Input.Buttons.LEFT)
        {
            long intervalo = TimeUtils.timeSinceMillis(timeClickStart);

            //fuerza proporcional al tiempo que esté presionado el botón
            //se aplica cuando se suelta
            makeShot(getForce(intervalo), true);
            charging = false;
            return false;
        }
        return false;
    }

    private Vector2 getForce(long intervalo)
    {
        return forceDir.scl(intervalo*0.3f);
    }

    private void makeShot(Vector2 force, boolean emit)
    {
        ball.applyForceToCenter(force, true);

        if(emit)
        {
            ShotData data = new ShotData(force, ball.getPosition(), ball.getLinearVelocity());
            Json json = new Json();
//            Gdx.app.log("socket", "enviando: " + json.toJson(data));
            socket.emit(Events.SHOT.name(), json.toJson(data));
        }
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer)
    {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY)
    {
        return false;
    }

    @Override
    public boolean scrolled(int amount)
    {
        return false;
    }
}
