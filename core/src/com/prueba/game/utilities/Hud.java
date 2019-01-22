package com.prueba.game.utilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.prueba.game.ScreenBase;

public class Hud
{
    public Stage stage;

    private Viewport viewport;

    private float gauge;
    private Label gaugeLabel;

    private int points;
    private Label pointsLabel;

    private TextButton button;

    private Skin skin;
    private ScreenBase screen;

    public Hud(ScreenBase screen, SpriteBatch batch)
    {
        this.screen = screen;
        viewport = new FitViewport(1000, 700, new OrthographicCamera());

        skin = new Skin(Gdx.files.internal("skins/craftacular/craftacular-ui.json"));

        points = 0;

        stage = new Stage(viewport, batch);

        Table table = new Table();
//        table.setDebug(true);
        table.top();
        table.setFillParent(true);

        pointsLabel = new Label("Puntos: " + points, skin, "font", Color.YELLOW);
        table.add(pointsLabel).left().padLeft(10).padTop(10);

        gaugeLabel = new Label(String.format("%.1f", gauge),
                skin, "font", Color.YELLOW);
        table.add(gaugeLabel).expandX().padTop(10);

        button = new TextButton("Switch screen", skin);
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                screen.switchScreen();
            }
        });
        button.setTransform(true);
        button.setScale(0.7f);

        table.row();
        table.add(button).expandY().bottom().padBottom(10).padLeft(10);

        stage.addActor(table);
    }

    public void updateGauge(float newGauge)
    {
        gauge = newGauge;

        gaugeLabel.setText(String.format("%.1f", gauge));
    }

    public void updatePoints(int newPoints)
    {
        points = newPoints;

        pointsLabel.setText("Points: " + points);
    }

    public void render(float delta)
    {

    }
}
