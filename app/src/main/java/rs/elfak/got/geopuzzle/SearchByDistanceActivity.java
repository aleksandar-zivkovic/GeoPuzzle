package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;
import rs.elfak.got.geopuzzle.library.UserFunctions;

public class SearchByDistanceActivity extends AppCompatActivity {
    private GoogleMap map;
    SeekBar mDistanceSeek;
    ImageView mShowSeekImg;
    private HashMap<Marker, String> markerFriendEmailMap;
    private HashMap<Marker, String> markerChunkTitleMap;

    private ImageView markerImageView;
    private ImageView frameImageView;
    private View markerLayout;

    private boolean mDrawCircle = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_distance);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        markerLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        markerImageView = (ImageView) markerLayout.findViewById(R.id.markerImage);
        frameImageView = (ImageView) markerLayout.findViewById(R.id.frame);

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        try {
            map.setMyLocationEnabled(false);
        }
        catch (SecurityException e) {
            e.getMessage();
        }


        final TextView distanceTxt = (TextView) findViewById(R.id.distanceTxt);

        mDistanceSeek = (SeekBar) findViewById(R.id.distanceSeek);
        mDistanceSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceTxt.setText((progress + 500) + "m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateSeekBarValue(seekBar);
            }
        });
        updateSeekBarValue(mDistanceSeek);

        mShowSeekImg = (ImageView) findViewById(R.id.showSeekImg);
        mShowSeekImg.bringToFront();
        mShowSeekImg.setRotation(180);
        mShowSeekImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = (LinearLayout) findViewById(R.id.seekPartLayout);
                if(layout.getVisibility() == View.VISIBLE)
                {
                    layout.setVisibility(View.GONE);
                    mShowSeekImg.setRotation(0);
                    mShowSeekImg.bringToFront();
                }
                else
                {
                    layout.setVisibility(View.VISIBLE);
                    mShowSeekImg.setRotation(180);
                    mShowSeekImg.bringToFront();
                }
            }
        });
    }

    private void updateSeekBarValue(SeekBar seekBar)
    {
        final TextView distanceTxt = (TextView) findViewById(R.id.distanceTxt);

        map.clear();
        mDrawCircle = true;

        float distance = 0.0f;
        String distanceString = distanceTxt.getText().toString();
        String[] split = distanceString.split("m");
        distanceString = split[0];
        distance = Float.valueOf(distanceString);
        //  contact server here
        addFriendsMarkers(distance);
    }

    private void addFriendsMarkers(float distance) {

        ProcessMyFriends processMyFriends = new ProcessMyFriends();
        Object[] params = new Object[1];
        params[0] = distance;
        processMyFriends.execute(params);
    }

    private class ProcessMyFriends extends AsyncTask {
        private float distance;
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SearchByDistanceActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Fetching friends...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            distance = (float) params[0];
            return userFunction.fetchFriends(getApplicationContext());
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {
                    String res = json.getString(Cons.KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1) {
                        pDialog.setTitle(R.string.msg_getting_data);
                        pDialog.setMessage("Loading friends...");

                        int friendsNum = json.getInt(Cons.KEY_FRIENDS_NUM);
                        if(friendsNum == 0)
                            Toast.makeText(getApplicationContext(), R.string.msg_friends_list_empty, Toast.LENGTH_SHORT).show();
                        else {
                            markerFriendEmailMap = new HashMap<Marker, String>((int)((double) friendsNum * 1.2));
                            for(int i = 1; i <= friendsNum; i++) {
                                JSONObject friend = json.getJSONObject("friend" + i);
                                String latitude = friend.getString(Cons.KEY_LATITUDE);
                                String longitude = friend.getString(Cons.KEY_LONGITUDE);

                                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                                HashMap user = db.getUserDetails();
                                String mEmail = user.get(Cons.KEY_EMAIL).toString();

                                ProcessMyLocation processMyLocation = new ProcessMyLocation();
                                Object[] params = new Object[5];
                                params[0] = mEmail;
                                params[1] = latitude;
                                params[2] = longitude;
                                params[3] = distance;
                                params[4] = friend;
                                processMyLocation.execute(params);
                            }
                        }

                        pDialog.dismiss();
                    }
                    else {
                        pDialog.dismiss();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class DownloadImageTask extends AsyncTask {
        String friendName;
        String friendEmail;
        String friendLatitude;
        String friendLongitude;
        Bitmap friendImage;

        @Override
        protected Object doInBackground(Object[] params) {

            JSONObject friend = (JSONObject) params[0];

            try {
                friendName = friend.getString(Cons.KEY_FIRSTNAME) + " " + friend.getString(Cons.KEY_LASTNAME);
                friendEmail = friend.getString(Cons.KEY_EMAIL);
                friendLatitude = friend.getString(Cons.KEY_LATITUDE);
                friendLongitude = friend.getString(Cons.KEY_LONGITUDE);

                friendImage = (Bitmap) UserFunctions.loadImageFromNetwork(Cons.KEY_UPLOADS_URL + friendEmail + ".jpg");

                return friendImage;
            }
            catch (Exception e) {
                e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object object) {
            if(object != null) {
                Bitmap friendImage = (Bitmap) object;
                frameImageView.setImageResource(R.drawable.custom_marker_blue);
                LatLng location = new LatLng(Double.parseDouble(friendLatitude), Double.parseDouble(friendLongitude));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(location);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(friendImage, 65, 60, false);
                markerImageView.setImageBitmap(scaledBitmap);
                //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(SearchByDistanceActivity.this, markerLayout)));
                markerOptions.title(friendName);
                markerOptions.snippet(friendEmail);
                Marker marker = map.addMarker(markerOptions);
                markerFriendEmailMap.put(marker, friendEmail);
            }
        }
    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private class ProcessMyLocation extends AsyncTask {
        private ProgressDialog pDialog;
        String mEmail;
        String friendLatitude;
        String friendLongitude;
        float distance;
        Double myLatitude;
        Double myLongitude;
        JSONObject friend;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(SearchByDistanceActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Fetching my location...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            mEmail = (String) params[0];
            friendLatitude = (String) params[1];
            friendLongitude = (String) params[2];
            distance = (float) params[3];
            friend = (JSONObject) params[4];
            return userFunction.fetchUser(mEmail);
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {

                    String res = json.getString(Cons.KEY_SUCCESS);
                    if(Integer.parseInt(res) == 1) {
                        // success
                        JSONObject selectedUser = json.getJSONObject("user");
                        String latitude = selectedUser.getString("latitude");
                        String longitude = selectedUser.getString("longitude");

                        myLatitude = Double.valueOf(latitude);
                        myLongitude = Double.valueOf(longitude);

                        // it can be optimized to be called only once, but it is here because of myLatitude and myLongitude values
                        if(mDrawCircle)
                        {
                            Circle circle = map.addCircle(new CircleOptions()
                                    .center(new LatLng(myLatitude, myLongitude))
                                    .radius(distance)
                                    .strokeColor(Color.RED)
                                    .fillColor(0x330000FF));

                            mDrawCircle = false;

                            int zoom = distance <= 600 ? 15 : distance <= 1200 ? 14 : distance <= 2400 ? 13 : 12;

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(myLatitude, myLongitude))      // Sets the center of the map to Mountain View
                                    .zoom(zoom)                   // Sets the zoom
                                    //.bearing(90)                // Sets the orientation of the camera to east
                                    //.tilt(30)                   // Sets the tilt of the camera to 30 degrees
                                    .build();                   // Creates a CameraPosition from the builder
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }

                        Location myLocation = new Location("point A");
                        myLocation.setLatitude(myLatitude);
                        myLocation.setLongitude(myLongitude);

                        Location friendLocation = new Location("point B");
                        friendLocation.setLatitude(Double.parseDouble(friendLatitude));
                        friendLocation.setLongitude(Double.parseDouble(friendLongitude));

                        float dist = myLocation.distanceTo(friendLocation);

                        if (dist < distance) {
                            new DownloadImageTask().execute(friend);
                        }

                        pDialog.dismiss();
                    }
                    else {
                        // error
                        pDialog.dismiss();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}


