package com.nili.utilities;

import android.os.Message;

import com.nili.globals.Commands;
import com.nili.globals.Globals;
import com.nili.main.MainActivity;
import com.nili.operator.Operator;

/**
 * Created by USER on 10/07/2016.
 */
public class Timer extends Thread{
    Operator    operator;
    MainActivity mainActivity;

    int counter;
    int timeRatio = 1;
    boolean isActive = false;
    boolean isPaused = true;

    void Timer(){}

    public void set(Operator operator, MainActivity mainActivity)
    {
        this.operator = operator;
        this.mainActivity = mainActivity;
    }

    public void run()
    {
        try {
            while(true)
            {
                if(!isPaused && isActive)
                {
                    if(counter==0)
                    {
                       signalNext();
                       currentThread().sleep(50);
                       continue;
                    }
                    else
                    {
                       counter--;
                       this.mainActivity.runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Timer.this.mainActivity.setCounter(counter + 1);
                           }
                       });
                    }
                    currentThread().sleep(Globals.MIN_TIK * timeRatio);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void signalNext()
    {
        Message message = new Message();
        message.arg1 = Commands.Operator.chordChangeTik;
        this.operator.mHandler.sendMessage(message);
    }

    public void setCounter(int counter)
    {
        this.counter = counter;
    }

    public void setActive(boolean isActive)
    {
        this.isActive = isActive;
    }

    public boolean getIsPaused() {
        return isPaused;
    }

    public void setIsPaused(boolean isPaused)
    {
        this.isPaused = isPaused;
    }

    public void changeRatio(int howMuch)
    {
        if(timeRatio + howMuch <= 1) return;
        timeRatio = timeRatio + howMuch;
    }

    public int getRatio()
    {
        return timeRatio;
    }
}
