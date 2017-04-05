package com.sih.mymqtt;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    private HistoryAdapter mAdapter;

    private MqttAndroidClient mqttAndroidClient;

    private final String serverUri = "";
    private String clientId = "";
    private final String subscriptionTopic = "";
    private final String publishTopic = "";
    private final String publishMessage = "Hello MQTT!";

    private EditText etMessage;
    private Button btSend;

    private InputMethodManager imm;

    private boolean isSend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publishMessage(publishMessage);
            }
        });

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HistoryAdapter(new ArrayList<String>());
        mRecyclerView.setAdapter(mAdapter);

        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        etMessage = (EditText) findViewById(R.id.et_publish);
        btSend = (Button) findViewById(R.id.bt_send);
        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etMessage != null && !"".equals(etMessage.getText().toString())) {
                    publishMessage(etMessage.getText().toString());
                    imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
                    etMessage.setText("");
                }
            }
        });

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(this);

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to connect to: " + serverUri);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void addToHistory(String mainText) {
        System.out.println("LOG: " + mainText);
        mAdapter.add(mainText);
        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_LONG).setAction("Action", null).show();
    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to subscribe");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMessage(String msg) {
        try {
            MqttMessage message = new MqttMessage(msg.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        addToHistory("The Connection was lost.");
        cause.printStackTrace();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String who = isSend ? "Me: " : "You: ";
        addToHistory(who + new String(message.getPayload()));
        isSend = false;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        isSend = true;
    }
}
