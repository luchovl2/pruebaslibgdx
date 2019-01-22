package com.prueba.game.utilities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.prueba.game.Screen2;

import java.util.Optional;

public class SpiderContactListener implements ContactListener
{
    private Screen2 screen;

    public SpiderContactListener(Screen2 screen)
    {
       this.screen = screen;
    }

    @Override
    public void beginContact(Contact contact)
    {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        Optional<String> nameA = Optional.ofNullable((String)fixA.getBody().getUserData());
        Optional<String> nameB = Optional.ofNullable((String)fixB.getBody().getUserData());

        if(nameA.isPresent() && nameB.isPresent())
        {
            if((nameA.get().equals("bullet") && nameB.get().equals("ceil")) ||
                    (nameB.get().equals("bullet") && nameA.get().equals("ceil")))
            {
                //cuando hay contacto:
                //destruir bala y crear rope joint entre el player y el punto de contacto
                Vector2 point = contact.getWorldManifold().getPoints()[0];
                screen.strapPlayerLater(point);
            }
        }
    }

    @Override
    public void endContact(Contact contact)
    {

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
