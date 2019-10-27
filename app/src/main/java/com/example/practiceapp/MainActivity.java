package com.example.practiceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button connectbutton;
    TextView messagetextbox;
    TextView wifitext;
    TextView peertext;
    Button sendbutton;

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    WifiManager wifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectbutton = findViewById(R.id.connectbutton);
        messagetextbox = findViewById(R.id.messagetextbox);
        sendbutton = findViewById(R.id.sendbutton);

        wifitext = findViewById(R.id.wifistatustext);
        peertext = findViewById(R.id.peerstatustext);
        initializeWifi();

        if(wifiManager.isWifiEnabled()){
            wifitext.setText("WiFi: ON");
            connectbutton.setEnabled(true);
        }

        peerDiscovery();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }


    private void initializeWifi() {

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        //receive broadcasts from wifip2p
        receiver = new WifiBroadcastReceiver(manager, channel, this);

        // fix which intents to receive by WifiBroadCastReceiver
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

    }

    private void peerDiscovery() {

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("debugwifi", "onSuccess: peer discovery successfull");

                messagetextbox.setEnabled(true);
                sendbutton.setEnabled(true);
            }

            @Override
            public void onFailure(int reason) {
                Log.d("debugwifi", "onSuccess: peer discovery failed (fail code-"+reason+")");
            }
        });

    }


    public void wifiClick(View view) {

        if(wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            wifitext.setText("WiFi: OFF");

            peertext.setText("Peer: none");
            connectbutton.setEnabled(false);
            sendbutton.setEnabled(false);
            messagetextbox.setEnabled(false);
        }
        else {
            wifiManager.setWifiEnabled(true);
            wifitext.setText("WiFi: ON");

            connectbutton.setEnabled(true);
        }
    }


    public void connectClick(View view) {

        if( true ) {

            peerDiscovery();

            /*messagetextbox.setEnabled(true);
            sendbutton.setEnabled(true);*/
        }
    }


    public void sendClick(View view) {

        //TODO: send data via wifi

    }


}
