package com.prueba.game.utilities;

import com.badlogic.gdx.physics.box2d.*;
import com.prueba.game.Screen;

import java.util.Optional;

public class PruebaContactListener implements ContactListener
{
    public enum BallState
    {
            AFUERA,
            ENTRANDO,
            ADENTRO
    };

    private BallState state = BallState.AFUERA;
    private Screen screen;

    private boolean inUp;
    private boolean inDown;

    public PruebaContactListener(Screen screen)
    {
        this.screen = screen;

        inUp = false;
        inDown = false;
    }

    @Override
    public void beginContact(Contact contact)
    {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        if(fixA.getBody().getUserData() != null &&
                fixA.getBody().getUserData().equals("ball"))
        {
            if(fixB.getUserData() != null && fixB.getUserData().equals("sensor_up"))
            {
                inUp = true;
            }
            else if(fixB.getUserData() != null && fixB.getUserData().equals("sensor_down"))
            {
                inDown = true;
            }
            detectGoal();
        }
        else if(fixB.getBody().getUserData() != null &&
                fixB.getBody().getUserData().equals("ball"))
        {
            if(fixA.getUserData() != null && fixA.getUserData().equals("sensor_up"))
            {
                inUp = true;
            }
            else if(fixA.getUserData() != null && fixA.getUserData().equals("sensor_down"))
            {
                inDown = true;
            }
            detectGoal();
        }
    }

    @Override
    public void endContact(Contact contact)
    {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        if(fixA.getBody().getUserData() != null &&
                fixA.getBody().getUserData().equals("ball"))
        {
            if(fixB.getUserData() != null && fixB.getUserData().equals("sensor_up"))
            {
                inUp = false;
            }
            else if(fixB.getUserData() != null && fixB.getUserData().equals("sensor_down"))
            {
                inDown = false;
            }
            detectGoal();
        }
        else if(fixB.getBody().getUserData() != null &&
                fixB.getBody().getUserData().equals("ball"))
        {
            if(fixA.getUserData() != null && fixA.getUserData().equals("sensor_up"))
            {
                inUp = false;
            }
            else if(fixA.getUserData() != null && fixA.getUserData().equals("sensor_down"))
            {
                inDown = false;
            }
            detectGoal();
        }
    }

    private void detectGoal()
    {
        switch(state)
        {
            case AFUERA:
                if(inUp && !inDown)
                {
                    state = BallState.ENTRANDO;
                }
                break;
            case ENTRANDO:
                if(!inUp && !inDown)
                {
                    state = BallState.AFUERA;
                }
                else if(inUp && inDown)
                {
                    state = BallState.ADENTRO;
                }

                break;
            case ADENTRO:
                if(!inUp && inDown)
                {
                    //GOL
                    screen.scoreGoal();
                }
                else if(inUp && !inDown)
                {
                    state = BallState.ENTRANDO;
                }
                else if(!inUp && !inDown)
                {
                    state = BallState.AFUERA;
                }
                break;
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold)
    {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse)
    {

    }
}
