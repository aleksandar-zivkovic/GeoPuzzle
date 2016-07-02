package rs.elfak.got.geopuzzle.library;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import rs.elfak.got.geopuzzle.LoginActivity;
import rs.elfak.got.geopuzzle.R;

/**
 * Created by Milan on 14.5.2016..
 */
@SuppressWarnings("deprecation")
public class UserFunctions {
    private JSONParser jsonParser;

    //URL of the PHP API
    private static String serverURL = "http://vasic.ddns.net/geopuzzle_login_api/";

    private static String login_tag = "login";
    private static String register_tag = "register";
    private static String forpass_tag = "forpass";
    private static String chgpass_tag = "chgpass";
    private static String fetchfriends_tag = "fetchfriends";
    private static String fetchsearchfriends_tag = "fetchsearchfriends";
    private static String fetchuser_tag = "fetchuser";
    private static String fetchpuzzles_tag = "fetchpuzzles";
    private static String fetchsearchpuzzles_tag = "fetchsearchpuzzles";
    private static String fetchpuzzlechunks_tag = "fetchpuzzlechunks";
    private static String fetchpuzzlechunk_tag = "fetchpuzzlechunk";
    private static String fetchscores_tag = "fetchscores";
    private static String update_location_tag = "updatelocation";
    private static String addfriendship_tag = "addfriendship";
    private static String updatepuzzlecreated_tag = "updatepuzzlecreated";
    private static String updatepuzzlesolved_tag = "updatepuzzlesolved";
    private static String uploadpuzzle_tag = "uploadpuzzle";
    private static String uploadpuzzlechunk_tag = "uploadpuzzlechunk";
    private static String collectpuzzlechunk_tag = "collectpuzzlechunk";

    public UserFunctions() {
        jsonParser = new JSONParser();
    }

    // Function for login
    public JSONObject loginUser(String email, String password) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", login_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for password changing
    public JSONObject chgPass(String newpas, String email) {
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", chgpass_tag));

        params.add(new BasicNameValuePair("newpas", newpas));
        params.add(new BasicNameValuePair("email", email));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for password reseting
    public JSONObject forPass(String forgotpassword) {
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", forpass_tag));
        params.add(new BasicNameValuePair("forgotpassword", forgotpassword));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for Register
    public JSONObject registerUser(String fname, String lname, String email, String uname, String password, String phonenumber) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", register_tag));
        params.add(new BasicNameValuePair("fname", fname));
        params.add(new BasicNameValuePair("lname", lname));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("uname", uname));
        params.add(new BasicNameValuePair("phonenumber", phonenumber));
        params.add(new BasicNameValuePair("password", password));


        JSONObject json = jsonParser.getJSONFromUrl(serverURL,params);
        return json;
    }

    // Function for user logout
    public boolean logoutUser(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap user = db.getUserDetails();
        String email = user.get(Cons.KEY_EMAIL) != null ? user.get(Cons.KEY_EMAIL).toString() : "";

        if(!email.equals(""))
            new UnregisterApp(context, email).execute();

        db.resetTables();
        return true;
    }

    // Async Task to check whether internet connection is working
    public class UnregisterApp extends AsyncTask {
        Context context;
        String email;

