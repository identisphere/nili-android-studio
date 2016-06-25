package com.nili.utilities;

import java.io.InputStream;

import android.os.Message;

import com.nili.globals.Commands;
import com.nili.main.MainActivity;
import com.nili.operator.Operator;

public class BtReadData extends Thread
{

	private Operator operator = null;
	public InputStream inputStream = null;
	private MainActivity mainActivity = null;
	private boolean receivedFirst = false;
    public char[] receivedChars = new char[24];

	public BtReadData()
	{
	}
	
    public void set(MainActivity mainActivity, InputStream inputStream, Operator operator)
    {
		this.inputStream = inputStream;
		this.mainActivity = mainActivity;
    	this.operator = operator;
		Thread.currentThread().setName("Read Data Thread");

    }
    
    @Override
    public void run()
    {
    	while(true) 
        {
            if (this.inputStream != null) {
                try {
                	// bug in arduino sends every message twice
                	if(!receivedFirst)
                	{
                		receivedChars = new char[24];
                    	int receivedChar = this.inputStream.read();
                    	if(Character.toChars(receivedChar)[0]!='+') 
                    		continue;
                        for(int i=0; i<24; i++)
                        {
                        	receivedChars[i] = (char)this.inputStream.read();
                        }
                        this.inputStream.read();

                        if(BtReadData.this.operator == null) return;
                        
						Message operatorMessage = new Message();
                        operatorMessage.obj = new String(receivedChars);
                        operatorMessage.arg1 = Commands.Operator.receivePress;
                		operator.mHandler.sendMessage(operatorMessage);
                        
                        //operator.receivedPressFromUser(new String(receivedChars));
                		receivedFirst = true;
                	}
                	else
                		receivedFirst = false;
                } 
                catch (Exception e) 
                { //if an error appear, we return to the Main activity
                	this.mainActivity.runOnUiThread(new Runnable() {@Override public void run()
                    {
	                    BtReadData.this.mainActivity.showToast("error reading data from bluetooth");
                    }});
                    break;
                }
            }
        }
    }
	public void setOperator(Operator operator) 
	{
		this.operator = operator;
	}
}