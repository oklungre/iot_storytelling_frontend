package edu.ntnu.iot_storytelling_sensor;

import android.content.ClipData;
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
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ntnu.iot_storytelling_sensor.Network.NetworkInterface;
import edu.ntnu.iot_storytelling_sensor.Network.NetworkTask;
import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity implements View.OnDragListener,
                                                                NetworkInterface,
                                                                View.OnTouchListener{
    public final static int QR_Call=0;
    private GifImageView m_field_obj;
    private GifImageView m_rel_obj;

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
        findViewById(R.id.parent_view).setOnDragListener(this);

        m_field_obj = (GifImageView) findViewById(R.id.myimage_fields);
        m_field_obj.setOnTouchListener(this);
        m_rel_obj = (GifImageView) findViewById(R.id.myimage_rel);
        m_rel_obj.setOnTouchListener(this);

        Button camButton = (Button) findViewById(R.id.camera_button);
        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QRScanner.class);
                startActivityForResult(intent, QR_Call);
            }
        });
    }
    /* ON TOUCH LISTENER */
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

    @Override
    public boolean onDrag(View v, DragEvent event) {
        Drawable enterShape = getDrawable(R.drawable.shape_droptarget);
        Drawable normalShape = getDrawable(R.drawable.shape);

        switch(event.getAction()) {
            case DragEvent.ACTION_DRAG_ENTERED:
                if(v.getId() != R.id.parent_view)
                    v.setBackground(enterShape);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                if(v.getId() != R.id.parent_view)
                    v.setBackground(normalShape);
                break;
            case DragEvent.ACTION_DROP:
                ViewGroup container = (ViewGroup) v;

                if(container.getId() == R.id.parent_view){
                    float x_cord = event.getX() - m_rel_obj.getWidth() / 2;
                    float y_cord = event.getY() - m_rel_obj.getHeight() / 2;
                    m_rel_obj.setX(x_cord);
                    m_rel_obj.setY(y_cord);
                    m_rel_obj.setVisibility(View.VISIBLE);
                    m_rel_obj.bringToFront();
                    m_field_obj.setVisibility(View.INVISIBLE);
                }else{
                    ViewGroup owner = (ViewGroup) m_field_obj.getParent();
                    owner.removeView(m_field_obj);
                    container.addView(m_field_obj);
                    v.setBackground(normalShape);
                    m_field_obj.setVisibility(View.VISIBLE);
                    m_rel_obj.setVisibility(View.INVISIBLE);
                }
                create_request();
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
                create_request(qr_msg);
            } else {
               Log.d("Error", "Could not read QR Code");
            }
        }
    }

    /* NETWORKING */
    private void create_request(){
        create_request("");
    }

    private void create_request(String qr_code){
        int position = 0;
        if(m_field_obj.getVisibility() == View.VISIBLE){
            ViewGroup parent = (ViewGroup) m_field_obj.getParent();
            switch(parent.getId()){
                case R.id.field_topleft:
                    position = 1;
                    break;
                case R.id.field_topright:
                    position = 2;
                    break;
                case R.id.field_bottomleft:
                    position = 3;
                    break;
                case R.id.field_bottomright:
                    position = 4;
                    break;
            }
        }

        try {
            JSONObject json_pkg = new JSONObject();

            if(position != 0)
                json_pkg.put("position", position);

            if(!qr_code.isEmpty())
                json_pkg.put("qr_code", qr_code);

            startRequest(json_pkg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startRequest(JSONObject packet) {
        NetworkTask network = new NetworkTask(this);
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
            NetworkTask.set_host(msg);
        }catch(NullPointerException e){
            Log.d("Firebase", "Empty Intent, onNewIntent");
        }
    }
}
