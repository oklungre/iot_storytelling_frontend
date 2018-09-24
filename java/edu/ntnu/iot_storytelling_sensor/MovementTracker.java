package edu.ntnu.iot_storytelling_sensor;

import android.util.Log;

public class MovementTracker {

    private boolean INIT_CALLED = false;
    private double last_x=0;
    private double last_y=0;
    private Long last_time;

    public void start_tracking(double x, double y){
        last_x = x;
        last_y = y;
        last_time = System.currentTimeMillis();
        INIT_CALLED = true;
    }
    public double addMovement(double x, double y){
        if(!INIT_CALLED)
            return 0.0;

        double distance = Math.hypot(x - last_x, y - last_y);
        Long time_past = System.currentTimeMillis() - last_time;

        double velocity = distance / time_past;
        Log.d("MovementTracking", String.valueOf(velocity) + " = "
                                         + String.valueOf(distance) + " / "
                                         + String.valueOf(time_past));
        last_x = x;
        last_y = y;

        if(Double.isInfinite(velocity))
            return 0.0;
        return velocity;
    }
}
