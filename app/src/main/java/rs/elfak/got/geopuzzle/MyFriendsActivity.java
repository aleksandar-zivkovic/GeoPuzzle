package rs.elfak.got.geopuzzle;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;
import rs.elfak.got.geopuzzle.library.UserFunctions;

public class MyFriendsActivity extends AppCompatActivity {

    private ListView mFriendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);

        mFriendsList = (ListView) findViewById(R.id.friendsListView);
        NetAsync();
    }

    // Async Task to check whether internet connection is working
    private class NetCheck extends AsyncTask {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(MyFriendsActivity.this);
            nDialog.setTitle(R.string.msg_checking_network);
            nDialog.setMessage("Loading...");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            // Gets current device state and checks for working internet connection by trying Google
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                }
                catch (MalformedURLException e1) {
                    e1.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Object o) {
            if((Boolean)o == true){
                nDialog.dismiss();
                new ProcessMyFriends().execute();
            }
            else {
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Async Task to get and send data to My Sql database through JSON response
    private class ProcessMyFriends extends AsyncTask {
        private ProgressDialog pDialog;
        String email, password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MyFriendsActivity.this);
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
                        pDialog.setTitle(R.string.msg_getting_data);

                        int friendsNum = json.getInt(Cons.KEY_FRIENDS_NUM);
                        if(friendsNum == 0)
                            Toast.makeText(getApplicationContext(), R.string.msg_friends_list_empty, Toast.LENGTH_SHORT).show();
                        else {
                        pDialog.setMessage("Loading friends list...");
                            ArrayList<String> friendNames = new ArrayList<String>();
                            ArrayList<String> friendEmails = new ArrayList<String>();
                            for(int i = 1; i <= friendsNum; i++) {
                                JSONObject friend = json.getJSONObject("friend" + i);
                                friendNames.add(friend.getString(Cons.KEY_FIRSTNAME) + " " + friend.getString(Cons.KEY_LASTNAME));
                                friendEmails.add(friend.getString(Cons.KEY_EMAIL));
                            }

                            FriendList adapter = new FriendList(MyFriendsActivity.this, friendNames.toArray(new String[friendNames.size()]), friendEmails.toArray(new String[friendEmails.size()]));
                            mFriendsList.setAdapter(adapter);
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

    public void NetAsync(){
        new NetCheck().execute();
    }
}
