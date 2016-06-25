package com.nili.utilities;

import android.graphics.Color;
import android.os.Message;

import com.nili.globals.Commands;
import com.nili.globals.Globals;
import com.nili.main.Operator;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;



/**
 * Created by USER on 24/06/2016.
 */
public class Listener {
    AudioDispatcher dispatcher;
    int currentString;
    boolean isActive = false;
    Operator operator;

    public void setCurrentString(int string)
    {
        currentString = string;
        if(string == 1) currentString = Strings.E1;
        else if(string==2) currentString = Strings.B;
        else if(string==3) currentString = Strings.G;
        else if(string==4) currentString = Strings.D;
        else if(string==5) currentString = Strings.A;
        else if(string==6) currentString = Strings.E2;
        else return;
        isActive = true;
    }

    static public class Strings
    {
        static public int E2 = 0;
        static public int A = 1;
        static public int D = 2;
        static public int G = 3;
        static public int B = 4;
        static public int E1 = 5;
    }

    public void sendStrummedCorrect()
    {
        Message message = new Message();
        message.arg1 = Commands.Operator.strummedCorrect;
        this.operator.mHandler.sendMessage(message);
        isActive = false;
    }

    public Listener()
    {
    }

    public void set(Operator operator)
    {
        try
        {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
            this.operator = operator;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                if (!isActive) return;

                final float pitchInHz = pitchDetectionResult.getPitch();
                if (pitchInHz > 320.0 && pitchInHz < 340.0 && currentString == Strings.E1)
                    sendStrummedCorrect();
                else if (pitchInHz > 244.0 && pitchInHz < 248.0 && currentString == Strings.B)
                    sendStrummedCorrect();
                else if (pitchInHz > 195.0 && pitchInHz < 197.0 && currentString == Strings.G)
                    sendStrummedCorrect();
                else if (pitchInHz > 145.0 && pitchInHz < 147.0 && currentString == Strings.D)
                    sendStrummedCorrect();
                else if (pitchInHz > 108.0 && pitchInHz < 112.0 && currentString == Strings.A)
                    sendStrummedCorrect();
                else if (pitchInHz > 81.0 && pitchInHz < 83.0 && currentString == Strings.E2)
                    sendStrummedCorrect();
            }
        }));
        new Thread(dispatcher,"Audio Dispatcher").start();
    }
}
