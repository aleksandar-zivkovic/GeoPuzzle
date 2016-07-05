package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;
import rs.elfak.got.geopuzzle.library.UserFunctions;

public class SearchForFriendsActivity extends AppCompatActivity {

    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ListView mListView;
    private ListView mListViewPendingFriendRequests;
    private Button mAcceptButton;
    private Button mDeclineButton;
    private Button mSendButton;

    private BluetoothServerSocket mmServerSocket;
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

    private boolean mPositionSet;
    private int mPosition;
    private int mPositionRemoteEmail;
    private String mEmail;
    private String mRemoteEmail;
    private String mSenderDeviceAddress;
    private String mSelectedAddress;
    private DatabaseHandler db;
    private BroadcastReceiver discoveryResult;
    private ArrayList<String> mPendingFriendRequestList;
    private HashMap mUser;
    private ArrayAdapter mAdapter;


    @Override
    protected void onCreate(@org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_friends);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        // Find Views by Id
        mListView = (ListView) findViewById(R.id.userListView);
        mListViewPendingFriendRequests = (ListView) findViewById(R.id.listViewPendingFriendRequests);
        mAcceptButton = (Button) findViewById(R.id.buttonAccept);
        mDeclineButton = (Button) findViewById(R.id.buttonDecline);
        mSendButton = (Button) findViewById(R.id.buttonSend);

        mPendingFriendRequestList = new ArrayList<String>();
        mAdapter = new ArrayAdapter(SearchForFriendsActivity.this , android.R.layout.simple_list_item_1, mPendingFriendRequestList);
        mListViewPendingFriendRequests.setAdapter(mAdapter);

        // boolean to ensure that position is always set before starting bluetooth connection
        mPositionSet = false;

        // Get my email
        db = new DatabaseHandler(getApplicationContext());
        mUser = db.getUserDetails();
        mEmail = (String)mUser.get(Cons.KEY_EMAIL);

        // Enable adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            finish(); // No Bluetooth support -> finish();
        }

        // turn on Bluetooth on startup and start Accept Thread
        Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(turnOn, Cons.REQUEST_ENABLE_BT);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    // friend request received
                    case Cons.DATA_RECEIVED: {
                        String data = (String) msg.obj;
                        String[] split = data.split("\n");
                        mRemoteEmail = split[0];
                        // Check if friend request already pending
                        if (mPendingFriendRequestList.contains(data.toString())) {
                            break;
                        }
                        // Check if you are already friend with the sender -> if not add sender as a friend
                        ProcessMyFriends processMyFriends = new ProcessMyFriends();
                        processMyFriends.execute();
                        break;
                    }
                    // already friends
                    case Cons.DATA_RESPONSE: {
                        String data = (String) msg.obj;
                        String mResponse = data;
                        Toast.makeText(getApplicationContext(), mResponse, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case Cons.DATA_ACCEPTED: {
                        String data = (String) msg.obj;
                        String mResponse = data;
                        Toast.makeText(getApplicationContext(), mResponse, Toast.LENGTH_SHORT).show();
                        String[] split = data.split(" ");
                        String mSelectedEmailAddress = split[0];

                        ProcessFriendship processFriendship = new ProcessFriendship(mEmail, mSelectedEmailAddress);
                        processFriendship.execute();
                        break;
                    }
                    case Cons.DATA_DECLINED: {
                        String data = (String) msg.obj;
                        String mResponse = data;
                        Toast.makeText(getApplicationContext(), mResponse, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "mPosition set to: " + mPosition, Toast.LENGTH_SHORT).show();
                mSendButton.setEnabled(true);
            }
        });

        mListViewPendingFriendRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPositionRemoteEmail = position;
