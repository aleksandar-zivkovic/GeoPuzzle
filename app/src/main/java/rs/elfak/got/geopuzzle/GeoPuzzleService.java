package rs.elfak.got.geopuzzle;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.io.IOException;
import android.widget.Toast;

import java.io.Console;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import rs.elfak.got.geopuzzle.library.UserFunctions;

/**
 * Created by Aleksandar on 24.5.2016..
 */
public class GeoPuzzleService extends Service {
    private static final int LOCATION_REFRESH_TIME = 5; //5*60*1000; // 5 mins
    private static final float LOCATION_REFRESH_DISTANCE = 5; // in meters
    private Location loc;

    public static final int STOPPED = 0;
    public static final int RUNNING = 1;
    public static int mState;

    public static final long UPDATE_LOCATION_INTERVAL = 30 * 1000; // 30 seconds

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    private Handler mLocationHandler = new Handler();
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            loc = location;
            new UpdateLocation().execute();

            /*NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            int icon = R.mipmap.logo;
            CharSequence notiText = "Your notification from the service";
            long meow = System.currentTimeMillis();

            Notification notification = new Notification(icon, notiText, meow);
            //notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;

            Context context = getApplicationContext();
            CharSequence contentTitle = "GeoPuzzle";
            CharSequence contentText = "Location changed...";
            Intent notificationIntent = new Intent(getApplicationContext(), HomeActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(contentTitle)
                    .setAutoCancel(true);
            notification = builder.build();


            int SERVER_DATA_RECEIVED = 1;
            notificationManager.notify(SERVER_DATA_RECEIVED, notification);*/
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    private static LocationManager mLocationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationHandler.post(new Runnable() {
            @Override
            public void run() {
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                try {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
                }
                catch(SecurityException e) {
                    e.getMessage(); // user did not give permission
                }

            }
        });

//        // cancel if already existed
//        if (mTimer != null) {
//            mTimer.cancel();
//        }
//        else {
//            // recreate new
//            mTimer = new Timer();
//        }
//        // schedule task
//        mTimer.scheduleAtFixedRate(new UpdateLocation(), 0, UPDATE_LOCATION_INTERVAL);


    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        mState = RUNNING;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mTimer != null)
            mTimer.cancel();
        mState = STOPPED;
    }

    class UpdateLocation extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            //check whether internet connection is working
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        // internet connection is ok, send location to server
                        Log.d("Location update", "Success!!");
                        new UserFunctions().updateLocation(getApplicationContext(), loc);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

//    class UpdateLocation extends TimerTask {
//        @Override
//        public void run() {
//            // run on another thread
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    // check whether internet connection is working
//                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
//                    if (netInfo != null && netInfo.isConnected()) {
//                        try {
//                            URL url = new URL("http://www.google.com");
//                            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
//                            urlc.setConnectTimeout(3000);
//                            urlc.connect();
//                            if (urlc.getResponseCode() == 200) {
//                                // internet connection is ok, send location to server
//                                new UserFunctions().updateLocation()
//                            }
//                        }
//                        catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });
//        }
//    }
}
