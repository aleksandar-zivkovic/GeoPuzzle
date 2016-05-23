package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.PersistableBundle;
import android.renderscript.ScriptGroup;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;

public class SearchForFriendsActivity extends AppCompatActivity {

    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mSelectedDevice;
    private Set<BluetoothDevice> mPairedDevices;
    // check this
    private BluetoothDevice mRemoteDevice;
    private ArrayList<String> pairedDeviceStrings;

    private ConnectThread mConnectThread;
    private AcceptThread mAcceptThread;
    private ConnectionThread mConnectionThread;

    public Handler mHandler;

    private ListView mListView;
    private ListView mListViewPendingFriendRequests;

    private boolean mPositionSet;
    private int mPosition;
    private int mPositionRemoteEmail;
    private ArrayList<String> mPendingFriendRequestList;
    private HashMap mUser;
    private String mEmail;
    private String mRemoteEmail;
    private DatabaseHandler db;
    private BroadcastReceiver discoveryResult;

    private AlertDialog mDialog;
    private final boolean[] mAnswer = new boolean[1];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_friends);

        mPendingFriendRequestList = new ArrayList<String>();

        // boolean to ensure that position is always set before starting bluetooth connection
        mPositionSet = false;

        mDialog = new AlertDialog.Builder(this).create();

        // get my email to send it as a friend request
        db = new DatabaseHandler(getApplicationContext());
        mUser = db.getUserDetails();
        mEmail = (String)mUser.get(Cons.KEY_EMAIL);

        mListView = (ListView) findViewById(R.id.userListView);
        mListViewPendingFriendRequests = (ListView) findViewById(R.id.listViewPendingFriendRequests);

        // Enable adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // No Bluetooth support
            finish();
        }

        // turn on Bluetooth on startup
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, Cons.REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(),"Bluetooth already turned on", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Cons.SOCKET_CONNECTED: {
                        //mConnectionThread = (ConnectionThread) msg.obj;
                        //mConnectionThread.write("EMAIL SENT!!!".getBytes());
                        break;
                    }
                    case Cons.DATA_RECEIVED: {
                        String data = (String) msg.obj;

                        //TODO:
                        // Check if friend request already pending
                        if (mPendingFriendRequestList.contains(data.toString())) {
                            break;
                        }
                        // Check if you are already friend with the sender
                        // my email = mEmail, remote device email = data



                        mPendingFriendRequestList.add(data);
                        // add it to pending friend request list
                        final ArrayAdapter adapter = new ArrayAdapter(SearchForFriendsActivity.this , android.R.layout.simple_list_item_1, mPendingFriendRequestList);
                        mListViewPendingFriendRequests.setAdapter(adapter);

                        break;
                    }
                }
            }
        };

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                mPositionSet = true;
                Toast.makeText(getApplicationContext(), "mPosition set to: " + mPosition, Toast.LENGTH_LONG).show();
            }
        });

        mListViewPendingFriendRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPositionRemoteEmail = position;
                String selectedEmail = mPendingFriendRequestList.get(mPositionRemoteEmail);
                Toast.makeText(getApplicationContext(), "Selected friend request from: " + selectedEmail, Toast.LENGTH_LONG).show();

                // Open a dialog which prompts you to "Accept" or "Decline" friend request
                mDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Accept", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        mAnswer[0] = true;
                    }
                });

                mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Decline", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        mAnswer[0] = false;
                    }
                });

                mDialog.show();

                //BlockingConfirmDialog mBlockingConformDialog = new BlockingConfirmDialog(SearchForFriendsActivity.this);
                //mAnswer[0] = mBlockingConformDialog.confirm("Friend Request", "Accept or decline friend request from " + selectedEmail);

                if (mAnswer[0] == true) {
                    Toast.makeText(getApplicationContext(), "You have accepted friend request from " + selectedEmail, Toast.LENGTH_LONG).show();

                    //TODO:
                    // Send POSITIVE response back to the device which sent you friend request
                }
                else {
                    Toast.makeText(getApplicationContext(), "You have declined friend request from " + selectedEmail, Toast.LENGTH_LONG).show();

                    //TODO:
                    // Send NEGATIVE response back to the device which sent you friend request

                }








            }
        });

        discoveryResult = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                BluetoothDevice remoteDevice;
                remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(getApplicationContext(), "Discovered: " + remoteDeviceName + " address " + remoteDevice.getAddress(), Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        // Start listening
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    public void on(View v){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, Cons.REQUEST_ENABLE_BT);
            Toast.makeText(getApplicationContext(),"Turned on",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getApplicationContext(),"Already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v){
        mBluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(),"Turned off" ,Toast.LENGTH_LONG).show();
    }

    public  void visible(View v){
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible, 0);
    }

    public void list(View v){
        mPairedDevices = mBluetoothAdapter.getBondedDevices();
        pairedDeviceStrings = new ArrayList();

        for(BluetoothDevice bt : mPairedDevices)
            pairedDeviceStrings.add(bt.getName() + "\n" + bt.getAddress());
        Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedDeviceStrings);
        mListView.setAdapter(adapter);
    }

    public void discover(View v) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<String> pairedDeviceStrings = new ArrayList<String>();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceStrings.add(device.getAddress());
            }
        }
        Intent showDevicesIntent = new Intent(this, ShowDevices.class);
        showDevicesIntent.putStringArrayListExtra("devices", pairedDeviceStrings);
        startActivityForResult(showDevicesIntent, Cons.SELECT_SERVER);
    }

    public void send(View v) {
        if (!mPositionSet) {
            Toast.makeText(this, "You have to choose a device!", Toast.LENGTH_LONG).show();
            return;
        }

        // get just the address, not the name
        String nameAndAddress = pairedDeviceStrings.get(mPosition);
        String[] split = nameAndAddress.split("\n");
        String address = split[1];
        mRemoteDevice =  mBluetoothAdapter.getRemoteDevice(address);

        mConnectThread = new ConnectThread(mRemoteDevice);
        mConnectThread.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket, because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e) {
                e.getMessage();
            }
            mmSocket = tmp;

        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block until it succeeds or throws an exception
                mmSocket.connect();
                mConnectionThread = new ConnectionThread(mmSocket);
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                }
                catch (IOException closeException) {
                    closeException.getMessage();
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            //manageConnectedSocket(mmSocket);
            //mConnectionThread.start();
            String stringToSend = mEmail;
            mConnectionThread.write(stringToSend.getBytes());
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Cons.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
            String address = BT.getAddress();
            String name = BT.getName();
            String toastText = name + " : " + address;
            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
        }
        else if (requestCode == Cons.SELECT_SERVER && resultCode == Activity.RESULT_OK) {

            mSelectedDevice = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // pair with mSelectedDevice
            pairDevice(mSelectedDevice);
        }
    }

    //For Pairing
    private void pairDevice(BluetoothDevice device) {
        try {
            Log.d("pairDevice()", "Start Pairing...");
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("pairDevice()", "Pairing finished.");
        } catch (Exception e) {
            Log.e("pairDevice()", e.getMessage());
        }
    }


    //For UnPairing
    private void unpairDevice(BluetoothDevice device) {
        try {
            Log.d("unpairDevice()", "Start Un-Pairing...");
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            Log.d("unpairDevice()", "Un-Pairing finished.");
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket, because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("friendRequest", MY_UUID);
            }
            catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    try {
                        bytes = socket.getInputStream().read(buffer);
                        String data = new String(buffer, 0, bytes);

                        // this message cannot be handled here, because this is worker thread
                        // so we use this handler to send it to send it as message to main thread
                        mHandler.obtainMessage(Cons.DATA_RECEIVED, data).sendToTarget();
                        mmServerSocket.close();
                    }
                    catch (Exception e) {
                        e.getMessage();
                    }
                    break;
                }
            }
        }
    }

    private class ConnectionThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectionThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(discoveryResult);
    }

//    public static class BlockingConfirmDialog {
//
//        private Activity context;
//
//        BlockingQueue<Boolean> blockingQueue;
//
//        public BlockingConfirmDialog(Activity activity) {
//            super();
//            this.context = activity;
//            blockingQueue = new ArrayBlockingQueue<Boolean>(1);
//        }
//
//        public boolean confirm(final String title, final String message){
//
//            context.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    new AlertDialog.Builder(context)
//                            .setTitle(title)
//                            .setMessage(message)
//                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    blockingQueue.add(true);
//                                }
//                            })
//                            .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    blockingQueue.add(false);
//                                }
//                            })
//                            .show();
//                }
//            });
//
//            try {
//                return blockingQueue.take();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//    }

}
