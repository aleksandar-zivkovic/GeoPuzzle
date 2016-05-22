package rs.elfak.got.geopuzzle;

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

import java.io.InputStream;
import java.net.URL;

import rs.elfak.got.geopuzzle.library.*;

/**
 * Created by Milan on 22.5.2016..
 */
public class FriendList extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] names;
    private final String[] emails;

    public FriendList(Activity context, String[] names, String[] emails) {
        super(context, R.layout.list_friend_item, names);
        this.context = context;
        this.names = names;
        this.emails = emails;
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
                Intent friendProfile = new Intent(view.getContext(), ProfileActivity.class);
                friendProfile.putExtra(Cons.KEY_FULLNAME, names[position]);
                friendProfile.putExtra(Cons.KEY_EMAIL, emails[position]);
                view.getContext().startActivity(friendProfile);
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
            imageView.setImageBitmap((Bitmap) o);
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
}