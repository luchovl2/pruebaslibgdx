package com.prueba.game.utilities;

import com.badlogic.gdx.math.Vector2;

public final class Constants
{
    private Constants()
    {
    }

    public static final int PORT = 50505;
    public static final String HOST = "localhost";

    public static final float PPM = 100; //pixel por metro

    //public static final float ARO_RADIO = 0.229f; //en metros
    public static final float ARO_RADIO = 0.229f; //en metros
    public static final float ARO_Y = 3.05f; // en metros
    public static final float ARO_X = 9f; //en metros
    public static final float ARO_SEMIGROSOR = 0.02f; // en metros

    public static final float TABLERO_ALTO = 1f; //en metros
    public static final Vector2 TABLERO_POS = //relativa al body (el aro)
            new Vector2(ARO_RADIO*2, TABLERO_ALTO/2-ARO_RADIO/2);

    public static final float PISO_ALTURA = 0.5f; //en metros
    public static final float TECHO_ALTURA = 8f;  //en metros

    public static final float PELOTA_RADIO = 0.12f; //en metros
    public static final float PELOTA_RESTITUCION = 0.65f;
    public static final float PELOTA_MASA = 0.62f; //en kg
    public static final Vector2 PELOTA_SPAWN = new Vector2(2f, 5f);

    public static final Vector2 PLAYER_SPAWN = new Vector2(1.6f, 1f);
    public static final float PLAYER_WIDTH = 0.4f;
    public static final float PLAYER_HEIGHT = 0.6f;
    public static final Vector2 ROPE_ANCHOR = new Vector2(0, PLAYER_HEIGHT / 2);

    public static final float LEGS_WIDTH = 0.3f;
    public static final float LEGS_HEIGHT = 0.4f;
    public static final Vector2 LEGS_SPAWN = PLAYER_SPAWN.cpy().add(0, -LEGS_HEIGHT/2);

    public static final float BULLET_RADIUS = 0.1f;
    public static final Vector2 BULLET_OFFSET = new Vector2(PLAYER_WIDTH, PLAYER_HEIGHT/2);
}
