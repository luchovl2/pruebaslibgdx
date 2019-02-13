package com.prueba.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.prueba.game.utilities.*;

import java.util.Optional;
import java.util.function.Consumer;

import static com.prueba.game.utilities.Constants.PPM;

public class Screen2 extends ScreenBase
{
    private final SpriteBatch batch;
    private final PruebaGame pruebaGame;

    private Viewport viewport;

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Body floor;
    private Body ceil;
    private Body ball;
    private Body block;
    private Body player;
    private Body legs;
    private Body bullet;
    private Body platform;

    private RevoluteJoint hip;
    private RopeJoint rope;

    private Sprite ballSprite;
    private Sprite blockSprite;

    private Hud hud;

    private long timeClickStart;

    private Vector2 strapAt;
    private boolean gottaStrap = false;
    private boolean isStrapped = false;

    public Screen2(PruebaGame pruebaGame, SpriteBatch batch)
    {
        this.batch = batch;
        this.pruebaGame = pruebaGame;
    }

    @Override
    public void show()
    {
        timeClickStart = 0L;

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        viewport = new FitViewport(width/PPM, height/PPM, new OrthographicCamera());
        OrthographicCamera cam = (OrthographicCamera)viewport.getCamera();
        cam.zoom += 1.4f;

        hud = new Hud(this, batch);

        ballSprite = new Sprite(new Texture("basketball.png"));
        ballSprite.setOriginCenter();

        blockSprite = new Sprite(new Texture("brick_wall2.png"));
        blockSprite.setOriginCenter();

        world = new World(new Vector2(0, -9.8f), false);
        debugRenderer = new Box2DDebugRenderer();

        world.setContactListener(new SpiderContactListener(this));

        InputMultiplexer mux = new InputMultiplexer(
                                    new SpiderInputProcessor(this),
                                    hud.stage);
        Gdx.input.setInputProcessor(mux);

        FixtureDef fixDef = new FixtureDef();
        BodyDef bodyDef = new BodyDef();

        //creando piso con edge
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(0.5f, Constants.PISO_ALTURA));
        floor = world.createBody(bodyDef);

        ChainShape chain = new ChainShape();
        float[] points = {-2, 5,
                0, 0,
                10, 0,
                12, -0.2f,
                15, 0.1f,
                20, 2,
                35, 2
        };
        chain.createChain(points);
        fixDef.shape = chain;
        fixDef.friction = 0.5f;
        floor.createFixture(fixDef);
        chain.dispose();

        //creando techo
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(0, Constants.TECHO_ALTURA));
        ceil = world.createBody(bodyDef);
        chain = new ChainShape();
        float[] points2 = {
                0, 0,
                10, 0,
                12, -0.2f,
                15, 0.1f,
                20, 1.6f,
                22, 10,
                26, 14,
                35, 12
        };
        chain.createChain(points2);
        fixDef.shape = chain;
        ceil.createFixture(fixDef);
        ceil.setUserData("ceil");
        chain.dispose();

        //creando bloque estático (línea de triple)
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2(Constants.ARO_X-7f, 0));
        block = world.createBody(bodyDef);
        PolygonShape shape2 = new PolygonShape();
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

        createPlayer();

        //creando plataforma
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(23f, 4f));
        bodyDef.fixedRotation = true;
        Body pivot = world.createBody(bodyDef);
        shape = new CircleShape();
        shape.setRadius(0.05f);
        fixDef = new FixtureDef();
        fixDef.shape = shape;
        pivot.createFixture(fixDef);
        shape.dispose();

        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(new Vector2(23f, 4f));
