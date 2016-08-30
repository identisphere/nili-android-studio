package com.nili.operator;

import android.os.Message;

import com.nili.globals.Commands;
import com.nili.globals.Globals;
import com.nili.utilities.ConnectionManager;
import com.nili.utilities.Strumming;

import java.sql.Connection;

/**
 * Created by USER on 10/07/2016.
 */
public class BtOperations
{
    private Strumming strumming;
    private ConnectionManager   connectionManager;


    public BtOperations(Strumming strumming, ConnectionManager connectionManager)
    {
        this.strumming = strumming;
        this.connectionManager = connectionManager;
    }

    public void blinkNeck(long delay, String postBlinkPositions)
    {
        this.sendStringToBt("111111111111111111111111");
        try { Thread.sleep(delay); } catch (Exception e) {e.printStackTrace();}
        this.sendStringToBt(postBlinkPositions);
    }

    synchronized void sendStringToBt(String data)
    {
        data = Globals.addBtDelimeters(data);

        Message message = new Message();
        message.arg1 = Commands.ConnectionManager.sendToBt;
        message.obj = Globals.addBtDelimeters(data);
        this.connectionManager.mHandler.sendMessage(message);
    }

    public void startStrumming(Chords.ChordObject chord)
    {

        Message message = new Message();
        message.arg1 = Commands.Strumming.startStrumming;
        message.arg2 = chord.topString;
        this.strumming.mHandler.sendMessage(message);
    }

    public void stopStrumming()
    {
        this.strumming.isActive = false;
		/*
		Message message = new Message();
		message.arg1 = Commands.Strumming.stopStrumming;
		this.strumming.mHandler.sendMessage(message);
		*/
    }

}
