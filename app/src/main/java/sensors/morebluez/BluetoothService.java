package sensors.morebluez;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by cost on 7/22/16.
 */
public class BluetoothService {
    private final BluetoothAdapter mAdapter;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    public final int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothSecure";

    // Unique UUID for this application
    private static final UUID MY_UUID =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    public BluetoothService(Context context) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
//        Start thread to listen on a secure bluetooth socket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread();
            mSecureAcceptThread.start();
        }
    }


    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            //uses temporary object
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            //Listen until an error occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                //If a connection was accepted
                if (socket != null) {
                    //Do work to manage the connection in a separate thread
                    connected(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) { }
                    break;
                }
            }
        }

        /*Will cancel listening socket, causing the thread to finish*/
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;

            //Get a bluetooth socket
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
//            Cancel discovery to avoid slowing down the connection
            mAdapter.cancelDiscovery();

            try {
                //Connect the device through the socket
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

//            Do work to manage the connection in a separate thread
            connected(mmSocket); //TODO implement ConnectedThread and connect()
        }

        /*Cancel an in-progress connection and close the socket*/
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

    }

}