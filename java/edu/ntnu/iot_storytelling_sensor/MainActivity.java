package edu.ntnu.iot_storytelling_sensor;

import android.content.ClipData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnDragListener{
    private TextView m_text;

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
                break;
            default:
                break;
        }
        return true;
    }
}
