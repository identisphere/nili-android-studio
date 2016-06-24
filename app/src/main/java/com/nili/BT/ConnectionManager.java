package com.nili.BT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.nili.globals.Commands;
import com.nili.globals.Globals;
import com.nili.main.MainActivity;

public class ConnectionManager extends Thread 
{
    private String address;
    private BluetoothAdapter btAdapter = null;
    BluetoothSocket socket = null;
	private OutputStream outputStream = null;
	public InputStream inputStream = null;
	private BluetoothDevice btDevice = null;
    private static final UUID UUID_me = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // the SPP uuid is good enough for our needs

    public MainActivity mainActivity = null;
	protected TextView dataReceived;
	public Handler mHandler;
    
	public ConnectionManager() 
    {
    }

	public void run()
	{
		Thread.currentThread().setName("Connection Manager");

		synchronized(this)
		{
	    	btAdapter = BluetoothAdapter.getDefaultAdapter();
	        while(!btAdapter.isEnabled())
	        {
	        	btAdapter.enable();
	        	try
	        	{
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }

	        try
	        {
		        this.connect();
	        	Globals.isConnectedToBT = true;
	        }
	        catch(Exception ex)
	        {
	        	Globals.isConnectedToBT = false;
	        }
	        finally
	        {
	        	this.notify();
	        }
		}
		
		Looper.prepare();
		
		mHandler = new Handler() {
			public void handleMessage(Message message)
			{
				if(!Globals.isConnectedToBT)
				{
					System.out.println("unable to send message to blue tooth");
					return;
				}
				if(message.arg1== Commands.ConnectionManager.sendToBt)
				{
					sendDataToBt((String)message.obj);
					return;
				}
			}
		};
		
		Looper.loop();

	}

	// formerly "write"
    public void sendDataToBt(String data) 
    {
        try 
        {
            outputStream.write(data.getBytes());
            outputStream.write(data.getBytes());
            outputStream.flush();
        }
        catch (IOException e) 
        {
            e.getStackTrace();
        }
    }
    
	public void connect() throws Exception 
	{
        resetConnection();
        if(btAdapter.isDiscovering())
        {
            btAdapter.cancelDiscovery();
        }
        if (btDevice == null)
        {
            btDevice = btAdapter.getRemoteDevice(address);
        }

        //here we try to connect as a client using different methods and channels; Why? Because different android devices, connect differently to bluetooth devices.
        boolean isClientConnected=false;
        
        socket = btDevice.createRfcommSocketToServiceRecord(UUID_me);
        isClientConnected=true;

        for (int i = 1; i <= 3 &&!isClientConnected; i++) 
        {
            Method m = btDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            socket = (BluetoothSocket) m.invoke(btDevice, 1);
            isClientConnected = true;
        }
        this.socket.connect();
        
        outputStream = socket.getOutputStream();
        inputStream  = socket.getInputStream();
    }
    
    private void resetConnection() {
        if (inputStream != null) {
            try
            {
                inputStream.close();
            } catch (Exception e) {
                e.getStackTrace();
            }
            inputStream = null;
        }
        if (outputStream != null) {
            try
            {
                outputStream.close();
            } catch (Exception e) {
                e.getStackTrace();
            }
            outputStream = null;
        }
        if (socket != null) {
            try
            {
                socket.close();
            } catch (Exception e) {
                e.getStackTrace();
            }
            socket = null;
        }
    }


    public void set(MainActivity mainActivity, String btAddress) 
    {
        address = btAddress;
        this.mainActivity = mainActivity;

        Thread.currentThread().setName("Connection Manager Thread");
	}

}
