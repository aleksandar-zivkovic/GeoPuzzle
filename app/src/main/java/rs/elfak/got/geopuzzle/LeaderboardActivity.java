package rs.elfak.got.geopuzzle;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import rs.elfak.got.geopuzzle.library.UserFunctions;

public class LeaderboardActivity extends AppCompatActivity {

    private ListView mScoreList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        mScoreList = (ListView) findViewById(R.id.scoreListView);
        NetAsync();
    }

    private class NetCheck extends AsyncTask {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(LeaderboardActivity.this);
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
                new ProcessMyScores().execute();
            }
            else {
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ProcessMyScores extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(LeaderboardActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Fetching scores...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            return userFunction.fetchScores();
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {
                    String res = json.getString(Cons.KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1) {
                        pDialog.setTitle(R.string.msg_getting_data);
                        pDialog.setMessage("Loading score list...");

                        int userNum = json.getInt("userNum");
                        if(userNum == 0)
                            Toast.makeText(getApplicationContext(), "No users to show!", Toast.LENGTH_SHORT).show();
                        else {
                            ArrayList<String> userNames = new ArrayList<String>();
                            ArrayList<String> userEmails = new ArrayList<String>();
                            ArrayList<String> userPuzzleSolved= new ArrayList<String>();
                            for(int i = 1; i <= userNum; i++) {
                                JSONObject user = json.getJSONObject("user" + i);
                                userNames.add(user.getString("firstname") + " " + user.getString("lastname"));
                                userEmails.add(user.getString("email"));
                                userPuzzleSolved.add(user.getString("puzzles_solved"));
                            }

                            ScoreList adapter = new ScoreList(LeaderboardActivity.this, userNames.toArray(new String[userNames.size()]), userEmails.toArray(new String[userEmails.size()]), userPuzzleSolved.toArray(new String[userPuzzleSolved.size()]));
                            mScoreList.setAdapter(adapter);
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
