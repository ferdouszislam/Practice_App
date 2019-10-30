package com.example.practiceapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;


public class WifiBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;



    public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {

        super();

        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            // check if wifi p2p mode is enabled or disabled

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Log.d("debugwifi", "onReceive: wifi is On ");
            }
            else {
                Log.d("debugwifi", "onReceive: wifi is Off ");
            }

        }

        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            Log.d("debugwifi", "onReceive: peer list changed ");

            // request for peers
            if(manager!=null)
                manager.requestPeers(channel,activity.peerListListener);
        }

        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            Log.d("debugwifi", "onReceive: peer connected/disconnected ");

            if(manager!=null){

                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){
                    //connected to peer ask for peer connection info
                    Log.d("debugwifi", "onReceive: connected to peer requesting for peer connection info(play notification sound here?)");

                    manager.requestConnectionInfo(channel,activity.connectionInfoListener);
                }
                else {
                    Log.d("debugwifi", "onReceive: not connected to peer yet (play notification sound here?)");
                }
            }
        }

        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            Log.d("debugwifi", "onReceive: my device wifi status changed ");
        }

    }


    private void doNothing(){}

}
