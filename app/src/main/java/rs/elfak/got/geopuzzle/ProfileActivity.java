package rs.elfak.got.geopuzzle;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import rs.elfak.got.geopuzzle.library.*;

public class ProfileActivity extends AppCompatActivity {
    private Button mLogoutBtn;
    private Button mChangePasswordBtn;
    private Button mViewMyFriendsBtn;
    private Button mSearchForFriendsBtn;
    private ImageView mUserImageView;

    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mLogoutBtn = (Button) findViewById(R.id.logoutBtn);
        mChangePasswordBtn = (Button) findViewById(R.id.changePasswordBtn);
        mViewMyFriendsBtn = (Button) findViewById(R.id.viewMyFriendsBtn);
        mSearchForFriendsBtn = (Button) findViewById(R.id.searchForFriendsBtn);
        mUserImageView = (ImageView) findViewById(R.id.userImg);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        // Hashmap to load data from the Sqlite database
        HashMap user = db.getUserDetails();

        // Start Search For Friends Activity
        mSearchForFriendsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent searchForFriends = new Intent(getApplicationContext(), SearchForFriendsActivity.class);
                startActivity(searchForFriends);
            }
        });
        // Start My Friends Activity
        mViewMyFriendsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent myFriends = new Intent(getApplicationContext(), MyFriendsActivity.class);
                startActivity(myFriends);
            }
        });
        // Start Change Password Activity
        mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent changePass = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePass);
            }
        });
        // Logout from the User Panel which clears the data in Sqlite database
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                UserFunctions logout = new UserFunctions();
                logout.logoutUser(getApplicationContext());
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                finish();
            }
        });

        // Sets user first name and last name in text view
        TextView titleText = (TextView) findViewById(R.id.titleText);
        titleText.setText(user.get(Cons.KEY_FIRSTNAME) + " " + user.get(Cons.KEY_LASTNAME));

        // Gets email
        mEmail = user.get(Cons.KEY_EMAIL).toString();


        new DownloadImageTask().execute("http://vasic.ddns.net/geopuzzle_login_api/uploads/" + mEmail + ".jpg");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    private class DownloadImageTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return (Bitmap) loadImageFromNetwork((String) params[0]);
        }

        @Override
        protected void onPostExecute(Object o) {

            mUserImageView.setImageBitmap((Bitmap) o);
        }

        private Bitmap loadImageFromNetwork(String url){
            try {
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
                return bitmap;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
