package rs.elfak.got.geopuzzle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import rs.elfak.got.geopuzzle.library.*;

/**
 * Created by Aleksandar on 22.5.2016..
 */
public class FriendList extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] names;
    private final String[] emails;
    private String mPuzzleChunkToSend = "";

    public FriendList(Activity context, String[] names, String[] emails) {
        super(context, R.layout.list_friend_item, names);
        this.context = context;
        this.names = names;
        this.emails = emails;
    }

    public FriendList(Activity context, String[] names, String[] emails, String puzzleChunkToSend) {
        super(context, R.layout.list_friend_item, names);
        this.context = context;
        this.names = names;
        this.emails = emails;
        mPuzzleChunkToSend = puzzleChunkToSend;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_friend_item, null, true);

        TextView nameText = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);

        nameText.setText(names[position]);
        Object[] params = new Object[2];
        params[0] = Cons.KEY_UPLOADS_URL + emails[position] + ".jpg";
        params[1] = imageView;
        new DownloadImageTask().execute(params);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPuzzleChunkToSend == "") {
                    Intent friendProfile = new Intent(view.getContext(), ProfileActivity.class);
                    friendProfile.putExtra(Cons.KEY_FULLNAME, names[position]);
                    friendProfile.putExtra(Cons.KEY_EMAIL, emails[position]);
                    view.getContext().startActivity(friendProfile);
                }
                else {
                    Toast.makeText(context, "Puzzle chunk " + mPuzzleChunkToSend + " sent to this friend!", Toast.LENGTH_SHORT);
                    // send puzzle chunk to friend -> user functions -> server -> database
                    String receiverEmail = emails[position];
                    SendPuzzleChunk sendPuzzleChunk = new SendPuzzleChunk();
                    Object[] params = new Object[2];
                    params[0] = receiverEmail;
                    params[1] = mPuzzleChunkToSend;
                    sendPuzzleChunk.execute(params);
                }

            }
        });

        return rowView;
    }

    private class DownloadImageTask extends AsyncTask {
        ImageView imageView;

        @Override
        protected Object doInBackground(Object[] params) {
            imageView = (ImageView) params[1];
            return (Bitmap) loadImageFromNetwork((String) params[0]);
        }

        @Override
        protected void onPostExecute(Object o) {
            final Bitmap image = (Bitmap)o;
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(image != null)
                        imageView.setImageBitmap((Bitmap) image);
                }
            });
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

    private class SendPuzzleChunk extends AsyncTask {
        private ProgressDialog pDialog;
        String receiverEmail;
        String puzzleChunkTitle;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(context);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Sending puzzle chunk...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            receiverEmail = (String) params[0];
            puzzleChunkTitle = (String) params[1];
            return userFunction.sendPuzzleChunk(context, receiverEmail, puzzleChunkTitle);
        }

        @Override
        protected void onPostExecute(Object o) {
            pDialog.dismiss();
        }
    }
}