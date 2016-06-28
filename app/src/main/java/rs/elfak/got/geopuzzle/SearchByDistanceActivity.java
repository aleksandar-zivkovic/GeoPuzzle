package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import rs.elfak.got.geopuzzle.library.Cons;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_by_distance);

        markerLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        markerImageView = (ImageView) markerLayout.findViewById(R.id.markerImage);
        frameImageView = (ImageView) markerLayout.findViewById(R.id.frame);

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);

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
                //  contact server here
                addFriendsMarkers();
            }
        });

        mShowSeekImg = (ImageView) findViewById(R.id.showSeekImg);
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

    private void addFriendsMarkers() {

        ProcessMyFriends processMyFriends = new ProcessMyFriends();
        processMyFriends.execute();
    }

    private class ProcessMyFriends extends AsyncTask {
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

                                new DownloadImageTask().execute(friend);
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
}
