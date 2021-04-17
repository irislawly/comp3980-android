package com.bcit.game;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.media.AudioManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import static com.bcit.game.shared.*;

public class Client extends AppCompatActivity {
    private static final String LOG_TAG = "UDPDebug";
    private Button play, stop, record, speakUDP, listenUDP;
    private MediaRecorder myAudioRecorder;
    private String outputFile;
    private boolean mic;
    private boolean speakers;
    private InetAddress address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);
        record = (Button) findViewById(R.id.record);
        speakUDP = (Button) findViewById(R.id.talk);
        listenUDP = (Button) findViewById(R.id.listen);

        stop.setEnabled(false);
        play.setEnabled(false);

        try {
            address = InetAddress.getByName(SERVER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        outputFile = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath() + "/recording.3gp";
        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);

        checkWritePermission();
        checkReadPermission();
        checkRecordingPermission();

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    myAudioRecorder.prepare();
                    myAudioRecorder.start();
                } catch (IllegalStateException ise) {
                    System.out.println(ise.getMessage());
                } catch (IOException ioe) {
                    // make something
                }

                Toast.makeText(getApplicationContext(), "Start record", Toast.LENGTH_SHORT).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (myAudioRecorder != null) {
                    try {
                        myAudioRecorder.stop();
                        myAudioRecorder.release();
                        myAudioRecorder = null;
                    } catch (IllegalStateException ise) {
                        System.out.println("Can't stop recording");
                    }
                }

                Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(outputFile);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Toast.makeText(getApplicationContext(), "Playback", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // make something
                }
            }
        });
    }


    public void speak(){
        Toast.makeText(getApplicationContext(), "Speak!", Toast.LENGTH_SHORT).show();
    }




    private void checkWritePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    123);
        }
    }

    private void checkReadPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);
        }
    }

    private void checkRecordingPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    123);
        }
    }

    public void startMic(View v) {



        // Creates the thread for capturing and transmitting audio
        mic = true;
        Thread thread = new Thread(new Runnable() {



            @Override
            public void run( ) {

//              ONE CHANNEL,
                Log.i(LOG_TAG, "Send thread started. Thread id: " + Thread.currentThread().getId());
                AudioRecord audioRecorder = new AudioRecord (MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)*10);

                int bytes_read = 0;
                int bytes_sent = 0;

                // UDP packet size
                ByteBuffer buffer = ByteBuffer.allocateDirect(PACKET_SIZE);
                // The array holds the voice message
                byte[] voice_buf;

                // <ordering>
                long current_ordering = 0;


                try {

                    try {
                        //      Get IP:
                   //     EditText ipTextField = (EditText)findViewById(R.id.server_ip);
                   //     c_server_address = InetAddress.getByName(ipTextField.getText().toString());

                        //      Get PORT:
                   //     EditText portTextField = (EditText)findViewById(R.id.server_port);
                   //     port = Integer.parseInt(portTextField.getText().toString());
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    // Create a socket and start recording
                    DatagramSocket UdpSocket = new DatagramSocket();
                    audioRecorder.startRecording();

                    // currently recording, enter the send packet loop
                    while(mic) {

//                      Clear previous buffer
                        buffer.clear();

                        voice_buf = new byte[BUF_SIZE];
                        bytes_read = audioRecorder.read(voice_buf, 0, BUF_SIZE);
                        Log.i(LOG_TAG, "bytes_read: " + bytes_read);

                        /**             32 32    64 -> (int) <some long number>
                         *              16 16      32 -> (short) <some int number></some>
                         * { ordering : uint32, (long, 0 ~ 4294967295)
                         *   uid      : uint32 (long, 0 ~ 4294967295)
                         *   port     : uint16 (int, 0 ~ 65535)
                         * */

                        ///???? UID is 32...where to get?
                        buffer.putInt((int)current_ordering++).putInt((int)0005);
//                                .putShort((short)port);
                        buffer.put(voice_buf);
                        buffer.rewind();
//                                                        0 ~  end of buffer
                        byte[] struct_to_send = new byte[buffer.remaining()];

                        buffer.get(struct_to_send);

                        System.out.println("Msg sent:");
                        System.out.println(Arrays.toString(struct_to_send));
                        System.out.println(Arrays.toString(voice_buf));

//                      THE SECOND PARAM HAS TO BE THE SAME LENGTH AS THE payload
                        DatagramPacket packet = new DatagramPacket(struct_to_send,  struct_to_send.length, address, 8080);
                        UdpSocket.send(packet);


                        Log.i(LOG_TAG, "Packet destination: " + address.toString());
                        Thread.sleep(SAMPLE_INTERVAL, 0);

                    }

                    // Stop recording and release resources
                    audioRecorder.stop();
                    audioRecorder.release();

                    UdpSocket.disconnect();
                    UdpSocket.close();

                    mic = false;
                    return;
                }
                catch(InterruptedException e) {

                    Log.e(LOG_TAG, "InterruptedException: " + e.toString());
                    mic = false;
                }
                catch(SocketException e) {

                    Log.e(LOG_TAG, "SocketException: " + e.toString());
                    mic = false;
                }
                catch(UnknownHostException e) {

                    Log.e(LOG_TAG, "UnknownHostException: " + e.toString());
                    mic = false;
                }
                catch(IOException e) {

                    Log.e(LOG_TAG, "IOException: " + e.toString());
                    mic = false;
                }
            }
        });
        thread.start();
    }

    public void startSpeakers(View v) {
        long ordering;

        // Creates the thread for receiving and playing back audio
        if(speakers) {

            speakers = true;
            Thread receiveThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // Create an instance of AudioTrack, used for playing back audio
                    Log.i(LOG_TAG, "Receive thread started. Thread id: " + Thread.currentThread().getId());
                    AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
                    track.play();

                    try {

                        // Define a socket to receive the audio
                        DatagramSocket socket = new DatagramSocket(8080);

                        byte[] received_struct = new byte[PACKET_SIZE] ;
                        byte[] buf;

                        long received_ordering ;
//                        if(received_ordering < ordering){
//
//                        }
//                        else{
//
//                        }
//                        ordering++;


                        while(speakers) {

                            // Play back the audio received from packets
                            DatagramPacket packet = new DatagramPacket(received_struct, PACKET_SIZE);
                            socket.receive(packet);

                            buf =  Arrays.copyOfRange(received_struct, 10, PACKET_SIZE);

//                            Log.i(LOG_TAG, "Packet received: " + packet.getLength());
//                            track.write(packet.getData(), 0, BUF_SIZE);
                            Log.i(LOG_TAG, "Packet received: " + Arrays.toString(packet.getData()));
                            track.write(buf, 0,BUF_SIZE);
                        }
                        // Stop playing back and release resources
                        socket.disconnect();
                        socket.close();
                        track.stop();
                        track.flush();
                        track.release();
                        speakers = false;
                        return;
                    }
                    catch(SocketException e) {

                        Log.e(LOG_TAG, "SocketException: " + e.toString());
                        speakers = false;
                    }
                    catch(IOException e) {

                        Log.e(LOG_TAG, "IOException: " + e.toString());
                        speakers = false;
                    }
                }
            });
            receiveThread.start();
        }
    }


}