        public UnregisterApp(Context context, String email){
            this.context = context;
            this.email = email;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            // Gets current device state and checks for working internet connection by trying Google
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        URI url2 = null;

                        try {
                            url2 = new URI(Cons.SERVER_URL + "unregister.php?email=" + email);
                        } catch (URISyntaxException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpGet request = new HttpGet();
                        request.setURI(url2);
                        try {
                            httpclient.execute(request);
                        } catch (ClientProtocolException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

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
            super.onPostExecute(o);
        }
    }

    // Function for friends fetching
    public JSONObject fetchFriends(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap user = db.getUserDetails();
        String email = user.get(Cons.KEY_EMAIL).toString();

        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", fetchfriends_tag));
        params.add(new BasicNameValuePair("email", email));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for friends fetching
    public JSONObject fetchSearchedFriends(Context context, String searchText) {
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap user = db.getUserDetails();
        String email = user.get(Cons.KEY_EMAIL).toString();

        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", fetchsearchfriends_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("searchText", searchText));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for fetching of user
    public JSONObject fetchUser(String email) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", fetchuser_tag));
        params.add(new BasicNameValuePair("email", email));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for puzzle fetching
    public JSONObject fetchPuzzles(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap user = db.getUserDetails();
        String email = user.get(Cons.KEY_EMAIL).toString();

        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", fetchpuzzles_tag));
        params.add(new BasicNameValuePair("email", email));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for puzzle fetching
    public JSONObject fetchSearchPuzzles(Context context, String searchText) {
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap user = db.getUserDetails();
        String email = user.get(Cons.KEY_EMAIL).toString();

        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", fetchsearchpuzzles_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("searchText", searchText));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for puzzle chunks fetching
    public JSONObject fetchPuzzleChunks() {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", fetchpuzzlechunks_tag));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for puzzle chunks collecting
    public JSONObject collectPuzzleChunk(String email, String puzzleTitle, String puzzleChunkTitle) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", collectpuzzlechunk_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("puzzleTitle", puzzleTitle));
        params.add(new BasicNameValuePair("puzzleChunkTitle", puzzleChunkTitle));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for score fetching
    public JSONObject fetchScores() {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", fetchscores_tag));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for one puzzle chunk fetching
    public JSONObject fetchPuzzleChunk(String title) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", fetchpuzzlechunk_tag));
        params.add(new BasicNameValuePair("title", title));
        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for updating of user location on server
    public JSONObject updateLocation(Context context, Location location) {
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap user = db.getUserDetails();
        String email = user.get(Cons.KEY_EMAIL).toString();

        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", update_location_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("latitude", String.valueOf(location.getLatitude())));
        params.add(new BasicNameValuePair("longitude", String.valueOf(location.getLongitude())));

        db.setValue(Cons.KEY_LATITUDE, String.valueOf(location.getLatitude()));
        db.setValue(Cons.KEY_LONGITUDE, String.valueOf(location.getLongitude()));

        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function for friendship adding
    public JSONObject addFriendship(String email1, String email2) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", addfriendship_tag));
        params.add(new BasicNameValuePair("email1", email1));
        params.add(new BasicNameValuePair("email2", email2));

        JSONObject json = jsonParser.getJSONFromUrl(serverURL,params);
        return json;
    }

    // Update user puzzle created field
    public JSONObject updateUserPuzzleCreated(String email) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", updatepuzzlecreated_tag));
        params.add(new BasicNameValuePair("email", email));

        JSONObject json = jsonParser.getJSONFromUrl(serverURL,params);
        return json;
    }

    // Update user puzzle solved field
    public JSONObject updateUserPuzzleSolved(String email) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", updatepuzzlesolved_tag));
        params.add(new BasicNameValuePair("email", email));

        JSONObject json = jsonParser.getJSONFromUrl(serverURL,params);
        return json;
    }

    // Update puzzle created field
    public JSONObject uploadPuzzle(String email, String title, int chunks) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", uploadpuzzle_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("title", title));
        String chunks_string = String.valueOf(chunks);
        params.add(new BasicNameValuePair("chunks", chunks_string));

        JSONObject json = jsonParser.getJSONFromUrl(serverURL,params);
        return json;
    }

    // Update puzzle piece created field
    public JSONObject uploadPuzzleChunk(String title, String latitude, String longitude) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", uploadpuzzlechunk_tag));
        params.add(new BasicNameValuePair("title", title));
        params.add(new BasicNameValuePair("latitude", latitude));
        params.add(new BasicNameValuePair("longitude", longitude));

        JSONObject json = jsonParser.getJSONFromUrl(serverURL,params);
        return json;
    }

    // Loads bitmap from network resource
    public static Bitmap loadImageFromNetwork(String url){
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
