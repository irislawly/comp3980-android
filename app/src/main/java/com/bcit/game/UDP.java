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


/**
 * UDP Code, sending bytes of datagrams, however UDP server unable to connect and keep track of the UID
 * of the player.
 */
public class UDP extends AppCompatActivity {
    private static final String LOG_TAG = "UDPDebug";
    private Button play, stop, record;
    private MediaRecorder myAudioRecorder;
    private String outputFile;
    private boolean mic;
    private boolean speakers;
    private InetAddress address;
    private  byte[] opponent_uid = {0,0,0,5};
    private  byte[] user_uid = {0,0,0,4};
    private long    current_order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp);

        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);
        record = (Button) findViewById(R.id.record);

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


    }

    /**
     * Problems: Wassn't able to connect to a UID using UDP,  therefore connection unable to be formed
     * @param v
     */
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

                try {
                    // Create a socket and start recording
                    audioRecorder.startRecording();

                    DatagramSocket socket = new DatagramSocket(PORT);

                    // ORDERING [4 BYTE] UID[4] VOICE[5000]
                    byte[] sending_packet = new byte[PACKET_SIZE];
                    byte[] buf = new byte[VOICE_BUF_SIZE];; //Size 5000B

                    while(mic) {
                        //capture audio
                        bytes_read = audioRecorder.read(buf, 0, VOICE_BUF_SIZE);

                        //put ordering into sending packet
                        //CONVERT long current order into byte format [undone]!!!

                        byte[] order = new byte[ORDERING_SIZE];


                        //add header of UI and ordering = 8 bytes
                        byte[] header = new byte[ORDERING_SIZE + UID_SIZE];
                        System.arraycopy(order, 0, header , 0, ORDERING_SIZE);
                        System.arraycopy(user_uid, 0, header, ORDERING_SIZE, UID_SIZE);

                        //put header and voice packet together = 5008
                        System.arraycopy(header, 0, sending_packet , 0, header.length);
                        System.arraycopy(buf, 0, sending_packet, header.length, VOICE_BUF_SIZE);

                        //now create a packet of the buffer to prepare
                        DatagramPacket packet = new DatagramPacket(sending_packet, PACKET_SIZE, address, PORT);
                        //send packet to server
                        socket.send(packet);

                        Log.i(LOG_TAG, "Audio bytes sent: " + bytes_read);
                        Thread.sleep(SAMPLE_INTERVAL, 0);

                    }
                    //incrment order after successfully sent out to server
                    current_order++;
                    // Stop recording and release resources
                    audioRecorder.stop();
                    audioRecorder.release();
                    socket.disconnect();
                    socket.close();
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

        // Creates the thread for receiving and playing back audio
        if(speakers) {

            speakers = true;
            Thread receiveThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    // Create an instance of AudioTrack, used for playing back audio
                    Log.i(LOG_TAG, "Receive thread started. Thread id: " + Thread.currentThread().getId());
                    AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, VOICE_BUF_SIZE, AudioTrack.MODE_STREAM);
                    track.play();

                    try {

                        // Define a socket to receive the audio
                        DatagramSocket socket = new DatagramSocket(PORT);
                        // ORDERING [4 BYTE] UID[4] VOICE[5000]
                        byte[] received_packet = new byte[PACKET_SIZE];
                        byte[] bufAudio;
                        byte[] ordering;
                        byte[] received_uid;


                        while(speakers) {

                            // Play back the audio part of the PACKET (5000 bytes, from index 7 to end)
                            DatagramPacket packet = new DatagramPacket(received_packet, PACKET_SIZE);
                            socket.receive(packet);

                            //Get the ordering of the recieving
                            ordering = Arrays.copyOfRange(received_packet, 0, ORDERING_SIZE-1);
                            //Get the uid of the receieving
                            received_uid =  Arrays.copyOfRange(received_packet, ORDERING_SIZE-1, UID_SIZE-1);

                            long received_ordering = ByteBuffer.wrap(ordering).getInt();


                            //Check that the UID retrieved is the other opponent's UID.

                            if(  Arrays.equals(received_uid ,opponent_uid)){
                                Log.i(LOG_TAG, "Wrong uid recieved!");
                            }

                            //if order of packets lower than the current ordering packet
                            if(received_ordering < current_order){
                                //disconnect
                                Log.i(LOG_TAG, "Wrong  order recieved!");
                            }
                            current_order++;

                            //filter the packet into audio packet
                            bufAudio =  Arrays.copyOfRange(received_packet, (ORDERING_SIZE+UID_SIZE-1), PACKET_SIZE);
                            Log.i(LOG_TAG, "Packet received: " + Arrays.toString(packet.getData()));
                            //start playing audio
                            track.write(bufAudio, SAMPLE_RATE, VOICE_BUF_SIZE);
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
    //code to test recording audio
    public void record(View v){
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
        } catch (IllegalStateException ise) {
            System.out.println(ise.getMessage());
        } catch (IOException ioe) {
            // make something
        }
        record.setEnabled(false);
        stop.setEnabled(true);
        Toast.makeText(getApplicationContext(), "Testing record", Toast.LENGTH_SHORT).show();
    }
    //code to stop testing audio
    public void stop(View v){
        if (myAudioRecorder != null) {
            try {
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;
            } catch (IllegalStateException ise) {
                System.out.println("Can't stop recording");
            }
        }
        record.setEnabled(true);
        stop.setEnabled(false);
        play.setEnabled(true);
        Toast.makeText(getApplicationContext(), "Stopped", Toast.LENGTH_SHORT).show();
    }
    //code to test replaying audio
    public void play(View v){
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(outputFile);
            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(getApplicationContext(), "Playback", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

        }
    }

    //Checking for permissions
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


}