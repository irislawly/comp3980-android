package com.bcit.game;

import android.media.AudioFormat;

public @interface shared {

   static final int PORT = 2034;
   static String SERVER = "192.168.1.89" ;// Iris' ipp adress
  // static String SERVER = "23.16.22.78";
   //for clent request
   static final int  UID_1 =0;
    static final int  UID_2 =1;
    static final int  UID_3 =2;
    static final int UID_4 =3;
    static final int REQ_TYPE =4;
    static final int  REQ_CONTEXT =5;
    static final int  REQ_PAYLOAD_LEN =6;
    static final int REQ_PAYLOAD =7;

    // For server's response
    static final int MSG_TYPE=0;
    static final int CONTEXT=1;
    static final int PAYLOAD_LEN=2;
    static final int PAYLOAD=3;


/**
 * Request Message Types
 */
static final int CONFIRMATION=1;
    static final int INFORMATION=2;
    static final int META_ACTION=3;
    static final int GAME_ACTION=4;

/**
 * Context
 */
static final int CONFIRM_RULESET=1;           // MsgType::= UPDATE, Payload::= Team
    static final int MAKE_MOVE=1 ;                // MsgType::= UPDATE, Payload::= Position of move
    static final int QUIT_GAME=1;      // MsgType::= SUCCESS, Payload::= Player id

/**
 * Payload Type
 */

/** PROTOCOL VERSION */
static final int V1=1;

/** GAME ID */
static final int TIC_TAC_TOE=1;          // PayloadType::= PROTOCOL_VERSION
    static final int ROCK_PAPER_SCISSOR=2;   // PayloadType::= PROTOCOL_VERSION

// 2. SUCCESS CONFIRMATION -> assign players to game
/** TEAM ID */
static final int X=1;
    static final int O=2;

/** END OF GAME STATUS */
    static final int WIN=1;
    static final int LOSS=2;
    static final int TIE=3;

/** MOVE FOR RPS  */
static final int ROCK=1;
    static final int PAPER=2;
    static final int SCISSORS=3;

/** ========== SERVER RESPONSE ========== */
/**
 * Response Message Types/Status
 */
static final int SUCCESS=10;
    static final int UPDATE=20;

/** Client error */
    static final int INVALID_REQUEST=30;
    static final int INVALID_UID=31;
    static final int INVALID_TYPE=32;
    static final int INVALID_CONTEXT=33;
    static final int INVALID_PAYLOAD=34;


/** Server error */
static final int SERVER_ERROR=40;

/** Game error */
static final int INVALID_ACTION=50;
    static final int OUT_OF_TURN=51;

/**
 * UPDATE Context
 */
static final int START_GAME=1;           // MsgType::= UPDATE, Payload::= Team
    static final int MOVE_MADE=2 ;           // MsgType::= UPDATE, Payload::= Position of move
    static final int END_GAME=3 ;            // MsgType::= UPDATE, Payload::= End game status
    static final int OPPONENT_DISCONNECTED=4;// MsgType::= UPDATE, Payload::= Empty
  //static final int CONFIRMATION =1;       // MsgType::= SUCCESS, Payload::= Player id

/**
 * Payload Type
 */

/** PROTOCOL VERSION
static final int V1=1;

*/

//Protocol VOICE
public static final int     SAMPLE_RATE     = 10000;
    public static final int     CHANNELS        = 1;
    public static final int     BIT_DEPTH       = AudioFormat.ENCODING_PCM_16BIT;
    public static final double  TIME_FRAME      = 0.5;
    public static final int     BUFFER_FACTOR   = 10;
    public static final int     SAMPLE_SIZE     = 2;
    public static final int     SAMPLE_INTERVAL = 20;
    int UID_SIZE = 4;
    int ORDERING_SIZE = 4;
    int VOICE_BUF_SIZE = 5000;
    public static final int PACKET_SIZE = 5008;
        //voice buf size is PAYLOAD .

}
