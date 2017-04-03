package com.sih.mymqtt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            MqttAndroidClient mqttClient = new MqttAndroidClient(this, "ssadata.iptime.org:1883", "sih");
            mqttClient.connect();
            mqttClient.subscribe("test", 0);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(this.getClass().getSimpleName(), "[rascaldom] connectionLost()");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(this.getClass().getSimpleName(), "[rascaldom] messageArrived()");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(this.getClass().getSimpleName(), "[rascaldom] deliveryComplete()");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
