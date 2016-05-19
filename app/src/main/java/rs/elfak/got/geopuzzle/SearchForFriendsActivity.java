package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import rs.elfak.got.geopuzzle.library.Cons;

public class SearchForFriendsActivity extends AppCompatActivity {

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final UUID APP_UUID =
            UUID.fromString("00000000-0000-1000-8000-00805F9B34FB");

    private UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final UUID MY_UUID_SERVER = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mPairedDevices;

    private ListView mListView;
    private int mPosition;

    private OutputStream outputStream, mOutStream;
    private InputStream inStream, mInStream;

    BluetoothSocket mServerSocket, mBluetoothSocket;
    BluetoothDevice mDevice;

    Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_friends);

        mListView = (ListView)findViewById(R.id.userListView);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;

                try {
                    init();
                    write("email");
                    run();
                }
                catch (Exception e){
                    e.getMessage();
                }
            }
        });
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
        ArrayList<String> pairedDeviceStrings = new ArrayList();

        for(BluetoothDevice bt : mPairedDevices)
            pairedDeviceStrings.add(bt.getName() + " " + bt.getAddress());
        Toast.makeText(getApplicationContext(),"Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedDeviceStrings);
        mListView.setAdapter(adapter);

//        Intent showDevicesIntent = new Intent(this, ShowDevices.class);
//        showDevicesIntent.putStringArrayListExtra("devices", pairedDeviceStrings);
//        startActivityForResult(showDevicesIntent, Cons.SELECT_SERVER);

    }

    private void init() throws IOException {

        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();

                if (bondedDevices.size() > 0) {
                    Object[] devices = (Object[]) bondedDevices.toArray();
                    BluetoothDevice device = (BluetoothDevice) devices[mPosition];
                    ParcelUuid[] uuids = device.getUuids();

                    for (ParcelUuid ep : uuids) {
                        //Utilities.print("UUID records : "+ ep.toString());
                        Toast.makeText(getApplicationContext(),"UUID records : "+ ep.toString(),Toast.LENGTH_LONG).show();
                    }

                    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuids[7].getUuid());

                    //BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);

                    try {
                        socket.connect();
                        Log.e("", "Connected");
                    } catch (IOException e) {
                        Log.e("", e.getMessage());
                        try {
                            Log.e("", "trying fallback...");

                            socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(device, 1);
                            socket.connect();

                            Log.e("", "Connected");
                        } catch (Exception e2) {
                            Log.e("", "Couldn't establish Bluetooth connection!");
                        }


                        //socket.connect();

                        outputStream = socket.getOutputStream();
                        inStream = socket.getInputStream();
                    }

                    Log.e("error", "No appropriate paired devices.");
                } else {
                    Log.e("error", "Bluetooth is disabled.");
                }
            }
        }
    }

    public void write(String s) throws IOException {
        outputStream.write(s.getBytes());
    }

    public void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes = 0;
        int b = BUFFER_SIZE;

        while (true) {
            try {
                bytes = inStream.read(buffer, bytes, BUFFER_SIZE - bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == Cons.REQUEST_ENABLE_BT && resultCode== Activity.RESULT_OK) {
//            BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
//            String address = BT.getAddress();
//            String name = BT.getName();
//            String toastText = name + " : " + address;
//            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
//        }
//    }
//
////    public class ShowDevices extends ListActivity {
////
////
////        protected void onCreate(Bundle savedInstanceState) {
////
////            // search for more devices
////            mBluetoothAdapter.startDiscovery();
////
////            // user selects one device
////            BluetoothDevice device =
////                    mBluetoothAdapter.getRemoteDevice(/* mac addr String */);
////            Intent data = new Intent();
////            data.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
////            setResult(RESULT_OK, data);
////            finish();
////
////        }
////    }
//
//    public class DataTransferActivity extends Activity {
//
//
//
//        @Override
//        public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
//            super.onCreate(savedInstanceState, persistentState);
//
//            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            if (mBluetoothAdapter == null) {
//                // No Bluetooth support
//                finish();
//            }
//            if (!mBluetoothAdapter.isEnabled()) {
//                Intent enableBluetoothIntent =
//                        new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(
//                        enableBluetoothIntent, Cons.REQUEST_ENABLE_BT);
//            }
//        }
//    }
//
//    // Listen for connection
//    class AcceptThread extends Thread {
//        public AcceptThread(Handler handler) {
//
//            try {
//                mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("test", DEFAULT_UUID);
//            }
//            catch (IOException e) {}
//        }
//
//        public void run() {
//            while (true) {
//                try {
//
//                    mBluetoothSocket = mServerSocket.accept();
//                    manageConnectedSocket();
//                    mServerSocket.close();
//                    break;
//                }
//                catch (IOException e1) {
//                    e1.getMessage();
//                }
//            }
//        }
//    }
//
//    public class ConnectThread extends Thread {
//
//        public ConnectThread(String deviceID, Handler handler) {
//            mDevice = mBluetoothAdapter.getRemoteDevice(deviceID);
//            try {
//                mBluetoothSocket = mDevice.createRfcommSocketToServiceRecord(
//                        DEFAULT_UUID);
//            }
//            catch (IOException e) {
//                e.getMessage();
//            }
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
//                    mHandler.obtainMessage(
//                            DataTransferActivity.DATA_RECEIVED, data).sendToTarget();
//                } catch (IOException e) {
//                    break;
//                }
//            }
//        }
//    }
//
//    public void write(byte[] bytes) {
//        try {
//            mOutStream.write(bytes);
//        } catch (IOException e) {
//            e.getMessage();
//        }
//    }

}
