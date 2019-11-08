package com.example.practiceapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button connectbutton;
    TextView messagetextbox;
    TextView wifitext;
    TextView peertext;
    Button sendbutton;

    Button locationbutton;
    TextView locationtext;


    User me;


/**wifi p2p variables**/
    static Boolean isServer;
    static InetAddress serverInetAddress;
    static Boolean wifiState;


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

                MainActivity.isServer = true;
                MainActivity.serverInetAddress = groupOwnerAddress;
                Log.d("socketdebug", "onConnectionInfoAvailable: MainActivity.isServer = "+MainActivity.isServer
                        + " server ip = "+groupOwnerAddress);


                Toast.makeText(MainActivity.this, "You are now Server",Toast.LENGTH_LONG).show();

                Log.d("debugwifi", "onConnectionInfoAvailable: server");
            } else if (info.groupFormed) {
                // The other device acts as the client. In this case,
                // you'll want to create a client thread that connects to the group
                // owner.

                MainActivity.isServer = false;
                MainActivity.serverInetAddress = groupOwnerAddress;
                Log.d("socketdebug", "sendClick: MainActivity.isServer = "+MainActivity.isServer
                        + " server ip = "+serverInetAddress.getHostAddress());

                Toast.makeText(MainActivity.this, "You are now client",Toast.LENGTH_LONG).show();

                Log.d("debugwifi", "onConnectionInfoAvailable: client");
            }

        }
    };
/**end**/



/**location variables declaration**/

    private static final String LOCATION_TAG = "debuglocation";
    private static final int ACCESS_FINE_LOCATION_REQUEST_CODE = 100;
    private Boolean isLocationEnabled = false;

    private FusedLocationProviderClient locationClient;
    private LocationRequest locationRequest;

    //callback for location request
    private LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            Log.d(LOCATION_TAG, "onLocationAvailability: location available");
        }
    };

    private String langlat = "";

/**end**/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectbutton = findViewById(R.id.connectbutton);
        messagetextbox = findViewById(R.id.messagetextbox);
        sendbutton = findViewById(R.id.sendbutton);
        locationbutton = findViewById(R.id.locationbutton);
        locationtext = findViewById(R.id.locationtext);

        /**wifip2p initialization**/
        wifitext = findViewById(R.id.wifistatustext);
        peertext = findViewById(R.id.peerstatustext);
        initializeWifi();

        if(wifiManager.isWifiEnabled()){
            wifitext.setText("WiFi: ON");
            connectbutton.setEnabled(true);
            wifiState = true;
        }
        else
            wifiState = false;

        peerDiscovery();
        /**end**/

        /**locations initialization**/
        initializeLocation();
        /**end**/


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

/**location methods*/

    private void initializeLocation(){

        locationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

/**end*/


/**wifip2p methods**/

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

        // Picking the first device(phone?) found on the network.
        Boolean deviceFound = false;

        WifiP2pDevice device = null;
        for (int i = 0; i < peers.size(); i++){

            //if(peers.get(i)!=null){}
            Log.d("debugwifi", peers.get(i).primaryDeviceType);

            if (peers.get(i).primaryDeviceType.charAt(0)=='1') {
                device = peers.get(i);
                deviceFound=true;
                Log.d("debugwifi", "connect: first phone? peer obtained");

                break;
            }
        }

        if(deviceFound==true) {
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
        else
            Toast.makeText(MainActivity.this,"No connectable peers found.",Toast.LENGTH_SHORT).show();
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


        String message = messagetextbox.getText().toString();
        messagetextbox.setText("");

        Log.d("socketdebug", "sendClick: "+message);

        transferData(message, false);

    }

    private void transferData(String message,Boolean sendLocation) {

        if(message!=null){
            if(MainActivity.isServer) {
                Log.d("socketdebug", "sendClick: MainActivity.isServer = "+MainActivity.isServer
                        + " server ip = "+serverInetAddress.getHostAddress());

                me = new User(8888, MainActivity.this); //as user
            }
            else {
                Log.d("socketdebug", "sendClick: MainActivity.isServer = "+MainActivity.isServer
                        + " server ip = "+serverInetAddress.getHostAddress());

                me = new User(8888, MainActivity.serverInetAddress.getHostAddress(), MainActivity.this); //as server
            }

            if(sendLocation)
                me.execute("!loc: "+message);
            else
                me.execute(message);
        }

    }

    /**wifip2p methods end**/


    public void locationClick(View view){

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
            //request user permission

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ACCESS_FINE_LOCATION_REQUEST_CODE);
        }

        else {

            locationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            if (location != null) {

                                locationtext.setText("Location: "+location.getLatitude()+","+location.getLongitude());

                                //locationClient.removeLocationUpdates(locationCallback);

                                transferData(location.getLatitude()+","+location.getLongitude()+" "+location.getAccuracy(), true);

                                Log.d(LOCATION_TAG, "onSuccess: device's last known location acquired. langlat-"
                                        + location.getLongitude()+","+location.getLatitude());
                            }
                            else if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){
                                    //request user permission

                                    ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ACCESS_FINE_LOCATION_REQUEST_CODE);

                            }
                            else {
                                Log.d(LOCATION_TAG, "onSuccess: user permission request done. location is null");

                                checkDeviceLocationSettings();

                                if(isLocationEnabled) {
                                    locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                                }
                            }
                        }
                    });

        }

    }

    private void checkDeviceLocationSettings() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    try{

                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,101); //runs onActivityResult() callback

                    }catch (IntentSender.SendIntentException sendEx){
                        //ignore
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 101){
            if(Activity.RESULT_OK == resultCode){
                isLocationEnabled = true;
            }
            else{
                Toast.makeText(this,"please turn on Locations.",Toast.LENGTH_SHORT).show();
                isLocationEnabled = false;
            }
        }
    }
}
