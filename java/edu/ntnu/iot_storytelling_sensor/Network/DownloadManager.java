package edu.ntnu.iot_storytelling_sensor.Network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import edu.ntnu.iot_storytelling_sensor.MainActivity;

public class DownloadManager extends AsyncTask<ArrayList, String, Boolean> {

    public static Integer HOST_PORT = 0;

    private String m_tag;
    private MainActivity m_context;


    DownloadManager(MainActivity context, String tag){
        m_tag = tag;
        m_context = context;
    }

    protected Boolean doInBackground(ArrayList... lists) {
        ArrayList files = lists[0];
        try {
            for(Integer i=0;i<files.size();i++){
                String file_name = files.get(i).toString();

                // download file
                java.net.URL url = new java.net.URL("http",
                        TCPTask.HOST_IP, HOST_PORT,
                        m_tag + "/" + file_name);
                HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();

                // get file content and save it
                InputStream input = connection.getInputStream();
                FileOutputStream out;
                out = m_context.openFileOutput(file_name, Context.MODE_PRIVATE);

                switch(m_tag){
                    case FirebaseManager.AUDIO_Key: {
                        byte data[] = new byte[1024];
                        int count;
                        while ((count = input.read(data)) != -1) {
                            out.write(data, 0, count);
                        }
                        break;
                    }
                    case FirebaseManager.IMAGE_Key: {
                        Bitmap bitmap = BitmapFactory.decodeStream(input);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        break;
                    }
                    case FirebaseManager.TEXT_Key: {
                        byte data[] = new byte[1024];
                        int count;
                        while ((count = input.read(data)) != -1) {
                            out.write(data, 0, count);
                        }
                        break;
                    }
                }

                out.flush();
                out.close();
                input.close();

                // publish file progress
                publishProgress(file_name);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected void onProgressUpdate(String... progress) {
        Log.d("Download", m_tag + ": " + progress[0]);
    }

    @Override
    protected void onPostExecute(Boolean  p) {
        m_context.download_finished(p);
        m_context = null;
    }
}