package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import rs.elfak.got.geopuzzle.library.Cons;

/**
 * Created by Aleksandar on 30.6.2016..
 */
public class GCMBroadcastReceiver extends WakefulBroadcastReceiver  {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private static final String TAG = "GcmIntentService";
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);

        this.context = context;

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        // The getMessageType() intent parameter must be the intent you received in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty() && extras.getString("title") != null) {  // has effect of unparcelling Bundle
            sendNotification(extras.getString("title"), extras.getString("message"));
        }
    }

    private void sendNotification(String title, String msg) {
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent;

        if (title.contains("Snap")) {

            String snapMessage = msg;
            // Message aleksandar@elfak.rs
            String[] strings = title.split(" ");
            String senderEmail = strings[1];

            Intent snapMessageIntent = new Intent(this.context, SnapMessageActivity.class);
            snapMessageIntent.putExtra("senderEmail", senderEmail);
            snapMessageIntent.putExtra("snapMessage", snapMessage);
            snapMessageIntent.setAction(Long.toString(System.currentTimeMillis()));
            contentIntent = PendingIntent.getActivity(context, 0, snapMessageIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        else if (title.contains("Chunk")) {
            String[] strings = msg.split(" ");
            String friendsEmail = strings[2];
            String sentPuzzleChunk = strings[5];

            Intent receivedPuzzleChunkIntent = new Intent(this.context, ReceivedPuzzleChunkActivity.class);
            receivedPuzzleChunkIntent.putExtra(Cons.KEY_EMAIL, friendsEmail);
            receivedPuzzleChunkIntent.putExtra("sentPuzzleChunk", sentPuzzleChunk);
            receivedPuzzleChunkIntent.setAction(Long.toString(System.currentTimeMillis()));
            contentIntent = PendingIntent.getActivity(context, 0, receivedPuzzleChunkIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        else {
            String[] strings = msg.split(" ");
            String friendsEmail = strings[2];
            Intent friendProfileIntent = new Intent(this.context, ProfileActivity.class);
            friendProfileIntent.putExtra(Cons.KEY_EMAIL, friendsEmail);
            friendProfileIntent.setAction(Long.toString(System.currentTimeMillis()));
            contentIntent = PendingIntent.getActivity(context, 0, friendProfileIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setContentTitle("GeoPuzzle: " + title)
                        .setSmallIcon(R.drawable.geopuzzle)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.geopuzzle))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg)
                        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