//        bodyDef.fixedRotation = true;
        platform = world.createBody(bodyDef);
        shape2 = new PolygonShape();
        shape2.setAsBox(0.7f, 0.1f);
        fixDef = new FixtureDef();
        fixDef.shape = shape2;
        fixDef.density = 1f;
        platform.createFixture(fixDef);
        shape2.dispose();

        RopeJointDef ropeDef = new RopeJointDef();
        ropeDef.bodyA = ceil;
        ropeDef.bodyB = pivot;
        ropeDef.localAnchorA.set(new Vector2(25f, 14f));
        ropeDef.localAnchorB.set(new Vector2(0, 0));
        ropeDef.maxLength = platform.getPosition().dst(ropeDef.localAnchorA);
        world.createJoint(ropeDef);

        RevoluteJointDef revDef = new RevoluteJointDef();
        revDef.bodyA = pivot;
        revDef.bodyB = platform;
        revDef.localAnchorA.set(new Vector2(0, 0));
        revDef.localAnchorB.set(new Vector2(0, 0));
        revDef.maxMotorTorque = 1f;
        revDef.motorSpeed = 0f;
        revDef.enableMotor = true;
        world.createJoint(revDef);
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

    private void update(float delta)
    {
        world.step(1 / 60f, 6, 2);
        handleInput();

        if(gottaStrap && strapAt != null)
        {
            gottaStrap = false;
            strapPlayer(strapAt);
        }

        float camY = viewport.getCamera().position.y;
        float camZ = viewport.getCamera().position.z;
//        viewport.getCamera().position.set(player.getPosition().x, camY, camZ);
        viewport.getCamera().position.set(
                                    player.getPosition().x,
                                    player.getPosition().y,
                                    camZ);
    }

    public void strapPlayerLater(Vector2 point)
    {
        gottaStrap = true;
        strapAt = point.cpy();
    }

    private void strapPlayer(Vector2 anchor)
    {
        destroyRope();

        RopeJointDef jdef = new RopeJointDef();
        jdef.bodyA = player;
        jdef.bodyB = ceil;
        jdef.localAnchorA.set(Constants.ROPE_ANCHOR);
        jdef.localAnchorB.set(ceil.getLocalPoint(anchor));
        jdef.collideConnected = true;
        jdef.maxLength = player.getPosition().dst(anchor);
        rope = (RopeJoint) world.createJoint(jdef);

        isStrapped = true;
    }

    private void destroyRope()
    {
        Optional<RopeJoint> joint = Optional.ofNullable(this.rope);
        joint.ifPresent(rope -> {
            world.destroyJoint(rope);
            this.rope = null;
            isStrapped = false;
        });
    }

    private  void createPlayer()
    {
        if(player == null)
        {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(Constants.PLAYER_SPAWN);
            bodyDef.fixedRotation = true;
            player = world.createBody(bodyDef);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(Constants.PLAYER_WIDTH/2, Constants.PLAYER_HEIGHT/2);
            FixtureDef fixDef = new FixtureDef();
            fixDef.shape = shape;
            fixDef.density = 1f;
            player.createFixture(fixDef);
            shape.dispose();

            bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(Constants.LEGS_SPAWN);
            legs = world.createBody(bodyDef);
            shape = new PolygonShape();
            shape.setAsBox(Constants.LEGS_WIDTH/2, Constants.LEGS_HEIGHT/2);
            fixDef = new FixtureDef();
            fixDef.shape = shape;
            fixDef.density = 1f;
            legs.createFixture(fixDef);
            shape.dispose();

            RevoluteJointDef jointDef = new RevoluteJointDef();
            jointDef.bodyA = player;
            jointDef.bodyB = legs;
            jointDef.enableMotor = true;
            jointDef.maxMotorTorque = 1000f;
            jointDef.enableLimit = true;
            jointDef.upperAngle = MathUtils.PI / 2;
            jointDef.lowerAngle = -MathUtils.PI / 2;
            jointDef.localAnchorA.set(0, -Constants.PLAYER_HEIGHT/2);
            jointDef.localAnchorB.set(0, Constants.LEGS_HEIGHT/2);

            hip = (RevoluteJoint)world.createJoint(jointDef);
            hip.enableMotor(true);
            hip.setMotorSpeed(0f);
        }
    }

    private void createBullet(Vector2 direction)
    {
        Vector2 pos = player.getPosition().cpy().add(Constants.BULLET_OFFSET);

        if(direction.x < player.getPosition().x)
            pos.sub(Constants.BULLET_OFFSET.x * 2f, 0);

        if(bullet != null)
            world.destroyBody(bullet);

        BodyDef bdef  = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(pos);
        bdef.fixedRotation = true;
        bullet = world.createBody(bdef);
        FixtureDef fdef = new FixtureDef();
        CircleShape bulletShape = new CircleShape();
        bulletShape.setRadius(Constants.BULLET_RADIUS);
        fdef.shape = bulletShape;
        bullet.createFixture(fdef);
        bullet.setUserData("bullet");
        bulletShape.dispose();

        bullet.applyLinearImpulse(direction.cpy().sub(player.getPosition()).scl(3f),
                bullet.getPosition(), true);
    }

    @Override
    public void resize(int width, int height)
    {
        viewport.update(width, height, true);
    }

    @Override
    public void switchScreen()
    {
        pruebaGame.switchToScreen3();
    }

    @Override
    public void onClick(int screenX, int screenY)
    {
        createBullet(viewport.unproject(new Vector2(screenX, screenY)));
    }

    private void handleInput()
    {
        OrthographicCamera cam = (OrthographicCamera) viewport.getCamera();

        float motorSpeed = 0f;

        if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
        {
            if(isStrapped)
            {
                motorSpeed = -15f;
            }
            else
            {
                player.applyLinearImpulse(new Vector2(-0.1f, 0), player.getPosition(), true);
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
        {
            if(isStrapped)
            {
                motorSpeed = 15f;
            }
            else
            {
                player.applyLinearImpulse(new Vector2(0.1f, 0), player.getPosition(), true);
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP))
        {
            if(isStrapped)
                rope.setMaxLength(rope.getMaxLength() - 0.1f);
            else
                player.applyLinearImpulse(new Vector2(0, 0.2f), player.getPosition(), true);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN))
        {
            if(isStrapped)
                rope.setMaxLength(rope.getMaxLength() + 0.1f);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE))
        {
            destroyRope();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.PLUS))
        {
            cam.zoom -= 0.2f;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.MINUS))
        {
            cam.zoom += 0.2f;
        }

        hip.setMotorSpeed(motorSpeed);
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
