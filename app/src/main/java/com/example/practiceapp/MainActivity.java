package com.example.practiceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

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

    //peers and peer listener
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            //Log.d("debugwifi", "onPeersAvailable: do notihng " + peerList.getDeviceList().toString());

            List<WifiP2pDevice> newPeers = new ArrayList(peerList.getDeviceList());

            if(!newPeers.equals(peers)){
                peers.clear();
                peers.addAll(newPeers);

                peertext.setText("Peers: "+peers.size() +" (");

                for(int i=0;i<peers.size();i++) {
                    if(i==peers.size()-1){
                        peertext.append(" " + peers.get(i).deviceName + " )");
                        break;
                    }
                    peertext.append(" " + peers.get(i).deviceName + ",");
                }

                Log.d("debugwifi", "onPeersAvailable: peers list updated - "+peers.toString());
            }

            if(peers.size()==0)
                Log.d("debugwifi", "onPeersAvailable: no peers found");
        }
    };

    //connection listener
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {

            // InetAddress from WifiP2pInfo struct.
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            // After the group negotiation(?), we can determine the group owner.
            if (info.groupFormed && info.isGroupOwner) {
                // Do whatever tasks are specific to the group owner.
                // One common case is creating a server thread and accepting
                // incoming connections.

                Toast.makeText(MainActivity.this, "You are now Server",Toast.LENGTH_LONG).show();

                Log.d("debugwifi", "onConnectionInfoAvailable: server");
            } else if (info.groupFormed) {
                // The other device acts as the client. In this case,
                // you'll want to create a client thread that connects to the group
                // owner.

                Toast.makeText(MainActivity.this, "You are now client",Toast.LENGTH_LONG).show();

                Log.d("debugwifi", "onConnectionInfoAvailable: client");
            }

        }
    };


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

                Toast.makeText(MainActivity.this, "Peer discovery success",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.d("debugwifi", "onSuccess: peer discovery failed (fail code-"+reason+")");

                Toast.makeText(MainActivity.this, "Peer discovery FAILED!",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void connect() {
        // Picking the first device found on the network.
        WifiP2pDevice device = peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.

                messagetextbox.setEnabled(true);
                sendbutton.setEnabled(true);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
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

        peerDiscovery();

        if( peers.size()!=0 ) {

            connect();

        }else {
            Toast.makeText(MainActivity.this, "no peers press again", Toast.LENGTH_SHORT).show();
        }
    }


    public void sendClick(View view) {

        //TODO: send data via wifi

    }


}
