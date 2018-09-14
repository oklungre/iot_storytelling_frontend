package edu.ntnu.iot_storytelling_sensor.Network;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkTask extends AsyncTask<JSONObject,JSONObject,JSONObject> {
    private static final String HOST = "";
    private static final Integer PORT = 5000;

    private NetworkInterface m_caller = null;

    public NetworkTask(NetworkInterface caller){
        m_caller = caller;
    }

    public void send(JSONObject packet){
        execute(packet);
    }
    @Override
    protected JSONObject doInBackground(JSONObject... obje) {
        Socket nsocket = null;
        String result = "";
        DataOutputStream dataOutputStream;

        try {
            /* Open Connection*/
            nsocket = new Socket();
            nsocket.connect(new InetSocketAddress(HOST, PORT),5000);
            dataOutputStream = new DataOutputStream(nsocket.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(nsocket.getInputStream()));

            /* Send data*/
            dataOutputStream.writeBytes(obje[0].toString());
            dataOutputStream.flush();

            /* Read response*/
            result = br.readLine();
            nsocket.close();
        } catch(UnknownHostException e) {
            System.out.println("Unknown host: " + HOST);
        } catch(IOException e) {
            System.out.println("No I/O");
        }

        Log.d("answer", result);

        try {
            return new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        m_caller.serverResult(result);
    }
}
