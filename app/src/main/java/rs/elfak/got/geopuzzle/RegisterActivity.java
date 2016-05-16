package rs.elfak.got.geopuzzle;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import rs.elfak.got.geopuzzle.library.*;

public class RegisterActivity extends AppCompatActivity {
    EditText mFirstNameEdit;
    EditText mLastNameEdit;
    EditText mEmailEdit;
    EditText mUsernameEdit;
    EditText mPasswordEdit;
    EditText mConfirmPasswordEdit;
    EditText mPhoneNumberEdit;
    Button mUploadPhotoBtn;
    Button mRegisterBtn;
    Button mCancelBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirstNameEdit = (EditText) findViewById(R.id.firstNameEdit);
        mLastNameEdit = (EditText) findViewById(R.id.lastNameEdit);
        mEmailEdit = (EditText) findViewById(R.id.emailEdit);
        mUsernameEdit = (EditText) findViewById(R.id.usernameEdit);
        mPasswordEdit = (EditText) findViewById(R.id.passwordEdit);
        mConfirmPasswordEdit = (EditText) findViewById(R.id.confirmPasswordEdit);
        mPhoneNumberEdit = (EditText) findViewById(R.id.phoneNumberEdit);
        mUploadPhotoBtn = (Button) findViewById(R.id.uploadPhotoBtn);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);
        mCancelBtn = (Button) findViewById(R.id.cancelBtn);

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFirstNameEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_first_name_empty, Toast.LENGTH_SHORT).show();
                    if (mFirstNameEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mLastNameEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_last_name_empty, Toast.LENGTH_SHORT).show();
                    if (mLastNameEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mEmailEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_email_empty, Toast.LENGTH_SHORT).show();
                    if (mEmailEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mUsernameEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_username_empty, Toast.LENGTH_SHORT).show();
                    if (mUsernameEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mUsernameEdit.getText().toString().length() < 6) {
                    Toast.makeText(getApplicationContext(), R.string.msg_username_length, Toast.LENGTH_SHORT).show();
                    if (mUsernameEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mPasswordEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_password_empty, Toast.LENGTH_SHORT).show();
                    if (mPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mPasswordEdit.getText().toString().length() < 6) {
                    Toast.makeText(getApplicationContext(), R.string.msg_password_length, Toast.LENGTH_SHORT).show();
                    if (mPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mConfirmPasswordEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_password_confirm_empty, Toast.LENGTH_SHORT).show();
                    if (mConfirmPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(!mConfirmPasswordEdit.getText().toString().equals(mPasswordEdit.getText().toString())) {
                    Toast.makeText(getApplicationContext(), R.string.msg_password_not_match, Toast.LENGTH_SHORT).show();
                    if (mConfirmPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mPhoneNumberEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_phone_number_empty, Toast.LENGTH_SHORT).show();
                    if (mPhoneNumberEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else {
                    NetAsync(view);
                }
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
//                startActivityForResult(myIntent, 0);
                finish();
            }
        });
    }

    private class NetCheck extends AsyncTask {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(RegisterActivity.this);
            nDialog.setTitle(R.string.msg_checking_network);
            nDialog.setMessage("Loading...");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            // Gets current device state and checks for working internet connection by trying Google.
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
//                catch (MalformedURLException e1) {
//                    e1.printStackTrace();
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Object o) {
            if((Boolean)o){
                nDialog.dismiss();
                new ProcessRegister().execute();
            }
            else {
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        private class ProcessRegister extends AsyncTask {
            private ProgressDialog pDialog;
            String firstName, lastName, email, username, password, phoneNumber;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                firstName = mFirstNameEdit.getText().toString();
                lastName = mLastNameEdit.getText().toString();
                email = mEmailEdit.getText().toString();
                username = mUsernameEdit.getText().toString();
                password = mPasswordEdit.getText().toString();
                phoneNumber = mPhoneNumberEdit.getText().toString();

                pDialog = new ProgressDialog(RegisterActivity.this);
                pDialog.setTitle(R.string.msg_contacting_servers);
                pDialog.setMessage("Registering ...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] params) {
                UserFunctions userFunction = new UserFunctions();
                return userFunction.registerUser(firstName, lastName, email, username, password, phoneNumber);
            }

            @Override
            protected void onPostExecute(Object o) {
                JSONObject json = (JSONObject)o;
                try {
                    if (json.getString(Cons.KEY_SUCCESS) != null) {
                        String res = json.getString(Cons.KEY_SUCCESS);
                        String red = json.getString(Cons.KEY_ERROR);

                        if(Integer.parseInt(res) == 1){
                            pDialog.setTitle(R.string.msg_getting_data);
                            pDialog.setMessage("Loading Info");
                            Toast.makeText(getApplicationContext(), R.string.msg_registration_success, Toast.LENGTH_SHORT).show();

                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            JSONObject json_user = json.getJSONObject(Cons.KEY_USER);

                            // Removes all the previous data in the SQlite database
                            UserFunctions logout = new UserFunctions();
                            logout.logoutUser(getApplicationContext());
                            db.addUser(json_user.getString(Cons.KEY_FIRSTNAME),
                                    json_user.getString(Cons.KEY_LASTNAME),
                                    json_user.getString(Cons.KEY_EMAIL),
                                    json_user.getString(Cons.KEY_USERNAME),
                                    json_user.getString(Cons.KEY_PHONE_NUMBER),
                                    json_user.getString(Cons.KEY_UID),
                                    json_user.getString(Cons.KEY_CREATED_AT));

                            // Stores registered data in SQlite Database
                            Intent registered = new Intent(getApplicationContext(), RegisteredActivity.class);

                            // Close all views before launching Registered screen
                            registered.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            pDialog.dismiss();
                            startActivity(registered);

                            finish();
                        }

                        else if (Integer.parseInt(red) == 2){
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.msg_user_exist, Toast.LENGTH_SHORT).show();
                        }
                        else if (Integer.parseInt(red) == 3){
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.msg_invalid_email, Toast.LENGTH_SHORT).show();
                        }
                    }

                    else {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), R.string.msg_registration_error, Toast.LENGTH_SHORT).show();
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
