package com.prueba.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PruebaGame extends Game
{
	private SpriteBatch batch;
	private ScreenBase currScreen;

	@Override
	public void create()
	{
		batch = new SpriteBatch();
		currScreen = new Screen(this, batch);
		this.setScreen(currScreen);
	}

	public void switchToScreen()
	{
		currScreen = new Screen(this, batch);
		this.setScreen(currScreen);
	}

	public void switchToScreen2()
	{
		currScreen = new Screen2(this, batch);
		this.setScreen(currScreen);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		batch.dispose();
	}
}
