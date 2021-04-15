package com.bcit.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bcit.game.R;

import static com.bcit.game.shared.*;

public class MainActivity extends Activity implements View.OnClickListener {

    private Socket socket;

    private static final int SERVER_PORT = 8080;
    private static final String SERVER_IP = "23.16.22.78";
    int count = 0, game_id, bytes_sent;
    boolean connected = false, close_conn = false;
    byte[] uid = new byte[4];
    char playBoard[] = new char[9];
    char this_player, other_player;
    byte confirm_req[] = new byte[9];
    byte req[] = new byte[8];
    byte choice[] = new byte[2];
    byte[] res = new byte[8];
    boolean accepted = false;
    //private Button button = (Button) findViewById(R.id.btn_send);
    Thread clientThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button_rock = findViewById(R.id.button_rock);
        Button button_paper = findViewById(R.id.button_paper);
        Button button_scissor = findViewById(R.id.button_scissor);

        button_rock.setOnClickListener(this);
        button_paper.setOnClickListener(this);
        button_scissor.setOnClickListener(this);

    }


    public void onClick(View v) {
        int choice = 0;
        switch (v.getId()) {

            case R.id.button_rock:
                choice = ROCK;
                break;

            case R.id.button_paper:
                choice = PAPER;
                break;

            case R.id.button_scissor:
                choice = SCISSORS;
                break;
        }
        clientThread = new Thread(new ClientThread(choice));
        clientThread.start();
        Toast.makeText(MainActivity.this, "Picked: " + choice, Toast.LENGTH_SHORT).show();
    }


    class ClientThread implements Runnable {
        int val;
        public ClientThread(int choice) {
            val = choice;
        }
        final Handler handler = new Handler();
        @Override
        public void run() {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            for (int i = 0; i < res.length; i++) {
                res[i] = 0;
            }

            while (true) {
                for (int i = 0; i < req.length; i++) {
                    req[i] = 0;
                }
                for (int i = 0; i < res.length; i++) {
                    res[i] = 0;
                }

                if (!connected) {
                    for (int i = 0; i < confirm_req.length; i++) {
                        confirm_req[i] = 0;
                    }

                    confirm_req[REQ_TYPE] = CONFIRMATION;
                    confirm_req[REQ_CONTEXT] = CONFIRM_RULESET;
                    confirm_req[REQ_PAYLOAD_LEN] = 2;
                    confirm_req[REQ_PAYLOAD] = 1;   // Version number
                    confirm_req[REQ_PAYLOAD + 1] = ROCK_PAPER_SCISSOR;
                    game_id = ROCK_PAPER_SCISSOR;
                    String c_req  ="";
                    for (int i = 0; i < confirm_req.length; i++) {
                        c_req += String.valueOf(confirm_req[i]);
                    }
                    Log.w("Debug", "Creq: " +c_req);
                    OutputStream socketOutputStream = null;
                    try {
                        socketOutputStream = socket.getOutputStream();
                        socketOutputStream.write(confirm_req);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //try to recieve the RES uid shiet
              /*      try {
                        InputStream stream = socket.getInputStream();

                        stream.read(res);
                        String rez = "";
                        String uID = "";
                        for (int i = 0; i < res.length; i++) {
                            rez += String.valueOf(res[i]);
                        }
                        for (int i = 3; i < 7; i++) {
                            uID += String.valueOf(res[i]);
                        }
                        Log.w("Debug", "Ur res: " + rez);
                        Log.w("Debug", "Ur uid: " + uID);*
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/


                    connected = true;

                } else {
                    Log.w("Debug", "Else!");
                    try {
                        InputStream stream = socket.getInputStream();
                        stream.read(res);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String rez = "";

                    for (int i = 0; i < res.length; i++) {
                        rez += String.valueOf(res[i]);
                    }

                    Log.w("Debug", "Else res: " + rez);
                    choice[0] = 0;
                    choice[1] = 0;

                    switch(res[MSG_TYPE]){
                        case SUCCESS:
                            Log.w("Debug", "Sucess!");
                            if(res[CONTEXT] == CONFIRMATION){ //CONFIRMATION
                                if(!accepted){
                                    for(int i = 3; i< 7; i++) {
                                        uid[i - 3] = res[i];
                                        Log.w("Debug", "Uid? " + uid[i-3]);
                                    }
                                    accepted = true;
                                }else{
                                    Log.w("Debug", "SUCESSMOVE!");
                                    break;
                                }
                            }
                            break;
                        case UPDATE:
                            switch(res[CONTEXT]){
                                case START_GAME:
                                    Log.w("Debug", "Start game!");
                                    //RPS
                                    for (int i = 0; i < 4; i++)
                                        req[i] = uid[i];
                                    req[REQ_TYPE] = GAME_ACTION;          // Game action
                                    req[REQ_CONTEXT] = MAKE_MOVE;       // Make a move
                                    req[REQ_PAYLOAD_LEN] = 1;

                                    choice[0] = (byte) val;
                                    Log.w("Debug", "Val:" + val);
                                //    choice[0] -= '0';
                                    req[REQ_PAYLOAD] = choice[0];
                                    //send to server
                                    String rezz = "";

                                    for (int i = 0; i < req.length; i++) {
                                        rezz += String.valueOf(req[i]);
                                    }

                                    Log.w("Debug", "Ur req: " + rezz);
                                    OutputStream socketOutputStream = null;
                                    try {
                                        socketOutputStream = socket.getOutputStream();
                                        socketOutputStream.write(req);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case END_GAME:
                                    Log.w("Debug", "Endgame: ");
                                    Log.w("Debug", "End res: " + rez);
                                    switch (res[PAYLOAD]) {
                                        case WIN:
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {

                                                    TextView t = findViewById(R.id.textView3);
                                                    t.setText("You win!");
                                                }
                                            });
                                            break;
                                        case LOSS:
                                            count++;
                                            choice[0] = res[PAYLOAD + 1];
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    TextView t = findViewById(R.id.textView3);
                                                    t.setText("You lose!");

                                                }
                                            });
                                            break;
                                        case TIE:
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    TextView t = findViewById(R.id.textView3);
                                                    t.setText("You tie!");
                                                }
                                            });
                                            break;
                                        case OPPONENT_DISCONNECTED:
                                            clientThread.stop();
                                            break;
                                        default:
                                            break;
                                    }
                            }
                    }

                }

            }

        }

    }
}