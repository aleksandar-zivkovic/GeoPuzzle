package rs.elfak.got.geopuzzle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;

/**
 * Created by Aleksandar on 30.6.2016..
 */
public class RegisterApp extends AsyncTask<Void, Void, String> {

    private ProgressDialog pDialog;
    private static final String TAG = "GCMRelated";
    Context ctx;
    GoogleCloudMessaging gcm;
    String SENDER_ID = "613497862690";
    String regid = null;

    public RegisterApp(Context ctx, ProgressDialog pDialog) {
        this.ctx = ctx;
        this.pDialog = pDialog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if(pDialog != null)
        {
            pDialog.setMessage("Registering on Google Cloud Messaging service...");
            pDialog.show();
        }
    }

    @Override
    protected String doInBackground(Void... arg0) {
        String msg = "";
        try {
            if (gcm == null) {
                gcm = GoogleCloudMessaging.getInstance(ctx);
            }
            regid = gcm.register(SENDER_ID);
            msg = "Device registered, registration ID=" + regid;

            // You should send the registration ID to your server over HTTP,
            // so it can use GCM/HTTP or CCS to send messages to your app.
            // The request to your server should be authenticated if your app
            // is using accounts.
            sendRegistrationIdToBackend();

            // For this demo: we don't need to send it because the device
            // will send upstream messages to a server that echo back the
            // message using the 'from' address in the message.

            // Persist the regID - no need to register again.
            DatabaseHandler db = new DatabaseHandler(ctx);
            //HashMap regId = db.getKeyValue(Cons.KEY_REG_ID);
            //if(!regId.containsKey(Cons.KEY_REG_ID))
            db.addValue(Cons.KEY_REG_ID, regid);

        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
        return msg;
    }

    private void sendRegistrationIdToBackend() {
        URI url = null;
        try {
            DatabaseHandler db = new DatabaseHandler(ctx);
            HashMap user = db.getUserDetails();

            String email = (String)user.get(Cons.KEY_EMAIL);
            String name = (String)user.get(Cons.KEY_FIRSTNAME) + "_" + (String)user.get(Cons.KEY_LASTNAME);

            url = new URI(Cons.SERVER_URL + "register.php?regId=" + regid + "&name=" + name + "&email=" + email);
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet request = new HttpGet();
        request.setURI(url);
        try {
            httpclient.execute(request);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Toast.makeText(ctx, "Registration Completed. Now you can see the notifications", Toast.LENGTH_SHORT).show();
        Log.v(TAG, result);

        if(pDialog != null)
            pDialog.dismiss();
    }
}
