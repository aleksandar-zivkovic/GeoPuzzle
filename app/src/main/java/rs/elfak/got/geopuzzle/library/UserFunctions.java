package rs.elfak.got.geopuzzle.library;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

/**
 * Created by Milan on 14.5.2016..
 */
public class UserFunctions {
    private JSONParser jsonParser;

    //URL of the PHP API
    private static String loginURL = "http://vasic.ddns.net/geopuzzle_login_api/";
    private static String registerURL = "http://vasic.ddns.net/geopuzzle_login_api/";
    private static String forpassURL = "http://vasic.ddns.net/geopuzzle_login_api/";
    private static String chgpassURL = "http://vasic.ddns.net/geopuzzle_login_api/";
    private static String serverURL = "http://vasic.ddns.net/geopuzzle_login_api/";

    private static String login_tag = "login";
    private static String register_tag = "register";
    private static String forpass_tag = "forpass";
    private static String chgpass_tag = "chgpass";
    private static String fetchfriends_tag = "fetchfriends";
    private static String update_location_tag = "updatelocation";
    private static String addfriendship_tag = "addfriendship";

    // constructor
    public UserFunctions() {
        jsonParser = new JSONParser();
    }

    // Function to Login
    public JSONObject loginUser(String email, String password) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", login_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
        return json;
    }

    // Function to change password
    public JSONObject chgPass(String newpas, String email) {
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", chgpass_tag));

        params.add(new BasicNameValuePair("newpas", newpas));
        params.add(new BasicNameValuePair("email", email));
        JSONObject json = jsonParser.getJSONFromUrl(chgpassURL, params);
        return json;
    }

    // Function to reset the password
    public JSONObject forPass(String forgotpassword) {
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", forpass_tag));
        params.add(new BasicNameValuePair("forgotpassword", forgotpassword));
        JSONObject json = jsonParser.getJSONFromUrl(forpassURL, params);
        return json;
    }

    // Function to  Register
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


        JSONObject json = jsonParser.getJSONFromUrl(registerURL,params);
        return json;
    }

    // Function to logout user
    public boolean logoutUser(Context context) {
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }

    // Function to fetch friends
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

    // Function to update user location on server
    public JSONObject updateLocation(Context context, Location location) {
        DatabaseHandler db = new DatabaseHandler(context);
        HashMap user = db.getUserDetails();
        String email = user.get(Cons.KEY_EMAIL).toString();

        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", update_location_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("latitude", String.valueOf(location.getLatitude())));
        params.add(new BasicNameValuePair("longitude", String.valueOf(location.getLongitude())));

        JSONObject json = jsonParser.getJSONFromUrl(serverURL, params);
        return json;
    }

    // Function to add Friendship
    public JSONObject addFriendship(String email1, String email2) {
        // Building Parameters
        List params = new ArrayList();
        params.add(new BasicNameValuePair("tag", addfriendship_tag));
        params.add(new BasicNameValuePair("email1", email1));
        params.add(new BasicNameValuePair("email2", email2));


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
