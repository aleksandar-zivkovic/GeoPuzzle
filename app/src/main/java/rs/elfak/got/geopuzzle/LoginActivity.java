package rs.elfak.got.geopuzzle;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import rs.elfak.got.geopuzzle.library.DatabaseHandler;
import rs.elfak.got.geopuzzle.library.UserFunctions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends Activity {
    Button btnLogin;
    Button Btnregister;
    Button passreset;
    EditText inputEmail;
    EditText inputPassword;
    private TextView loginErrorMsg;
    /**
     * Called when the activity is first created.
     */
    private static String KEY_SUCCESS = "success";
    private static String KEY_UID = "uid";
    private static String KEY_USERNAME = "uname";
    private static String KEY_FIRSTNAME = "fname";
    private static String KEY_LASTNAME = "lname";
    private static String KEY_EMAIL = "email";
    private static String KEY_CREATED_AT = "created_at";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.pword);
        Btnregister = (Button) findViewById(R.id.registerbtn);
        btnLogin = (Button) findViewById(R.id.login);
        passreset = (Button) findViewById(R.id.passres);
        loginErrorMsg = (TextView) findViewById(R.id.loginErrorMsg);

        passreset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), ResetPasswordActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }
        });

        Btnregister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), RegisterActivity.class);
                startActivityForResult(myIntent, 0);
                finish();
            }
        });

        /**
         * Login button click event
         * A Toast is set to alert when the Email and Password field is empty
         **/
        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if ((!inputEmail.getText().toString().equals("")) && (!inputPassword.getText().toString().equals(""))) {
                    NetAsync(view);
                }
                else if ( ( !inputEmail.getText().toString().equals("")) ) {
                    Toast.makeText(getApplicationContext(),
                            "Password field empty", Toast.LENGTH_SHORT).show();
                }
                else if ( ( !inputPassword.getText().toString().equals("")) ) {
                    Toast.makeText(getApplicationContext(),
                            "Email field empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Email and Password field are empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Async Task to check whether internet connection is working.
     **/

    private class NetCheck extends AsyncTask {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(LoginActivity.this);
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading..");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            /**
             * Gets current device state and checks for working internet connection by trying Google.
             **/
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
                } catch (MalformedURLException e1) {
                    e1.printStackTrace();
                } catch (IOException e) {
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
                loginErrorMsg.setText("Error in Network Connection");
            }
        }
    }

    /**
     * Async Task to get and send data to My Sql database through JSON respone.
     **/
    private class ProcessLogin extends AsyncTask {

        private ProgressDialog pDialog;
        String email,password;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            inputEmail = (EditText) findViewById(R.id.email);
            inputPassword = (EditText) findViewById(R.id.pword);
            email = inputEmail.getText().toString();
            password = inputPassword.getText().toString();
            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setTitle("Contacting Servers");
            pDialog.setMessage("Logging in ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            JSONObject json = userFunction.loginUser(email, password);
            return json;
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(KEY_SUCCESS) != null) {

                    String res = json.getString(KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1){
                        pDialog.setMessage("Loading User Space");
                        pDialog.setTitle("Getting Data");
                        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                        JSONObject json_user = json.getJSONObject("user");
                        /**
                         * Clear all previous data in SQlite database.
                         **/
                        UserFunctions logout = new UserFunctions();
                        logout.logoutUser(getApplicationContext());
                        db.addUser(json_user.getString(KEY_FIRSTNAME),json_user.getString(KEY_LASTNAME),json_user.getString(KEY_EMAIL),json_user.getString(KEY_USERNAME),json_user.getString(KEY_UID),json_user.getString(KEY_CREATED_AT));
                        /**
                         *If JSON array details are stored in SQlite it launches the User Panel.
                         **/
                        Intent upanel = new Intent(getApplicationContext(), MainActivity.class);
                        upanel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        pDialog.dismiss();
                        startActivity(upanel);
                        /**
                         * Close Login Screen
                         **/
                        finish();
                    }else{

                        pDialog.dismiss();
                        loginErrorMsg.setText("Incorrect username/password");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void NetAsync(View view){
        new NetCheck().execute();
    }
}