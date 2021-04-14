package com.bcit.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TTT extends Activity {

    private Socket socket;

    private static final int SERVERPORT = 8888;
    private static final String SERVER_IP = "192.168.1.89";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        new Thread(new ClientThread()).start();
    }

    public void sendMessage(View view)
    {

        try {
            EditText et = (EditText) findViewById(R.id.editText);
            String str = et.getText().toString();

            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),
                    true);
            Toast.makeText(TTT.this, "Sent: " + str,
                    Toast.LENGTH_LONG).show();
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
/*
    public void clickb1(View view){
        Toast.makeText(MainActivity.this, "Pressed 1", Toast.LENGTH_SHORT).show();
    }
    public void clickb2(View view){
        Toast.makeText(MainActivity.this, "Pressed 2", Toast.LENGTH_SHORT).show();
    }
    public void clickb3(View view){
        Toast.makeText(MainActivity.this, "Pressed 3", Toast.LENGTH_SHORT).show();
    }
    public void clickb4(View view){
        Toast.makeText(MainActivity.this, "Pressed 4", Toast.LENGTH_SHORT).show();
    }
    public void clickb5(View view){
        Toast.makeText(MainActivity.this, "Pressed 5", Toast.LENGTH_SHORT).show();
    }

 */

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket("192.168.1.89", 8080);

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final String st = input.readLine();
                Toast.makeText(TTT.this, "Reading " + st,
                        Toast.LENGTH_LONG).show();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
}