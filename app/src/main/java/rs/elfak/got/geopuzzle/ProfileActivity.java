package rs.elfak.got.geopuzzle;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;

import rs.elfak.got.geopuzzle.library.*;

public class ProfileActivity extends AppCompatActivity {
    private static final int STATE_USER_PROFILE = 0;
    private static final int STATE_FRIEND_PROFILE = 1;

    private Button mLogoutBtn;
    private Button mChangePasswordBtn;
    private Button mViewMyFriendsBtn;
    private Button mSearchForFriendsBtn;
    private ImageView mUserImageView;
    private ProgressDialog pDialog;
    private int state;

    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        pDialog = new ProgressDialog(ProfileActivity.this);
        pDialog.setTitle(R.string.msg_contacting_servers);
        pDialog.setMessage("Downloading data...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        mUserImageView = (ImageView) findViewById(R.id.userImg);
        mLogoutBtn = (Button) findViewById(R.id.logoutBtn);
        mChangePasswordBtn = (Button) findViewById(R.id.changePasswordBtn);
        mViewMyFriendsBtn = (Button) findViewById(R.id.viewMyFriendsBtn);
        mSearchForFriendsBtn = (Button) findViewById(R.id.searchForFriendsBtn);
        TextView titleText = (TextView) findViewById(R.id.titleText);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        // Hashmap to load data from the Sqlite database or server
        HashMap user;

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.getString(Cons.KEY_EMAIL) != null) {
            state = STATE_FRIEND_PROFILE;
            this.setTitle(R.string.title_activity_friend);

            mLogoutBtn.setVisibility(View.INVISIBLE);
            mChangePasswordBtn.setVisibility(View.INVISIBLE);
            mViewMyFriendsBtn.setVisibility(View.INVISIBLE);
            mSearchForFriendsBtn.setVisibility(View.INVISIBLE);

            // Sets user first name and last name in text view
            titleText.setText(bundle.getString(Cons.KEY_FULLNAME));
            mEmail = bundle.getString(Cons.KEY_EMAIL);
        }
        else {
            state = STATE_USER_PROFILE;
            user = db.getUserDetails();

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
                    login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(login);
                    finish();
                }
            });

            // Sets user first name and last name in text view
            titleText.setText(user.get(Cons.KEY_FIRSTNAME) + " " + user.get(Cons.KEY_LASTNAME));

            // Gets email
            mEmail = user.get(Cons.KEY_EMAIL).toString();
        }

        new DownloadImageTask().execute(Cons.KEY_UPLOADS_URL + mEmail + ".jpg");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(state == STATE_USER_PROFILE)
            getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        else
            getMenuInflater().inflate(R.menu.menu_friend_profile, menu);
        return true;
    }

    private class DownloadImageTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return (Bitmap) UserFunctions.loadImageFromNetwork((String) params[0]);
        }

        @Override
        protected void onPostExecute(Object o) {
            if(o != null)
                mUserImageView.setImageBitmap((Bitmap) o);

            pDialog.dismiss();
        }
    }
}
