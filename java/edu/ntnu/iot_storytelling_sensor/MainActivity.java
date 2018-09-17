package edu.ntnu.iot_storytelling_sensor;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ntnu.iot_storytelling_sensor.iot_storytelling_network.NetworkTask;


public class MainActivity extends AppCompatActivity implements View.OnDragListener, edu.ntnu.iot_storytelling_sensor.iot_storytelling_network.NetworkInterface {

    private TextView m_text;

    public final static int QR_Call=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout environment = (RelativeLayout) findViewById(R.id.environment);
        m_text = (TextView) findViewById(R.id.text_view);

        /* Firebase Init */
        FirebaseMessaging.getInstance().subscribeToTopic("host")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.d("Firebase", "Cannot subscribe to topic");
                        }
                    }
                });

        try {
            Intent i = getIntent();
            String msg = i.getStringExtra("message");
            Log.d("Firebase", msg);
            NetworkTask.set_host(msg);
        }catch(NullPointerException e){
            Log.d("Firebase", "Empty Intent");
        }

        /* Drag and Drop Init */
        environment.setOnDragListener(this);

        m_text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText(".", "..");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(m_text);
                    m_text.startDrag(data, shadowBuilder, m_text, 0);
                    m_text.setVisibility(View.INVISIBLE);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public boolean onDrag(View view, DragEvent event) {
        switch(event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                break;
            case DragEvent.ACTION_DRAG_EXITED :
                break;
            case DragEvent.ACTION_DRAG_LOCATION  :
                break;
            case DragEvent.ACTION_DRAG_ENDED   :
                m_text.setVisibility(View.VISIBLE);
                break;

            case DragEvent.ACTION_DROP:
                int x_cord = (int) event.getX() - m_text.getWidth() / 2;
                int y_cord = (int) event.getY() - m_text.getHeight() / 2;
                m_text.setX(x_cord);
                m_text.setY(y_cord);
                m_text.setVisibility(View.VISIBLE);

                /*for testing*/
                //Intent intent = new Intent(MainActivity.this, QRScanner.class);
                //startActivityForResult(intent, QR_Call);
                String qr_msg = "Hello World";
                Log.d("Hi", "This was successfull" + qr_msg);
                JSONObject pkg = new JSONObject();
                try {
                    pkg.put("QR_Code", qr_msg);
                    startRequest(pkg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /* QR CODE SCANNER CALLBACK*/
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_Call) {
            if (resultCode == RESULT_OK) {
                String qr_msg = data.getStringExtra("scanCode");
                Log.d("Hi", "This was successfull" + qr_msg);
                JSONObject pkg = new JSONObject();
                try {
                    pkg.put("QR_Code", qr_msg);
                    startRequest(pkg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
               Log.d("Hi", "This was a failure");

            }
        }
    }

    /* NETWORKING */
    @Override
    public void startRequest(JSONObject packet) {
        Log.d("Network","Sending: " + packet.toString());
        edu.ntnu.iot_storytelling_sensor.iot_storytelling_network.NetworkTask network
                = new edu.ntnu.iot_storytelling_sensor.iot_storytelling_network.NetworkTask(this);
        network.send(packet);
    }

    @Override
    public void serverResult(String result) {
        if(!result.isEmpty()){
            Toast toast = Toast.makeText(getApplicationContext(), "Error: " + result, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /* FIREBASE NETWORKING */
    protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        try {
            String msg = i.getStringExtra("message");
            Log.d("Firebase", msg);
            NetworkTask.set_host(msg);
        }catch(NullPointerException e){
            Log.d("Firebase", "Empty Intent, onNewIntent");
        }
    }
}
