package rs.elfak.got.geopuzzle;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import rs.elfak.got.geopuzzle.library.*;

public class LoginActivity extends AppCompatActivity {
    private Button mLoginBtn;
    private Button mRegisterBtn;
    private Button mPasswordResetBtn;
    private EditText mEmailEdit;
    private EditText mPasswordEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailEdit = (EditText) findViewById(R.id.emailEdit);
        mPasswordEdit = (EditText) findViewById(R.id.passwordEdit);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);
        mLoginBtn = (Button) findViewById(R.id.loginBtn);
        mPasswordResetBtn = (Button) findViewById(R.id.resetPasswordBtn);

        mPasswordResetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resetPass = new Intent(view.getContext(), ResetPasswordActivity.class);
                startActivity(resetPass);
            }
        });
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resetPass = new Intent(view.getContext(), RegisterActivity.class);
                startActivity(resetPass);
            }
        });
        // A Toast is set to alert when the Email and Password field is empty
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((!mEmailEdit.getText().toString().equals("")) && (!mPasswordEdit.getText().toString().equals(""))) {
                    NetAsync(view);
                }
                else if ((!mEmailEdit.getText().toString().equals(""))) {
                    Toast.makeText(getApplicationContext(), R.string.msg_password_empty, Toast.LENGTH_SHORT).show();
                }
                else if ((!mPasswordEdit.getText().toString().equals(""))) {
                    Toast.makeText(getApplicationContext(), R.string.msg_email_empty, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.msg_email_password_empty, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    // Async Task to check whether internet connection is working
    private class NetCheck extends AsyncTask {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(LoginActivity.this);
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
                new ProcessLogin().execute();
            }
            else {
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Async Task to get and send data to My Sql database through JSON response
    private class ProcessLogin extends AsyncTask {
        private ProgressDialog pDialog;
        String email, password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mEmailEdit = (EditText) findViewById(R.id.emailEdit);
            mPasswordEdit = (EditText) findViewById(R.id.passwordEdit);
            email = mEmailEdit.getText().toString();
            password = mPasswordEdit.getText().toString();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Logging in...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            return userFunction.loginUser(email, password);
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {
                    String res = json.getString(Cons.KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1) {
                        pDialog.setTitle(R.string.msg_getting_data);
                        pDialog.setMessage("Loading User Space");

                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        JSONObject json_user = json.getJSONObject(Cons.KEY_USER);

                        // Clear all previous data in SQlite database.
                        UserFunctions logout = new UserFunctions();
                        logout.logoutUser(getApplicationContext());
                        db.addUser(json_user.getString(Cons.KEY_FIRSTNAME),
                                json_user.getString(Cons.KEY_LASTNAME),
                                json_user.getString(Cons.KEY_EMAIL),
                                json_user.getString(Cons.KEY_USERNAME),
                                json_user.getString(Cons.KEY_PHONE_NUMBER),
                                json_user.getString(Cons.KEY_UID),
                                json_user.getString(Cons.KEY_CREATED_AT));

                        CheckBox loggedInChk = (CheckBox) findViewById(R.id.keepLoggedInChk);
                        if (loggedInChk.isChecked())
                            db.setValue(Cons.KEY_LOGGED_IN, "true");
                        else
                            db.setValue(Cons.KEY_LOGGED_IN, "false");

                        // If JSON array details are stored in SQlite it launches the User Panel.
                        Intent upanel = new Intent(getApplicationContext(), ProfileActivity.class);
                        upanel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        pDialog.dismiss();
                        startActivity(upanel);

                        // Close Login Screen
                        finish();
                    }
                    else {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), R.string.msg_incorrect_username_password, Toast.LENGTH_SHORT).show();
                        if(mEmailEdit.requestFocus()) {
                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void NetAsync(View view){
        new NetCheck().execute();
    }
}