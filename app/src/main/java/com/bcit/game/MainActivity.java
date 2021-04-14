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
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.bcit.game.shared.*;

public class MainActivity extends Activity implements View.OnClickListener  {



    private Socket socket;

    private static final int SERVER_PORT = 8080;
    private static final String SERVER_IP = "192.168.1.89";
    int count = 0, game_id, bytes_sent;
    boolean connected = false, close_conn = false;
    byte[] uid = new byte[4];
    char playBoard[] = new char[9];
    char this_player, other_player;
    byte confirm_req[] = new byte[9];
    byte req[] = new byte[8];
    byte choice[] = new byte[2];
    byte[] res = new byte[8];
    //private Button button = (Button) findViewById(R.id.btn_send);

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < 9; i++)
            playBoard[i] = '_';
     //   new Thread(new ClientThread()).start();

        Button button_rock = findViewById(R.id.button_rock);
        Button button_paper = findViewById(R.id.button_paper);
        Button button_scissor = findViewById(R.id.button_scissor);

        button_rock.setOnClickListener( this);
        button_paper.setOnClickListener( this);
        button_scissor.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int choice = 0;
        switch (v.getId()){

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

        Toast.makeText(MainActivity.this, "Picked: " + choice, Toast.LENGTH_SHORT).show();
    }



    void update_board(int cell, char board[], char player) {
        Log.w("Debug", "making board");
        if (board[cell] == ' ') {
            board[cell] = player;
            String r1 = "", r2 = "", r3 = "";
            r1 += board[0] + " " + board[1] + " " + board[2];
            r2 += board[3] + " " + board[4] + " " + board[5];
            r3 += board[6] + " " + board[7] + " " + board[8];
            Toast.makeText(MainActivity.this, "Sent: " + r1+ "\n" + r2+ "\n" + r3, Toast.LENGTH_LONG).show();
      //      Log.w("Debug", r1);
      //      Log.w("Debug", r2);
        //    Log.w("Debug", r3);

        }
    }

    /*
    public int getValue() {
        Button button

        boolean done = false;
        while (!done) {
            if(button.isSelected()){
                done = true;
            }

        }
        EditText et = (EditText) findViewById(R.id.edt_send_message);
        String str = et.getText().toString();
        return Integer.parseInt(str);
    }

*/


    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
               // Log.w("Debug", "anything here?");

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Log.w("Debug", "Welcome to TTT!");
          //  while (!close_conn) {
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

                    //  byte TIC_TAC_TOE = 1;
                    confirm_req[REQ_TYPE] = 1;
                    confirm_req[REQ_CONTEXT] = 1;
                    confirm_req[REQ_PAYLOAD_LEN] = 2;
                    confirm_req[REQ_PAYLOAD] = 1;   // Version number
                 //   byte b = (byte) getValue();
                 //   Toast.makeText(MainActivity.this, "Sent: " + b, Toast.LENGTH_LONG).show();
       //                Log.w("Debug", "Gameid: " + String.valueOf(b));
                    confirm_req[REQ_PAYLOAD + 1] = 1;//(byte) getValue();
                    game_id = TIC_TAC_TOE;
                    for (int i = 0; i < confirm_req.length; i++) {
                        //   Log.w("Debug", String.valueOf(confirm_req[i]));
                    }

                    OutputStream socketOutputStream = null;
                    try {
                        socketOutputStream = socket.getOutputStream();
                        socketOutputStream.write(confirm_req);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //try to recieve the RES uid shiet
                    try {
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
                   //     Log.w("Debug", "Ur res: " + rez);
                   //     Log.w("Debug", "Ur uid: " + uID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    connected = true;

                } else {
                    //try to recieve moes i guess
                    choice[0] = 0;
                    choice[1] = 0;
                    try {
                        InputStream stream = socket.getInputStream();

                        stream.read(res);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.w("Debug", "MsgType? " + String.valueOf(res[0]));
                    switch (res[MSG_TYPE]) {

                        case SUCCESS:
                            if (CONFIRMATION == 1) {
                                for (int i = 3; i < 7; i++) {
                                    uid[i - 3] = res[i];
                                }
                            }
                            break;
                        case UPDATE:
                            Log.w("Debug", "Context? " + String.valueOf(res[CONTEXT]));

                            switch (res[CONTEXT]) {
                                case START_GAME:
                                    Log.w("Debug", "GameID: " + game_id);
                                    if (game_id == TIC_TAC_TOE) {
                                        for (int i = 0; i < 4; i++)
                                            req[i] = uid[i];
                                        req[REQ_TYPE] = GAME_ACTION;        // Game action
                                        req[REQ_CONTEXT] = MAKE_MOVE;       // Make a move
                                        req[REQ_PAYLOAD_LEN] = 1;

                                        Log.w("Debug", "Your turn, place your move: ");
                                        //cin >> choice;
                                        choice[0] = 1;
                                        Log.w("Debug", "Choice:" + String.valueOf(choice[0]));
                                        // cout << "sent " << choice[0] << " to server!" << endl;

                                      //  choice[0] -= '0';
                                        req[REQ_PAYLOAD] = choice[0];
                                        Log.w("Debug", "Payload: " + String.valueOf(req[REQ_PAYLOAD]));
                                        Toast.makeText(MainActivity.this, "Updatboard!", Toast.LENGTH_LONG).show();
                                        update_board(req[REQ_PAYLOAD], playBoard, (count % 2 == 0 ? 'X' : 'O'));
                                        OutputStream socketOutputStream = null;
                                        try {
                                            socketOutputStream = socket.getOutputStream();
                                            socketOutputStream.write(confirm_req);
                                        } catch (IOException e) {
                                            e.printStackTrace();

                                        }
                                        // bytes_sent = send(sockfd, req, sizeof(req), 0);
                                        Log.w("Debug", "Updateboard: " + req[REQ_PAYLOAD]);


                                        if (bytes_sent == -1) {

                                            return;
                                        }
                                        count++; // TO KEEP TRACK OF 'X' & 'O'
                                        this_player = 'X';
                                        other_player = 'O';
                                    }
                                    if (res[PAYLOAD] == O) {
                                        System.out.println("Please wait for your turn\n");
                                        count--;
                                        this_player = 'O';
                                        other_player = 'X';
                                    }


                                default:
                                    Log.w("Debug", "Context? " + "default");
                                    break;
                            }

                    }


                }
          //  }


        }


    }
}