//                String selectedEmailAndAddress = mPendingFriendRequestList.get(mPositionRemoteEmail);
//                // get just the address, not the name
//                String[] split = selectedEmailAndAddress.split("\n");
//                mSelectedAddress = split[1];
                mSelectedAddress = mPendingFriendRequestList.get(mPositionRemoteEmail);
                Toast.makeText(getApplicationContext(), "Selected friend request from: " + mSelectedAddress, Toast.LENGTH_SHORT).show();
                mAcceptButton.setEnabled(true);
                mDeclineButton.setEnabled(true);
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
    }

    public void acceptFriendRequest(View v) {
        // Send POSITIVE response back to the device which sent you friend request
        String responseAnswer = mEmail + " accepted friend request";
        BluetoothDevice mResponseRemoteDevice =  mBluetoothAdapter.getRemoteDevice(mSenderDeviceAddress);
        ConnectThread responseConnectThread = new ConnectThread(mResponseRemoteDevice, responseAnswer);
        responseConnectThread.start();

        mPendingFriendRequestList.remove(mPositionRemoteEmail);
        mAdapter.notifyDataSetChanged();
        // disable button
        mAcceptButton.setEnabled(false);
    }

    public void declineFriendRequest(View v) {
        // Send NEGATIVE response back to the device which sent you friend request
        String responseAnswer = mEmail + " declined friend request";
        BluetoothDevice mResponseRemoteDevice =  mBluetoothAdapter.getRemoteDevice(mSenderDeviceAddress);
        ConnectThread responseConnectThread = new ConnectThread(mResponseRemoteDevice, responseAnswer);
        responseConnectThread.start();

        mPendingFriendRequestList.remove(mPositionRemoteEmail);
        mAdapter.notifyDataSetChanged();
        // disable button
        mAcceptButton.setEnabled(false);
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
        Intent showDevicesIntent = new Intent(this, ShowDevicesActivity.class);
        showDevicesIntent.putStringArrayListExtra("devices", pairedDeviceStrings);
        startActivityForResult(showDevicesIntent, Cons.SELECT_SERVER);
    }

    public void send(View v) {
        if (!mPositionSet) {
            Toast.makeText(this, "You have to choose a device!", Toast.LENGTH_SHORT).show();
            return;
        }

        // get just the address, not the name
        String nameAndAddress = pairedDeviceStrings.get(mPosition);
        String[] split = nameAndAddress.split("\n");
        String address = split[1];
        mRemoteDevice =  mBluetoothAdapter.getRemoteDevice(address);
        mConnectThread = new ConnectThread(mRemoteDevice);
        mConnectThread.start();
        // disable Send button
        mSendButton.setEnabled(false);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mResponse = "undefined";

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket, because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e) {
                e.getMessage();
            }
            mmSocket = tmp;

        }

        public ConnectThread(BluetoothDevice device, String responseAnswer) {
            mResponse = responseAnswer;
            // Use a temporary object that is later assigned to mmSocket, because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                //tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e) {
                e.getMessage();
            }
            mmSocket = tmp;

        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            String address = "undefined";

            try {
                // Connect the device through the socket. This will block until it succeeds or throws an exception
                mmSocket.connect();
                address = mmSocket.getRemoteDevice().getAddress();
                mConnectionThread = new ConnectionThread(mmSocket);
            }
            catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                }
                catch (IOException closeException) {
                    closeException.getMessage();
                }
                return;
            }

            String stringToSend;
            // Do work to manage the connection (in a separate thread)
            if (mResponse.equals("undefined")) {
                stringToSend = mEmail + "\n" + address;
            }
            else {
                stringToSend = mResponse;
            }
            mConnectionThread.write(stringToSend.getBytes());
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                //mConnectionThread.cancel();
                mmSocket.close();
            }
            catch (IOException e) {
                e.getMessage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Cons.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
            String address = BT.getAddress();
            String name = BT.getName();
            //Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();
            String toastText = "My device is " + name + " : " + address;
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

            // Start listening
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
            Toast.makeText(getApplicationContext(), "Accept Thread started. Listening for bluetooth socket connections...", Toast.LENGTH_SHORT).show();
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
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        }
        catch (Exception e) {
            e.getMessage();
        }
    }


    //For UnPairing
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        }
        catch (Exception e) {
            e.getMessage();
        }
    }

    private class AcceptThread extends Thread {


        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket, because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                //tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("friendRequest", MY_UUID);
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("friendRequest", MY_UUID);

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
                }
                catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    try {
                        mSenderDeviceAddress = socket.getRemoteDevice().getAddress();
                        bytes = socket.getInputStream().read(buffer);
                        String data = new String(buffer, 0, bytes);

                        // this messages cannot be handled here, because this is worker thread, so we use this handler to send it to send it as message to main thread
                        // already friends
                        if (data.contains("already friends")) {
                            mHandler.obtainMessage(Cons.DATA_RESPONSE, data).sendToTarget();
                            //mmServerSocket.close();
                        }
                        // accept
                        else if (data.contains("accepted")) {
                            mHandler.obtainMessage(Cons.DATA_ACCEPTED, data).sendToTarget();
                            //mmServerSocket.close();
                        }
                        // decline
                        else if (data.contains("declined")) {
                            mHandler.obtainMessage(Cons.DATA_DECLINED, data).sendToTarget();
                            //mmServerSocket.close();
                        }
                        // email
                        else {
                            mHandler.obtainMessage(Cons.DATA_RECEIVED, data).sendToTarget();
                            //mmServerSocket.close();
                        }
                    }
                    catch (Exception e) {
                        e.getMessage();
                    }
                    break;
                    // TODO: RAZMOTRI SUTRA OVO ZATVARANJE SOKETA I OVAJ BREAK -> DEBUG-UJ
                }
            }
        }
    }

    private class ConnectionThread extends Thread {
        private final BluetoothSocket mmSocket;
        //private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectionThread(BluetoothSocket socket) {
            mmSocket = socket;
            //InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because member streams are final
            try {
                //tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            //mmInStream = tmpIn;
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
                mmOutStream.close();
                mmSocket.close();
            }
            catch (IOException e) {
                e.getMessage();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(discoveryResult);
        try {
            mmServerSocket.close();
        }
        catch (Exception e) {
            e.getMessage();
        }
    }

    private class ProcessMyFriends extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SearchForFriendsActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Fetching friends...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            return userFunction.fetchFriends(getApplicationContext());
        }

        @Override
        protected void onPostExecute(Object o) {

            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {
                    String res = json.getString(Cons.KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1) {
                        boolean alreadyFriends;
                        pDialog.setTitle(R.string.msg_getting_data);
                        pDialog.setMessage("Checking friends list...");

                        int friendsNum = json.getInt(Cons.KEY_FRIENDS_NUM);
                        if(friendsNum == 0) {
                            alreadyFriends = false;
                        }
                        else {
                            String responseAnswer;
                            alreadyFriends = false;

                            for(int i = 0; i < friendsNum; i++) {
                                JSONObject friend = json.getJSONObject("friend" + (i+1) );
                                String friendsEmail = friend.getString(Cons.KEY_EMAIL);



                                if (friendsEmail.equals(mRemoteEmail)) {
                                    alreadyFriends = true;
                                    Toast.makeText(getApplicationContext(), "You are already friends with " + friendsEmail , Toast.LENGTH_SHORT).show();
                                    // Send already friend string back to the sender
                                    responseAnswer = "You are already friends with " + mEmail; // send you are already friends with ME
                                    BluetoothDevice mResponseRemoteDevice =  mBluetoothAdapter.getRemoteDevice(mSenderDeviceAddress);
                                    ConnectThread responseConnectThread = new ConnectThread(mResponseRemoteDevice, responseAnswer);
                                    responseConnectThread.start();
                                    break;
                                }
                            }
                        }
                        if (alreadyFriends == false) {
                            // add it to pending friend request list
                            mPendingFriendRequestList.add(mRemoteEmail);
                            mAdapter.notifyDataSetChanged();
                        }
                        pDialog.dismiss();
                    }
                    else {
                        pDialog.dismiss();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ProcessFriendship extends AsyncTask {
        private ProgressDialog pDialog;
        String email1, email2;

        public ProcessFriendship(String email_1, String email_2) {
            this.email1 = email_1;
            this.email2 = email_2;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SearchForFriendsActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Adding friendship...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            return userFunction.addFriendship(email1, email2);
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject) o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {
                    String res = json.getString(Cons.KEY_SUCCESS);

                    if (Integer.parseInt(res) == 1) {
                        Toast.makeText(getApplicationContext(), "You have successfully added a friend!", Toast.LENGTH_SHORT).show();
                    }
                }
                pDialog.dismiss();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
