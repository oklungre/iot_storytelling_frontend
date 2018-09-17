package edu.ntnu.iot_storytelling_sensor;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import edu.ntnu.iot_storytelling_sensor.iot_storytelling_network.NetworkTask;


public class MainActivity extends AppCompatActivity implements View.OnDragListener, edu.ntnu.iot_storytelling_sensor.iot_storytelling_network.NetworkInterface {
    public final static int QR_Call=0;
    private TextView m_object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
        findViewById(R.id.topleft).setOnDragListener(this);
        findViewById(R.id.topright).setOnDragListener(this);
        findViewById(R.id.bottomleft).setOnDragListener(this);
        findViewById(R.id.bottomright).setOnDragListener(this);

        m_object = (TextView) findViewById(R.id.myimage1);
        m_object.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipData data = ClipData.newPlainText("", "");
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                            v);
                    v.startDrag(data, shadowBuilder, v, 0);
                    v.setVisibility(View.INVISIBLE);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        Drawable enterShape = getDrawable(R.drawable.shape_droptarget);
        Drawable normalShape = getDrawable(R.drawable.shape);

        switch(event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                v.setBackground(enterShape);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                v.setBackground(normalShape);
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                m_object.setVisibility(View.VISIBLE);
                v.setBackground(normalShape);
                break;

            case DragEvent.ACTION_DROP:
                View view = (View) event.getLocalState();
                ViewGroup owner = (ViewGroup) view.getParent();
                owner.removeView(view);
                LinearLayout container = (LinearLayout) v;
                container.addView(view);
                int id = container.getId();
                Log.d("Drag", "ID: " + Integer.toString(id));

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
                Log.d("Drag", "DEFAULT");
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
