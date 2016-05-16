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
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;

import rs.elfak.got.geopuzzle.library.*;

public class ResetPasswordActivity extends AppCompatActivity {
    EditText mEmailEdit;
    Button mResetPasswordBtn;
    Button mCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mEmailEdit = (EditText) findViewById(R.id.emailEdit);
        mResetPasswordBtn = (Button) findViewById(R.id.resetPasswordBtn);
        mCancelBtn = (Button) findViewById(R.id.cancelBtn);

        mResetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEdit.getText().toString();
                if(email.equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_email_empty, Toast.LENGTH_SHORT).show();
                    if (mEmailEdit.requestFocus()) {
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
        protected void onPreExecute() {
            super.onPreExecute();
            nDialog = new ProgressDialog(ResetPasswordActivity.this);
            nDialog.setTitle(R.string.msg_checking_network);
            nDialog.setMessage("Loading...");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
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
            if ((Boolean)o) {
                nDialog.dismiss();
                new ProcessRegister().execute();
            }
            else {
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.msg_network_error, Toast.LENGTH_LONG).show();
            }
        }

        private class ProcessRegister extends AsyncTask {
            private ProgressDialog pDialog;
            String email;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                email = mEmailEdit.getText().toString();

                pDialog = new ProgressDialog(ResetPasswordActivity.this);
                pDialog.setTitle(R.string.msg_contacting_servers);
                pDialog.setMessage("Getting Data ...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] params) {
                UserFunctions userFunction = new UserFunctions();
                JSONObject json = userFunction.forPass(email);
                return json;
            }

            @Override
            protected void onPostExecute(Object o) {
                JSONObject json = (JSONObject) o;
                try {
                    if (json.getString(Cons.KEY_SUCCESS) != null) {
                        String res = json.getString(Cons.KEY_SUCCESS);
                        String red = json.getString(Cons.KEY_ERROR);

                        if (Integer.parseInt(res) == 1) {
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.msg_recovery_mail, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        else if (Integer.parseInt(red) == 2) {
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.msg_email_dont_exist, Toast.LENGTH_SHORT).show();
                            if(mEmailEdit.requestFocus()) {
                                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            }
                        }
                        else {
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.msg_password_reset_error, Toast.LENGTH_SHORT).show();
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
