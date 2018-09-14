package edu.ntnu.iot_storytelling_sensor;

import android.content.ClipData;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONObject;

import edu.ntnu.iot_storytelling_sensor.Network.NetworkInterface;
import edu.ntnu.iot_storytelling_sensor.Network.NetworkTask;

public class MainActivity extends AppCompatActivity implements View.OnDragListener, NetworkInterface {

    private TextView m_text;

    public final static int QR_Call=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout environment = (RelativeLayout) findViewById(R.id.environment);
        m_text = (TextView) findViewById(R.id.text_view);

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
                Intent intent = new Intent(MainActivity.this, QRScanner.class);
                startActivityForResult(intent, QR_Call);
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
                Log.d("Hi", "This was successfull" + data.getStringExtra("scanCode"));
            } else {
               Log.d("Hi", "This was a failure");

            }
        }
    }

    /* NETWORKING */
    @Override
    public void startRequest(JSONObject packet) {
        Log.d("Network","Sending: " + packet.toString());
        NetworkTask network = new NetworkTask(this);
        network.send(packet);
    }

    @Override
    public void serverResult(JSONObject result) {
        Log.d("Network", "Answer: " + result.toString());
    }
}
