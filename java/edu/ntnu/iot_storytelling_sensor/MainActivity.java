package edu.ntnu.iot_storytelling_sensor;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ntnu.iot_storytelling_sensor.Network.NetworkInterface;
import edu.ntnu.iot_storytelling_sensor.Network.NetworkTask;
import pl.droidsonroids.gif.GifImageView;


public class MainActivity extends AppCompatActivity implements View.OnDragListener,
                                                                NetworkInterface,
                                                                View.OnTouchListener,
                                                                ValueEventListener {
    public final static int QR_Call = 0;
    public final static int PERMISSION_REQUEST_CAMERA = 1;
    public static final String HOST_KEY = "Host";
    public static final String HOST_IP_KEY = "ip";
    public static final String HOST_PORT_KEY = "tcp_port";

    private GifImageView m_field_obj;
    private GifImageView m_rel_obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* Check for permissions */
        check_camera_permission();

        DatabaseReference m_Database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference host = m_Database.child(HOST_KEY);
        host.addValueEventListener(this);

        /* Drag and Drop Init */
        findViewById(R.id.field_topleft).setOnDragListener(this);
        findViewById(R.id.field_topright).setOnDragListener(this);
        findViewById(R.id.field_bottomleft).setOnDragListener(this);
        findViewById(R.id.field_bottomright).setOnDragListener(this);
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

    /* PERMISSION REQUEST FOR CAMERS */
    private void check_camera_permission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    check_camera_permission();
                }
            }
        }
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
        int position = 0; // stays zero if m_rel_obj is active

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
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();

        if (key != null) {
            switch (key) {
                case HOST_KEY: {
                    String host_ip = dataSnapshot.child(HOST_IP_KEY).getValue(String.class);
                    Integer host_port = dataSnapshot.child(HOST_PORT_KEY).getValue(Integer.class);
                    NetworkTask.set_host(host_ip);
                    NetworkTask.set_port(host_port);
                    break;
                }
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.w("Error", "loadPost:onCancelled", databaseError.toException());
    }
}
