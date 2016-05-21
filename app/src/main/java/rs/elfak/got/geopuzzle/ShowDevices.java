package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ShowDevices extends Activity {

    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;
    private int mPosition;
    private String mAddress;

    ArrayList<String> mListItems= new ArrayList<String>();
    ArrayAdapter mAdapter;

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // update display

                mListItems.add(device.getAddress());
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_devices);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mListView = (ListView) findViewById(R.id.list);
        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mListItems);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = position;
                mAddress = mListItems.get(mPosition);
                //mAddress = mListView.getSelectedItem().toString();

                // user selects one device
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mAddress);
                Intent data = new Intent();
                data.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // search for more devices
        mBluetoothAdapter.startDiscovery();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

}
