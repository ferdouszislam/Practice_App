package com.example.practiceapp;

import android.app.usage.NetworkStatsManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class User extends AsyncTask<String,String,String> {

    private static final String SOCKET_DEBUG = "socketdebug";

    private MainActivity activity;
    private int port;
    private String serverIp;

    private ServerSocket serverSocket;
    private Socket client;
    private DataInputStream input;
    private DataOutputStream output;

    public User(int port, MainActivity activity) {
        this.port = port;
        this.activity = activity;
        this.serverIp = null;
    }

    public User(int port, String serverIp, MainActivity activity) {
        this.port = port;
        this.serverIp = serverIp;
        this.activity = activity;
    }



    private void init(){

        if(this.serverIp==null){
            //this is server
            Log.d(User.SOCKET_DEBUG, "init: initializing as server");

            try {
                serverSocket = new ServerSocket(this.port);
                client = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();

                Log.d(User.SOCKET_DEBUG, "init: problem starting server or connectiong to client");
                publishProgress("Error starting server or connecting to client. Retry.");
            }
        }
        else {
            //this is client
            Log.d(User.SOCKET_DEBUG, "init: initializing as client");

            try {
                client = new Socket();
                client.connect(new InetSocketAddress(this.serverIp,this.port),1000);
            } catch (IOException e) {
                e.printStackTrace();

                Log.d(User.SOCKET_DEBUG, "init: problem starting client socket send again");
                publishProgress("Error starting client socket. Retry.");
            }
        }

        try {
            input = new DataInputStream(this.client.getInputStream());
            output = new DataOutputStream(this.client.getOutputStream());
        } catch (IOException e) {

            e.printStackTrace();

            Log.d(User.SOCKET_DEBUG, "init: problem starting i/o streams");
        }
    }

    private void showToast(String alert){
        Toast.makeText(activity.getApplicationContext(), alert, Toast.LENGTH_SHORT).show();
    }



    /*@Override
    protected void onPreExecute() {
        showToast("AsyncTask started");
        Log.d(User.SOCKET_DEBUG, "onPreExecute: AsyncTask started");
    }*/

    @Override
    protected String doInBackground(String... strings) { Log.d(User.SOCKET_DEBUG, "doInBackground: "+strings[0]);

        this.init();

        String sendMessage = strings[0];

        if(sendMessage!=null) {
            try {
                output.writeUTF(sendMessage);
            } catch (IOException e) {
                e.printStackTrace();

                Log.d(User.SOCKET_DEBUG, "doInBackground: message not sent");
                publishProgress("Error! message not sent.");
            } catch (NullPointerException ne){
                ne.printStackTrace();

                Log.d(User.SOCKET_DEBUG, "doInBackground: server not connected yet");
                publishProgress("Not connected to server. Retry.");
            }
        }


        String receivedMessage = null;
        try {
            receivedMessage = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();

            Log.d(User.SOCKET_DEBUG, "doInBackground: message not received");
            publishProgress("Error! message not received.");
        } catch (NullPointerException ne){
            ne.printStackTrace();

            Log.d(User.SOCKET_DEBUG, "doInBackground: server not connected yet");
            publishProgress("Not connected to server. Retry.");
        }

        if(receivedMessage==null)
            receivedMessage = "";


        try {
            serverSocket.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();

            Log.d(User.SOCKET_DEBUG, "doInBackground: socket closing error");
        }

        return receivedMessage;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        showToast(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        //Log.d(User.SOCKET_DEBUG, "onPostExecute: end of AsyncTask");

        Toast messageToast;

        if(s!=null && s!="") {

            try {
                if (s.substring(0, 5).equals("!loc:")) {

                    //format: latitude,longitude accuracy
                    String langlat = s.substring(5, s.indexOf(" "));
                    String accuracy = s.substring(s.indexOf(" ") + 1);

                    s = langlat + " " + accuracy;

                    double latitude = Double.parseDouble( langlat.substring(0,langlat.indexOf(",")) );
                    double longitude = Double.parseDouble( langlat.substring(langlat.indexOf(",")+1) );
                    Log.d(User.SOCKET_DEBUG, "onPostExecute: co-ordinates converted to double: "+latitude+" "+longitude);

                    //show on map
                    Intent startMapIntent = new Intent(activity, MapsActivity.class);
                    startMapIntent.putExtra(Constants.MAP_LATITUDE, latitude);
                    startMapIntent.putExtra(Constants.MAP_LONGITUDE, longitude);

                    activity.startActivity(startMapIntent);

                }
            }catch (Exception se){
                se.printStackTrace();
                Log.e(User.SOCKET_DEBUG, "onPostExecute: plain text received or formatting error\n"+se.getMessage());
            }


            messageToast = Toast.makeText(activity.getApplicationContext(), s, Toast.LENGTH_LONG);
            messageToast.setGravity(Gravity.CENTER, 0, 0);
            messageToast.show();
        }

        else
            Log.d(User.SOCKET_DEBUG, "onPostExecute: null message received");

    }
}
