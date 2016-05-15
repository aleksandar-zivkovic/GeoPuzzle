package rs.elfak.got.geopuzzle;

import java.io.IOException;
import java.util.HashMap;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;

import rs.elfak.got.geopuzzle.library.*;

public class ChangePasswordActivity extends AppCompatActivity {
    private EditText mPasswordEdit;
    private EditText mConfirmPasswordEdit;
    private Button mChangePasswordBtn;
    private Button mCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mPasswordEdit = (EditText) findViewById(R.id.passwordEdit);
        mConfirmPasswordEdit = (EditText) findViewById(R.id.confirmPasswordEdit);
        mChangePasswordBtn = (Button) findViewById(R.id.changePasswordBtn);
        mCancelBtn = (Button) findViewById(R.id.cancelBtn);

        mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassword = mPasswordEdit.getText().toString();
                String confirmPassword = mConfirmPasswordEdit.getText().toString();
                if(newPassword.equals("")) {
                    Toast.makeText(getApplicationContext(), "Password field empty.", Toast.LENGTH_SHORT).show();
                    if (mPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(confirmPassword.equals("")) {
                    Toast.makeText(getApplicationContext(), "Password confirm field empty.", Toast.LENGTH_SHORT).show();
                    if (mConfirmPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(newPassword.equals(confirmPassword)) {
                    if(newPassword.length() >= 6) {
                        NetAsync(view);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Password must contain 6 or more characters.", Toast.LENGTH_SHORT).show();
                        if(mPasswordEdit.requestFocus()) {
                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Passwords doesn't match.", Toast.LENGTH_SHORT).show();
                    if(mConfirmPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent login = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(login);
                finish();
            }
        });
    }

    // Async Task to check whether internet connection is working
    private class NetCheck extends AsyncTask {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(ChangePasswordActivity.this);
            nDialog.setMessage("Loading...");
            nDialog.setTitle("Checking Network");
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
                new ProcessPasswordChange().execute();
            }
            else {
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error in Network Connection.", Toast.LENGTH_LONG).show();
            }
        }

        // Async Task to get and send data to My Sql database through JSON response
        private class ProcessPasswordChange extends AsyncTask {
            private ProgressDialog pDialog;
            String email, newPassword;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                DatabaseHandler db = new DatabaseHandler(getApplicationContext());

                HashMap user = new HashMap();
                user = db.getUserDetails();

                newPassword = mPasswordEdit.getText().toString();
                email = (String)user.get("email");

                pDialog = new ProgressDialog(ChangePasswordActivity.this);
                pDialog.setTitle("Contacting Servers");
                pDialog.setMessage("Getting Data ...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] params) {
                UserFunctions userFunction = new UserFunctions();
                JSONObject json = userFunction.chgPass(newPassword, email);
                return json;
            }

            @Override
            protected void onPostExecute(Object o) {
                JSONObject json = (JSONObject)o;
                try {
                    if (json.getString(Cons.KEY_SUCCESS) != null) {
                        String res = json.getString(Cons.KEY_SUCCESS);
                        String red = json.getString(Cons.KEY_ERROR);

                        if (Integer.parseInt(res) == 1) {
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Your Password is successfully changed.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else if (Integer.parseInt(red) == 2) {
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Invalid old Password.", Toast.LENGTH_LONG).show();
                        }
                        else {
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Error occured in changing Password.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void NetAsync(View view){
        new NetCheck().execute();
    }
}
