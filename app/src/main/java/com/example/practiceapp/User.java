package com.example.practiceapp;

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
            Log.d("socketdebug", "init: initializing as server");

            try {
                serverSocket = new ServerSocket(this.port);
                client = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();

                Log.d("socketdebug", "init: problem starting server or connectiong to client");
                publishProgress("Error starting server or connecting to client. Retry.");
            }
        }
        else {
            //this is client
            Log.d("socketdebug", "init: initializing as client");

            try {
                client = new Socket();
                client.connect(new InetSocketAddress(this.serverIp,this.port),1000);
            } catch (IOException e) {
                e.printStackTrace();

                Log.d("socketdebug", "init: problem starting client socket send again");
                publishProgress("Error starting client socket. Retry.");
            }
        }

        try {
            input = new DataInputStream(this.client.getInputStream());
            output = new DataOutputStream(this.client.getOutputStream());
        } catch (IOException e) {

            e.printStackTrace();

            Log.d("socketdebug", "init: problem starting i/o streams");
        }
    }

    private void showToast(String alert){
        Toast.makeText(activity.getApplicationContext(), alert, Toast.LENGTH_SHORT).show();
    }



    /*@Override
    protected void onPreExecute() {
        showToast("AsyncTask started");
        Log.d("socketdebug", "onPreExecute: AsyncTask started");
    }*/

    @Override
    protected String doInBackground(String... strings) { Log.d("socketdebug", "doInBackground: "+strings[0]);

        this.init();

        String sendMessage = strings[0];

        if(sendMessage!=null) {
            try {
                output.writeUTF(sendMessage);
            } catch (IOException e) {
                e.printStackTrace();

                Log.d("socketdebug", "doInBackground: message not sent");
                publishProgress("Error! message not sent.");
            } catch (NullPointerException ne){
                ne.printStackTrace();

                Log.d("socketdebug", "doInBackground: server not connected yet");
                publishProgress("Not connected to server. Retry.");
            }
        }


        String receivedMessage = null;
        try {
            receivedMessage = input.readUTF();
        } catch (IOException e) {
            e.printStackTrace();

            Log.d("socketdebug", "doInBackground: message not received");
            publishProgress("Error! message not received.");
        } catch (NullPointerException ne){
            ne.printStackTrace();

            Log.d("socketdebug", "doInBackground: server not connected yet");
            publishProgress("Not connected to server. Retry.");
        }

        if(receivedMessage==null)
            receivedMessage = "";


        try {
            serverSocket.close();
            client.close();
        } catch (Exception e) {
            e.printStackTrace();

            Log.d("socketdebug", "doInBackground: socket closing error");
        }

        return receivedMessage;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        showToast(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        //Log.d("socketdebug", "onPostExecute: end of AsyncTask");

        if(s.substring(0,3).equals("!loc: ")){
            //show in map
            String langlat = s.substring(0,s.indexOf(" ") );
            String accuracy = s.substring(s.indexOf(" ")+1);


        }

        else{
            Toast messageToast = Toast.makeText(activity.getApplicationContext(), s, Toast.LENGTH_LONG);
            messageToast.setGravity(Gravity.CENTER, 0, 0);
            messageToast.show();
        }

    }
}
