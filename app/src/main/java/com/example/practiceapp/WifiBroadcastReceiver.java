package com.example.practiceapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            //Log.d("debugwifi", "onPeersAvailable: do notihng " + peerList.getDeviceList().toString());

            List<WifiP2pDevice> newPeers = new ArrayList(peerList.getDeviceList());

            if(!newPeers.equals(peers)){
                peers.clear();
                peers.addAll(newPeers);

                activity.peertext.setText("Peers: "+peers.size() +" (");

                for(int i=0;i<peers.size();i++) {
                    if(i==peers.size()-1){
                        activity.peertext.append(" " + peers.get(i).deviceName + " )");
                        break;
                    }
                    activity.peertext.append(" " + peers.get(i).deviceName + ",");
                }

                Log.d("debugwifi", "onPeersAvailable: peers list updated - "+peers.toString());
            }

            if(peers.size()==0)
                Log.d("debugwifi", "onPeersAvailable: no peers found");
        }
    };

    public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity activity) {

        super();

        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    public List<WifiP2pDevice> getPeers() {
        return peers;
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
                manager.requestPeers(channel,peerListListener);
        }

        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            Log.d("debugwifi", "onReceive: peer connected/disconnected ");
        }

        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            Log.d("debugwifi", "onReceive: my device wifi status changed ");
        }

    }




}
