
package edu.buffalo.cse.cse486586.groupmessenger;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();

    private ContentResolver mContentResolver;

    static final int SERVER_PORT = 10000;
    
    ServerSocket serverSocket;

    private HashMap<String, String> hashMap;

    private HashMap<String, Integer> backupHashMap;

    TextView tv;

    OnSendClickListener onClick;

    private final Uri uri = Uri
            .parse("content://edu.buffalo.cse.cse486586.groupmessenger.provider");

    private String myPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        TelephonyManager tel = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        /*
         * TODO: Use the TextView to display your messages. Though there is no
         * grading component on how you display the messages, if you implement
         * it, it'll make your debugging easier.
         */
        tv = (TextView)findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is
         * the "PTest" button. OnPTestClickListener demonstrates how to access a
         * ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        hashMap = new HashMap<String, String>();
        backupHashMap = new HashMap<String, Integer>();

        /*
         * TODO: You need to register and implement an OnClickListener for the
         * "Send" button. In your implementation you need to get the message
         * from the input box (EditText) and send it to other AVDs in a
         * total-causal order.
         */

        /*
         * To Clear the Screen and Send the Message on Click.
         */
        EditText editText = (EditText)findViewById(R.id.editText1);
        onClick = new OnSendClickListener(editText, myPort);
        findViewById(R.id.button4).setOnClickListener(onClick);
        editText.setText("");
        mContentResolver = getContentResolver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);

        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            String msgReceived;
            serverSocket = sockets[0];

            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            clientSocket.getInputStream()));

                    if ((msgReceived = br.readLine()) != null) {
                        String[] messages = msgReceived.split("\\$");

                        publishProgress(messages);
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... strings) {
            /*
             * Send message to all avds or to Sequencer depending on presence of
             * Sequence number.
             */

            String message = strings[0].trim();
            String unique_id = strings[1];
            String port = strings[2];

            if (strings.length == 3) {
                if (backupHashMap.containsKey(unique_id)) {
                    ContentValues keyValueToInsert = new ContentValues();
                    int bSeq = backupHashMap.get(unique_id);
                    tv.append("" + bSeq + ": " + message + "\t\n");
                    keyValueToInsert.put("key", bSeq);
                    keyValueToInsert.put("value", message);
                    mContentResolver.insert(uri, keyValueToInsert);
                    return;
                }
                hashMap.put(unique_id, message);
            }

            if (Integer.parseInt(myPort) == 11108) {
                if (strings.length == 3) {
                    int nextSeq = onClick.sequence;
                    message = "sequencer" + "$" + unique_id + "$" + port + "$" + nextSeq;
                    onClick.sequence++;
                    new OnSendClickListener().new ClientTask().executeOnExecutor(
                            AsyncTask.SERIAL_EXECUTOR, message, myPort);
                }
            }

            if (strings.length == 4) {
                String messageToPut = hashMap.get(unique_id);
                if (messageToPut == null) {
                    backupHashMap.put(unique_id, Integer.parseInt(strings[3]));
                    return;
                }
                ContentValues keyValueToInsert = new ContentValues();
                tv.append(strings[3] + ": " + messageToPut + "\t\n");
                keyValueToInsert.put("key", strings[3]);
                keyValueToInsert.put("value", messageToPut);
                mContentResolver.insert(uri, keyValueToInsert);
            }

            return;
        }
    }

    @Override
    public void onDestroy() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.onDestroy();

    }

}
