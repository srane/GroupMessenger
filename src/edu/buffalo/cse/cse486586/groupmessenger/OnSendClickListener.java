
package edu.buffalo.cse.cse486586.groupmessenger;

import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class OnSendClickListener implements OnClickListener {
    EditText editText;

    static final int SERVER_PORT = 10000;

    static final String[] REMOTE_PORTS = {
        "11108", "11112", "11116", "11120", "11124"
    };

    String message;

    String myPort;

    int sequence = 0;

    int uC = 0;

    public OnSendClickListener() {

    }

    public OnSendClickListener(EditText editText, String myPort) {
        this.editText = editText;
        this.myPort = myPort;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        message = editText.getText().toString();
        editText.setText("");
        String unique_id = "" + myPort + "" + uC;
        uC++;
        message = message + "$" + unique_id + "$" + myPort;
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message, myPort);
    }

    class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub

            String message = params[0];
            for (String s : REMOTE_PORTS) {
                String remotePort = s;
                try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[] {
                            10, 0, 2, 2
                    }), Integer.parseInt(remotePort));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    String [] colr = {"key","value"};
                    MatrixCursor mCursor = new MatrixCursor(colr);
                    out.print(mCursor);
                    out.print(message);
                    out.flush();
                    socket.close();
                } catch (NumberFormatException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            return null;
        }

    }

}
