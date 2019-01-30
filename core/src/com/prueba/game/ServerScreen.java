package com.prueba.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.prueba.game.utilities.Constants;
import com.prueba.game.utilities.Events;

import java.util.ArrayList;
import java.util.List;

public class ServerScreen extends ScreenBase
{
    private final PruebaGame game;
    private final SpriteBatch batch;

//    private ServerSocket serverSocket;

    private SocketIOServer serverSocket;
    private List<SocketIOClient> clients;

    public ServerScreen(PruebaGame game, SpriteBatch batch)
    {
        this.game = game;
        this.batch = batch;

        clients = new ArrayList<>();

        Configuration config = new Configuration();
        config.setHostname(Constants.HOST);
        config.setPort(Constants.PORT);

        serverSocket = new SocketIOServer(config);
        serverSocket.addConnectListener(client -> {
            Gdx.app.log("server", "client connected with Id: " + client.getSessionId());
        });
        serverSocket.addDisconnectListener(client -> Gdx.app.log("socket", "client disconnected"));

        serverSocket.addEventListener(Events.MYID.name(), String.class, (client, data, ackSender) -> {
            Gdx.app.log("server", Events.MYID.name() +" event with Id: " + data);
        });

        serverSocket.addEventListener(Events.SHOT.name(), String.class, (client, data, ackSender) -> {
            Gdx.app.log("server", Events.SHOT.name() + " event with data: " + data);
            serverSocket.getBroadcastOperations()
                    .sendEvent(Events.SHOT.name(), client.getSessionId().toString(), data);
        });

        Gdx.app.log("server", "Starting server in port " + Constants.PORT);
        serverSocket.start();

        try
        {
            Thread.sleep(Integer.MAX_VALUE);
            serverSocket.stop();

        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
