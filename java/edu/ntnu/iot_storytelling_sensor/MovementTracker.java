package edu.ntnu.iot_storytelling_sensor;

import android.util.Log;

import java.util.ArrayList;

public class MovementTracker {

    private boolean INIT_CALLED = false;
    private myPoint start_point;
    private Long start_time;
    private ArrayList<myPoint> m_tracking_list;

    public void start_tracking(double x, double y){
        start_time = System.currentTimeMillis();
        INIT_CALLED = true;
        start_point = new myPoint(x,y);
        m_tracking_list = new ArrayList<myPoint>();
        m_tracking_list.add(new myPoint(0,0)); // center coordinate system
    }

    public void addMovement(double x, double y){
        x -= start_point.x();
        y -= start_point.y();
        m_tracking_list.add(new myPoint(x,y));
    }

    public double getVelocity(){
        if(!INIT_CALLED)
            return 0.0;

        Long new_time = System.currentTimeMillis();
        Long time_past = new_time - start_time;

        double distance = overall_distance();
        double velocity = distance / time_past;
        Log.d("MovementTracking", "Lenght: " + String.valueOf(m_tracking_list.size()));
        Log.d("MovementTracking", String.valueOf(velocity) + " = "
                                             + String.valueOf(distance) + " / "
                                             + String.valueOf(time_past));

        if(Double.isInfinite(velocity))
            return 0.0;

        INIT_CALLED = false;
        return velocity;
    }

    private double overall_distance(){
        double sum=0;

        for (myPoint step: m_tracking_list) {
            sum += step.length();
        }
        return sum;
    }

    private class myPoint{
        private double m_x;
        private double m_y;

        myPoint(double x, double y){
            m_x = x;
            m_y = y;
        }

        double x(){return m_x;}
        double y(){return m_y;}

        double length(){
            return Math.hypot(m_x, m_y);
        }
    }
}
