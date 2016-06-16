package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;

import rs.elfak.got.geopuzzle.library.Cons;

/**
 * Created by aleksandar on 6/9/2016.
 */
public class PuzzleList extends ArrayAdapter<String> {

    private Activity context;
    private String[] titles;
    private String[] numOfChunks;

    public PuzzleList(Activity context, String[] titles, String[] numOfChunks) {
        super(context, R.layout.list_puzzle_item, titles);
        this.context = context;
        this.titles = titles;
        this.numOfChunks = numOfChunks;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_puzzle_item, null, true);

        TextView titleText = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);

        titleText.setText(titles[position]);
        Object[] params = new Object[2];
        params[0] = Cons.KEY_PUZZLES_URL + titles[position] + ".jpg";
        params[1] = imageView;
        new DownloadImageTask().execute(params);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titles[position];
                String numOfChunksString = numOfChunks[position];
                Intent intent = new Intent(view.getContext(), ChunkedImageActivity.class);
                intent.putExtra("puzzleTitle", title);
                intent.putExtra("numOfChunks", numOfChunksString);
                view.getContext().startActivity(intent);
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

}
