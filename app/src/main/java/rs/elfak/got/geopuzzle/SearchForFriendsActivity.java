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
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import rs.elfak.got.geopuzzle.library.Cons;

public class SearchForFriendsActivity extends AppCompatActivity {

    public static final int SOCKET_CONNECTED = 1;
    public static final int DATA_RECEIVED = 2;

    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothDevice mDevice;
    private BluetoothDevice mSelectedDevice;
    private Set<BluetoothDevice> mPairedDevices;

    private InputStream mInStream;
    private OutputStream mOutStream;

    private AcceptThread mAcceptThread;
//    private ConnectThread mConnectThread;
//    private ConnectionThread mBluetoothConnection;

    public Handler mHandler;

    private ListView mListView;

    private int mPosition;

    private BluetoothDevice mRemoteDevice;
    private ArrayList<String> pairedDeviceStrings;
    private DataOutputStream os;
    private DataInputStream is;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_friends);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        mListView = (ListView) findViewById(R.id.userListView);

        // Enable adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // No Bluetooth support
            finish();
        }

        mHandler = new Handler();

        // Handler in DataTransferActivity
//        mHandler = new Handler() {
//            public void handleMessage(Message msg) {
//                switch (msg.what) {
//                    case SOCKET_CONNECTED: {
//                        mBluetoothConnection = (ConnectionThread) msg.obj;
//                        mBluetoothConnection.write("EMAIL SENT!!!".getBytes());
//                        break;
//                    }
//                    case DATA_RECEIVED: {
//                        String data = (String) msg.obj;
//                        Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
//                        //mBluetoothConnection.write(data.getBytes());
//                    }
//
//                }
//            }
//        };

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;

                Toast.makeText(getApplicationContext(), "mPosition set to: " + mPosition, Toast.LENGTH_LONG).show();
            }
        });

        BroadcastReceiver discoveryResult = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {


                String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                BluetoothDevice remoteDevice;

                remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Toast.makeText(getApplicationContext(), "Discovered: " + remoteDeviceName + " address " + remoteDevice.getAddress(), Toast.LENGTH_SHORT).show();

                try {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(remoteDevice.getAddress());

                    Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});

                    BluetoothSocket clientSocket = (BluetoothSocket) m.invoke(device, 2);

                    clientSocket.connect();

                    os = new DataOutputStream(clientSocket.getOutputStream());
                    is = new DataInputStream(clientSocket.getInputStream());


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("BLUETOOTH", e.getMessage());
                }
            }
        };

        registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        mAcceptThread = new AcceptThread();
        mAcceptThread.start();

    }

    public class ClientSock extends Thread {
        public void run () {
            try {
                os.writeBytes("anything you want"); // anything you want
                os.flush();

//                Intent intent = new Intent("email");
//                intent.putExtra("email", "test@elfak.com");
//                sendBroadcast(intent);


            } catch (Exception e1) {
                e1.printStackTrace();
                return;
            }
        }
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

        // Create bluetooth server socket to listen for connections
//        AcceptThread acceptThread = new AcceptThread(mHandler);
//        acceptThread.run();
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
            pairedDeviceStrings.add(bt.getAddress());
        Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedDeviceStrings);
        mListView.setAdapter(adapter);
    }

    public void discover(View v) {
        selectServer();
    }

    public void send(View v) {
        String address = pairedDeviceStrings.get(mPosition);
        mRemoteDevice =  mBluetoothAdapter.getRemoteDevice(address);

//        try {
//
//            Method m = mRemoteDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
//            BluetoothSocket clientSocket =  (BluetoothSocket) m.invoke(mRemoteDevice, 2);
//            clientSocket.connect();
//            os = new DataOutputStream(clientSocket.getOutputStream());
//        }
//        catch (Exception e) {
//            e.getMessage();
//        }

        UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // bluetooth serial port service
        //UUID SERIAL_UUID = device.getUuids()[0].getUuid(); //if you don't know the UUID of the bluetooth device service, you can get it like this from android cache

        BluetoothSocket socket = null;

        try {
            socket = mRemoteDevice.createRfcommSocketToServiceRecord(SERIAL_UUID);
        }
        catch (Exception e) {
            Log.e("","Error creating socket");
        }

        try {
            socket.connect();
            Log.e("","Connected");
        }
        catch (IOException e) {
            Log.e("",e.getMessage());
            try {
                Log.e("","trying fallback...");

                socket =(BluetoothSocket) mRemoteDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mRemoteDevice,2);
                socket.connect();

                Log.e("","Connected");
            }
            catch (Exception e2) {
                Log.e("", "Couldn't establish Bluetooth connection!");
            }
        }

        new ClientSock().start();

    }




    private void selectServer() {
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

    // Read data about local adapter
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
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("email", MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
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

                    //manageConnectedSocket(socket);
                    Toast.makeText(getApplicationContext(),"RECEIVED!!!!!!!!!",Toast.LENGTH_SHORT).show();

                    try {
                        mmServerSocket.close();
                    }
                    catch (Exception e) {
                        e.getMessage();
                    }


                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }


//    class AcceptThread extends Thread {
//        public AcceptThread(Handler handler) {
//
//            try {
//
//                mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("friendRequest", MY_UUID);
//            } catch (IOException e) {
//                e.getMessage();
//            }
//        }
//
//        public void run() {
//            while (true) {
//                try {
//                    mBluetoothSocket = mServerSocket.accept();
//                    manageConnectedSocket();
//                    mServerSocket.close();
//                    break;
//                } catch (IOException e1) {
//                    e1.getMessage();
//                }
//            }
//        }
//
//        public void manageConnectedSocket() {
//
//        }
//    }

//    public class ConnectThread extends Thread {
//        public ConnectThread(String deviceID, Handler handler) {
//            mDevice = mBluetoothAdapter.getRemoteDevice(deviceID);
//            try {
//                mBluetoothSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
//            } catch (IOException e) {
//                e.getMessage();
//            }
//        }
//
//        public void run() {
//            mBluetoothAdapter.cancelDiscovery();
//            try {
//                mBluetoothSocket.connect();
//                manageConnectedSocket();
//            } catch (IOException connectException) {
//                connectException.getMessage();
//            }
//        }
//
//        private void manageConnectedSocket() {
//            ConnectionThread conn = new ConnectionThread(mBluetoothSocket, mHandler);
//            mHandler.obtainMessage(SOCKET_CONNECTED, conn).sendToTarget();
//            conn.start();
//        }
//    }
//
//    public class ConnectionThread extends Thread {
//
//        ConnectionThread(BluetoothSocket socket, Handler handler) {
//            super();
//            mBluetoothSocket = socket;
//            mHandler = handler;
//            try {
//                mInStream = mBluetoothSocket.getInputStream();
//                mOutStream = mBluetoothSocket.getOutputStream();
//            } catch (IOException e) {
//
//            }
//        }
//
//        public void run() {
//            byte[] buffer = new byte[1024];
//            int bytes;
//            while (true) {
//                try {
//                    bytes = mInStream.read(buffer);
//                    String data = new String(buffer, 0, bytes);
//                    mHandler.obtainMessage(DATA_RECEIVED, data).sendToTarget();
//                } catch (IOException e) {
//                    break;
//                }
//            }
//        }
//        public void write(byte[] bytes) {
//            try {
//                mOutStream.write(bytes);
//            } catch (IOException e) {
//
//            }
//        }
//    }



}
