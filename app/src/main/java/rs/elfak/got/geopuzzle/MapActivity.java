package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;
import rs.elfak.got.geopuzzle.library.UserFunctions;

public class MapActivity extends AppCompatActivity {
    public static final int SHOW_MAP = 0;
    public static final int CENTER_PLACE_ON_MAP = 1;
    public static final int SELECT_COORDINATES = 2;
    public static final int PLACE_CHUNK_ON_MAP = 3;

    private int state = 0;
    private boolean selCoordsEnabled = false;
    private boolean chunkToPlaceEnabled = false;
    private LatLng placeLoc;

    private GoogleMap map;
    private HashMap<Marker, String> markerFriendEmailMap;
    private HashMap<Marker, String> markerChunkTitleMap;
    private String puzzleTitle;
    private String clickedPuzzleTitle;
    private Bitmap imageChunk;
    private Bitmap imageChunkLoaded;

    private Double myLatitude = 0.0;
    private Double myLongitude = 0.0;

    private String mPuzzleChunkLat;
    private String mPuzzleChunkLon;
    private Marker mMarker;


    private GoogleMap mMap;
    private Marker customMarker;
    private LatLng markerLatLng;

    private ImageView markerImageView;
    private ImageView frameImageView;
    private View markerLayout;
    private Button mPlaceChunkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        markerLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        markerImageView = (ImageView) markerLayout.findViewById(R.id.markerImage);
        frameImageView = (ImageView) markerLayout.findViewById(R.id.frame);

        mPlaceChunkButton = (Button) findViewById(R.id.placeChunkButton);
        mPlaceChunkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                chunkToPlaceEnabled = false;
                // add chunk coordinates to database
                markerChunkTitleMap.put(mMarker, puzzleTitle);

                mPlaceChunkButton.setVisibility(View.INVISIBLE);

