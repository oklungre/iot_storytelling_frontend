package edu.ntnu.iot_storytelling_sensor.Network;

import org.json.JSONObject;

public interface NetworkInterface {
    void startRequest(JSONObject packet);
    void serverResult(String result);
}
