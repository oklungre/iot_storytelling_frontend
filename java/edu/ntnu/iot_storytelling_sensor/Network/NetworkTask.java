package edu.ntnu.iot_storytelling_sensor.Network;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkTask extends AsyncTask<JSONObject,JSONObject,String> {
    private static String HOST = "10.22.76.45";
    private static final Integer PORT = 8888;

    private NetworkInterface m_caller = null;

    public NetworkTask(NetworkInterface caller){
        m_caller = caller;
    }

    public static void set_host(String host){
        HOST = host;
    }
    public void send(JSONObject packet){
        execute(packet);
    }
    @Override
    protected String doInBackground(JSONObject... obje) {
        Socket nsocket = null;
        DataOutputStream dataOutputStream;

        try {
            /* Open Connection*/
            nsocket = new Socket();
            nsocket.connect(new InetSocketAddress(HOST, PORT),500);
            dataOutputStream = new DataOutputStream(nsocket.getOutputStream());

            /* Send data*/
            dataOutputStream.writeBytes(obje[0].toString());
            dataOutputStream.flush();
            nsocket.close();
        } catch(UnknownHostException e) {
            Log.e("Network", "Unknown host: " + HOST);
            return "Unknown Host";
        } catch(IOException e) {
            Log.e("Network", "No I/O");
            return "Failed to Connect";
        } catch(Exception e){
            Log.e("Network", e.toString());
            return "Unexpected Error";
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            m_caller.serverResult(result);
        }catch(NullPointerException e){
            m_caller.serverResult("");
        }
    }
}
