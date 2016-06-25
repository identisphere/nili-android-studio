package com.nili.utilities;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.nili.globals.Commands;
import com.nili.globals.Globals;

/**
 * Created by USER on 25/06/2016.
 */
public class Strumming extends Thread
{
    public boolean isActive = false;
    ConnectionManager connectionManager;
    int topString;
    long delay = 80;
    long delayCycle = 150;
    public 	Handler mHandler;

    public Strumming()
    {}

    public void run()
    {
        Thread.currentThread().setName("Strumming");

        Looper.prepare();

        mHandler = new Handler() {
            public void handleMessage(Message message) {
                try {
                    if (message.arg1 == Commands.Strumming.startStrumming)
                        startStrumming((Integer)message.arg2);
                    else if (message.arg1 == Commands.Operator.addChord)
                        stopStrumming();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        Looper.loop();
    }

    public void startStrumming(int topString)
    {
        isActive = true;
        this.topString = topString-1;
        lightStrumString(this.topString);
    }

    private void stopStrumming()
    {
        isActive = false;
    }

    public void set(ConnectionManager connectionManager)
    {
        this.connectionManager = connectionManager;
    }

    private void lightStrumString(int stringIndex)
    {
        try
        {
            if(!isActive) return;

            if(stringIndex==-1)
            {
                Thread.sleep(delayCycle);
                lightStrumString(topString);
                return;
            }

            StringBuilder positionString = new StringBuilder("000000000000000000000000");
            positionString.setCharAt(positionString.length() - 1 - stringIndex, '1');

            if(isActive)
            {
                Message message = new Message();
                message.arg1 = Commands.ConnectionManager.sendToBt;
                message.obj = Globals.addBtDelimeters(positionString.toString());
                this.connectionManager.mHandler.sendMessage(message);
            }
            else
                return;

            Thread.sleep(delay);
            lightStrumString(stringIndex - 1);
        }
        catch(Exception ex)
        {

        }

    }
}