                ProcessPuzzleChunkImageUpload processPuzzleChunkImageUpload = new ProcessPuzzleChunkImageUpload();
                Object[] params = new Object[3];
                params[0] = puzzleTitle;
                params[1] = mPuzzleChunkLat;
                params[2] = mPuzzleChunkLon;
                processPuzzleChunkImageUpload.execute(params);

            }
        });

        try {
            Intent mapIntent = getIntent();
            Bundle mapBundle = mapIntent.getExtras();
            if(mapBundle != null) {
                state = mapBundle.getInt("state");
                if(state == CENTER_PLACE_ON_MAP) {
                    String placeLat = mapBundle.getString("lat");
                    String placeLon = mapBundle.getString("lon");
                    placeLoc = new LatLng(Double.parseDouble(placeLat), Double.parseDouble(placeLon));
                }
            }
        }
        catch (Exception e) {
            Log.d("Error", "Error reading state");
        }

        // get puzzle piece to put on map!
        if (getIntent().hasExtra("puzzleTitle")) {
            puzzleTitle = getIntent().getStringExtra("puzzleTitle");

            // get puzzle chunk from database
            DownloadChunkImageTask downloadChunkImageTask = new DownloadChunkImageTask();
            Object[] params = new Object[1];
            params[0] = puzzleTitle;
            downloadChunkImageTask.execute(params);

            state = PLACE_CHUNK_ON_MAP;
            chunkToPlaceEnabled = true;
            Toast.makeText(this, "Tap on map to place puzzle! ", Toast.LENGTH_SHORT).show();
        }

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        Location location = null;
        try {
            location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        }
        catch (SecurityException e) {
            e.getMessage();
        }

        if (location != null)
        {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 15));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

        if(state == SHOW_MAP) {
            try {
                map.setMyLocationEnabled(true);
            }
            catch (SecurityException e) {
                e.getMessage(); // there is a problem with the gps - permission not given by user
            }
        }
        else if(state == SELECT_COORDINATES) {
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (state == SELECT_COORDINATES && selCoordsEnabled) {

                        String lat = Double.toString(latLng.latitude);
                        String lon = Double.toString(latLng.longitude);

                        Intent locationIntent = new Intent();
                        locationIntent.putExtra("lat", lat);
                        locationIntent.putExtra("lon", lon);
                        setResult(Activity.RESULT_OK, locationIntent);
                        finish();
                    }
                }
            });
        }
        else if(state == PLACE_CHUNK_ON_MAP) {
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (state == PLACE_CHUNK_ON_MAP && chunkToPlaceEnabled) {

                        if (mMarker != null) {
                            mMarker.remove();
                        }

                        mPlaceChunkButton.setVisibility(View.VISIBLE);

                        mPuzzleChunkLat = Double.toString(latLng.latitude);
                        mPuzzleChunkLon = Double.toString(latLng.longitude);

                        frameImageView.setImageResource(R.drawable.custom_marker);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageChunk, 65, 60, false);

                        markerImageView.setImageBitmap(scaledBitmap);
                        
                        //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(MapActivity.this, markerLayout)));
                        markerOptions.title("Puzzle");
                        markerOptions.snippet("Collect me!");

                        mMarker = map.addMarker(markerOptions);
                    }
                }
            });
        }
        else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc, 15));
        }


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                // check if friend or puzzle chunk is clicked
                if (marker.getTitle().equalsIgnoreCase("Puzzle")) {

                    clickedPuzzleTitle = markerChunkTitleMap.get(marker);
                    ProcessMyPuzzleChunk processMyPuzzleChunk = new ProcessMyPuzzleChunk();
                    Object[] params = new Object[1];
                    params[0] = clickedPuzzleTitle;
                    processMyPuzzleChunk.execute(params);
                }
                else {
                    Intent friendProfile = new Intent(getApplicationContext(), ProfileActivity.class);
                    friendProfile.putExtra(Cons.KEY_FULLNAME, marker.getTitle());
                    String email = markerFriendEmailMap.get(marker);
                    friendProfile.putExtra(Cons.KEY_EMAIL, email);
                    startActivity(friendProfile);
                }

                return true;
            }
        });

        // show friends and puzzles on map create
        addFriendsMarkers();
        addPuzzleMarkers();
    }

    private void addFriendsMarkers() {

        ProcessMyFriends processMyFriends = new ProcessMyFriends();
        processMyFriends.execute();
    }

    private void addPuzzleMarkers() {

        ProcessMyPuzzles processMyPuzzles = new ProcessMyPuzzles();
        processMyPuzzles.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //getMenuInflater().inflate(R.menu.menu_my_places_map,menu);
        //return true;

        if(state == SELECT_COORDINATES && !selCoordsEnabled) {
            menu.add(0, 1, 1, "Select Coordinates");
            menu.add(0, 2, 2, "Cancel");
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case 1:
                selCoordsEnabled = true;
                item.setEnabled(false);
                Toast.makeText(this, "Select coordinates", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ProcessMyFriends extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MapActivity.this);
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
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(MapActivity.this, markerLayout)));
                markerOptions.title(friendName);
                markerOptions.snippet(friendEmail);
                Marker marker = map.addMarker(markerOptions);
                markerFriendEmailMap.put(marker, friendEmail);
            }
        }
    }

    private class DownloadChunkImageTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return (Bitmap) loadImageFromNetwork((String) params[0]);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            imageChunk = (Bitmap)o;

        }

        private Bitmap loadImageFromNetwork(String url){
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

    private class ProcessPuzzleChunkImageUpload extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MapActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Uploading puzzle image chunk...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            String title = (String) params[0];
            String latitude = (String) params[1];
            String longitude = (String) params[2];

            return userFunction.uploadPuzzleChunk(title, latitude, longitude);
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {
                    String res = json.getString(Cons.KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1) {
                        pDialog.setTitle(R.string.msg_getting_data);
                        pDialog.setMessage("Puzzle chunk successfully uploaded!");
                        pDialog.dismiss();
                    }
                    else {
                        pDialog.dismiss();
                        //Toast.makeText(getApplicationContext(), "Problem while uploading puzzle chunk", Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Problem while uploading puzzle chunk - It is already on map!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ProcessMyPuzzles extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MapActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Fetching puzzles...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            return userFunction.fetchPuzzleChunks();
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {
                    String res = json.getString(Cons.KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1) {
                        pDialog.setTitle(R.string.msg_getting_data);
                        pDialog.setMessage("Loading puzzle chunks...");

                        int puzzleNum = json.getInt(Cons.KEY_PUZZLE_CHUNK_NUM);
                        if(puzzleNum == 0)
                            Toast.makeText(getApplicationContext(), R.string.msg_puzzle_chunks_list_empty, Toast.LENGTH_SHORT).show();
                        else {
                            markerChunkTitleMap = new HashMap<Marker, String>((int)((double) puzzleNum * 1.2));
                            for(int i = 1; i <= puzzleNum; i++) {
                                JSONObject puzzleChunk = json.getJSONObject("puzzleChunk" + i);

                                String title = (String) puzzleChunk.getString("title");
                                String lat = (String) puzzleChunk.getString("latitude");
                                String lon = (String) puzzleChunk.getString("longitude");

                                // get image from server with this title - get puzzle chunk from database
                                DownloadChunkImageTaskLoaded downloadChunkImageTaskLoaded = new DownloadChunkImageTaskLoaded();
                                Object[] params = new Object[3];
                                params[0] = title;
                                params[1] = lat;
                                params[2] = lon;
                                downloadChunkImageTaskLoaded.execute(params);
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

    private class DownloadChunkImageTaskLoaded extends AsyncTask {

        String title;
        String latitude;
        String longitude;

        @Override
        protected Object doInBackground(Object[] params) {

            title = (String) params[0];
            latitude = (String) params[1];
            longitude = (String) params[2];

            return (Bitmap) loadImageFromNetwork(title);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            imageChunkLoaded = (Bitmap)o;
            frameImageView.setImageResource(R.drawable.custom_marker);
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)));
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(imageChunkLoaded, 65, 60, false);
            markerImageView.setImageBitmap(scaledBitmap);
            //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(MapActivity.this, markerLayout)));
            markerOptions.title("Puzzle");
            markerOptions.snippet("Collect me!");
            Marker marker = map.addMarker(markerOptions);
            markerChunkTitleMap.put(marker, title);

        }

        private Bitmap loadImageFromNetwork(String url){
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

    private class ProcessMyPuzzleChunk extends AsyncTask {
        private ProgressDialog pDialog;
        String title;
        String mEmail;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MapActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Fetching puzzle chunk...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {

            UserFunctions userFunction = new UserFunctions();
            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
            HashMap user = db.getUserDetails();
            mEmail = user.get(Cons.KEY_EMAIL).toString();
            title = (String) params[0];
            return userFunction.fetchPuzzleChunk(title);
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {

                    String res = json.getString(Cons.KEY_SUCCESS);
                    if(Integer.parseInt(res) == 1) {
                        // success -> there is puzzle in database

                        JSONObject selectedPuzzleChunk = json.getJSONObject("puzzleChunk");
                        String title = selectedPuzzleChunk.getString("title");
                        String latitude = selectedPuzzleChunk.getString("latitude");
                        String longitude = selectedPuzzleChunk.getString("longitude");

                        // get my location from database
                        ProcessMyLocation processMyLocation = new ProcessMyLocation();
                        Object[] params = new Object[3];
                        params[0] = mEmail;
                        params[1] = latitude;
                        params[2] = longitude;
                        processMyLocation.execute(params);

                        pDialog.dismiss();
                    }
                    else {
                        // error -> there is no puzzle -> this should not happen
                        pDialog.dismiss();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ProcessMyLocation extends AsyncTask {
        private ProgressDialog pDialog;
        String email;
        String chunkLatitude;
        String chunkLongitude;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MapActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Fetching my location...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            email = (String) params[0];
            chunkLatitude = (String) params[1];
            chunkLongitude = (String) params[2];
            return userFunction.fetchUser(email);
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
                        String email = selectedUser.getString("email");
                        String latitude = selectedUser.getString("latitude");
                        String longitude = selectedUser.getString("longitude");

                        myLatitude = Double.valueOf(latitude);
                        myLongitude = Double.valueOf(longitude);

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(myLatitude, myLongitude))      // Sets the center of the map to Mountain View
                                .zoom(13)                   // Sets the zoom
                                .build();                   // Creates a CameraPosition from the builder
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        // check if you are near the puzzle --> if close: collect puzzle, else : toast -> you cannot collect that puzzle, you are not nearby
                        Location chunkLocation = new Location("point A");
                        chunkLocation.setLatitude(Double.parseDouble(chunkLatitude));
                        chunkLocation.setLongitude(Double.parseDouble(chunkLongitude));

                        Location myLocation = new Location("point B");
                        myLocation.setLatitude(myLatitude);
                        myLocation.setLongitude(myLongitude);

                        Float distance = chunkLocation.distanceTo(myLocation);

                        if (distance < 5000) {


                            String[] split1 = clickedPuzzleTitle.split("/chunks/"); //http://vasic.ddns.net/geopuzzle_login_api/chunks/desert_3.jpg
                            String[] split2 = split1[1].split("_"); //desert_3.jpg
                            // split2[0] == desert
                            String title = "http://vasic.ddns.net/geopuzzle_login_api/puzzles/" + split2[0] + ".jpg";

                            // insert into database -> email, puzzle title, chunk
                            CollectPuzzle collectPuzzle = new CollectPuzzle();
                            Object[] params = new Object[3];
                            params[0] = email; // email of user who fetched the chunk
                            params[1] = title; // puzzle title for easier search
                            params[2] = clickedPuzzleTitle; // puzzle chunk title
                            collectPuzzle.execute(params);
                            //Toast.makeText(getApplicationContext(), "You have collected the puzzle!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "You are too far away to collect that puzzle!", Toast.LENGTH_SHORT).show();
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

    private class CollectPuzzle extends AsyncTask {
        private ProgressDialog pDialog;
        String email;
        String puzzleTitle;
        String puzzleChunkTitle;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MapActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Seding collected chunk to server...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            email = (String) params[0];
            puzzleTitle = (String) params[1];
            puzzleChunkTitle = (String) params[2];
            return userFunction.collectPuzzleChunk(email, puzzleTitle, puzzleChunkTitle);
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {

                    String res = json.getString(Cons.KEY_SUCCESS);
                    if(Integer.parseInt(res) == 1) {
                        // success

                        int solved = json.getInt("solved");
                        if (solved == 1) {
                            // puzzle with puzzleTitle is solved!
                            Toast.makeText(getApplicationContext(),"You have solved " + puzzleTitle + " puzzle!", Toast.LENGTH_SHORT).show();

                            // increment puzzles solved
                            ProcessPuzzleSolved processPuzzleSolved = new ProcessPuzzleSolved();
                            Object[] params = new Object[1];
                            params[0] = email;
                            processPuzzleSolved.execute(params);

                            // start an activity (fragment in future?) which contains puzzle image, congratulations message and button to return back to map
                            Intent puzzleSolvedIntent = new Intent(getApplicationContext(), PuzzleSolvedActivity.class);
                            puzzleSolvedIntent.putExtra("puzzleTitle", puzzleTitle);
                            startActivity(puzzleSolvedIntent);

                        }
                        pDialog.dismiss();
                    }
                    else {
                        // error

                        int alreadyCollected = json.getInt("alreadyCollected");
                        if (alreadyCollected == 1) {
                            // puzzle with puzzleTitle is already collected by  you!
                            Toast.makeText(getApplicationContext(),"You have already collected " + puzzleTitle + " puzzle!", Toast.LENGTH_SHORT).show();
                        }

                        pDialog.dismiss();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ProcessPuzzleSolved extends AsyncTask {
        private ProgressDialog pDialog;
        String email;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MapActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Incrementing puzzles solved...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            email = (String) params[0];
            return userFunction.updateUserPuzzleSolved(email);
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {

                    String res = json.getString(Cons.KEY_SUCCESS);
                    if(Integer.parseInt(res) == 1) {
                        // success
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
