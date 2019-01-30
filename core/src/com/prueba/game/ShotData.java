package com.prueba.game;

import com.badlogic.gdx.math.Vector2;

import java.io.Serializable;

public class ShotData implements Serializable
{
    private Vector2 force;
    private Vector2 position;
    private Vector2 velocity;

    public ShotData(){}

    public ShotData(Vector2 force, Vector2 position, Vector2 velocity)
    {
        this.force = force;
        this.position = position;
        this.velocity = velocity;
    }

    public Vector2 getForce()
    {
        return force;
    }

    public void setForce(Vector2 force)
    {
        this.force = force;
    }

    public Vector2 getPosition()
    {
        return position;
    }

    public void setPosition(Vector2 position)
    {
        this.position = position;
    }

    public Vector2 getVelocity()
    {
        return velocity;
    }

    public void setVelocity(Vector2 velocity)
    {
        this.velocity = velocity;
    }

    @Override
    public String toString()
    {
        return "{" +
                "force:'" + force + "'" +
                ", position:'" + position + "'" +
                ", velocity:'" + velocity + "'" +
                "}";
    }
}
