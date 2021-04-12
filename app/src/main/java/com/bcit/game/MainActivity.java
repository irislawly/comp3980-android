package com.bcit.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Socket socket;

    private static final int SERVER_PORT = 8080;
    private static final String SERVER_IP = "192.168.1.89";
    int choice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        new Thread(new ClientThread()).start();
        Log.w("Debug", "Welcome to TTT!");
        new Thread(new RcvThread()).start();




    }

    public void sendMessage(View view)
    {

        try {
            //reading?
            /*
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final String st = input.readLine();
            Toast.makeText(MainActivity.this, "Reading: " + st, Toast.LENGTH_LONG).show();

             */
            //      InputStream stream = socket.getInputStream();
            // byte[] data = new byte[4];

            // int count = stream.read(data);
            //   Toast.makeText(MainActivity.this, "Num: " + count, Toast.LENGTH_LONG).show();
            //send input text to server
            EditText et = (EditText) findViewById(R.id.editText);
            String str = et.getText().toString();

            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            Toast.makeText(MainActivity.this, "Sent: " + str, Toast.LENGTH_LONG).show();
           Payload payload;
      //      byte[] data = createBytes(payload);
     //       gattCharacteristic.setValue(data);
            out.println(str);
            out.flush();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                Log.w("Debug", "anything here?");

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Log.w("Debug", "Welcome to TTT!");




        }
    }
    public class RcvThread implements Runnable { // recieve
        public void run() {
            while (true) {//connected
                try {
                    /*
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    byte[] imBytes = new byte[31000];
                    int numRead = 0;
                    while ((numRead = inputStream.read(imBytes)) >= 0) {
                        baos.write(imBytes,0,numRead);
                    }
                    byte[] imageInBytes = baos.toByteArray();

                    int k = imageInBytes.length;
                    Message msg = new Message();
                    Log.w("Debug", String.valueOf(msg));
                    msg.obj = k;
                   // mHandler.sendMessage(msg);


                    Log.w("Debug", "anything here?");
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String st = input.readLine();
                    Log.w("Debug", st);
                    Toast.makeText(MainActivity.this, "Reading: " + st, Toast.LENGTH_LONG).show();

                     */

                    try {
                        BufferedReader input  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String st = input.readLine();
                        Log.w("Debug", st);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    Log.e("SocketConnectionv02Activity", "C: ErrorRCVD", e);


                   }
                }
            }

    }
    class Payload {
        int time1;
        int time2;
        int time3;
        int time4;
        int time5;
        int time6;
        byte speed1;
        byte speed2;
        byte speed3;

    }
    void createBytes(Payload payload) {
        /*
        ByteByffer buffer = ByteBuffer.allocate(28).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(payload.time1);
        buffer.putInt(payload.time2);
        buffer.putInt(payload.time3);
        buffer.putInt(payload.time4);
        buffer.putInt(payload.time5);
        buffer.putInt(payload.time6);
        buffer.putByte(payload.speed1);
        buffer.putByte(payload.speed2);
        buffer.putByte(payload.speed3);
        buffer.putByte(0);
        return buffer.array();

         */
    }
}