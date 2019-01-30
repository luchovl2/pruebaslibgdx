package com.prueba.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Optional;
import java.util.function.Consumer;

public class PruebaGame extends Game
{
	private SpriteBatch batch;
	private ScreenBase currScreen;

	private boolean isServer = false;

	@Override
	public void create()
	{
		Optional.ofNullable(System.getProperty("server"))
				.ifPresent(value -> isServer = value.equals("true"));

		batch = new SpriteBatch();

		if(isServer)
		{
			Gdx.app.log("game", "Instancia servidor");
			currScreen = new ServerScreen(this, batch);
		}
		else
		{
			Gdx.app.log("game", "Instancia cliente");
			currScreen = new Screen(this, batch);
		}

		this.setScreen(currScreen);
	}

	public void switchToScreen()
	{
		currScreen.dispose();
		currScreen = new Screen(this, batch);
		this.setScreen(currScreen);
	}

	public void switchToScreen2()
	{
		currScreen.dispose();
		currScreen = new Screen2(this, batch);
		this.setScreen(currScreen);
	}

	@Override
	public void dispose()
	{
		Gdx.app.log("game", "about to dispose this sh*t");
		currScreen.dispose();
		batch.dispose();
		super.dispose();
	}
}
