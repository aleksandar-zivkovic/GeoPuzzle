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
import java.net.URL;

import rs.elfak.got.geopuzzle.library.Cons;

/**
 * Created by aleksandar on 7/7/2016.
 */
public class PuzzleChunkList extends ArrayAdapter<String> {

    private Activity context;
    private String[] titles;
    private String titleToSet;

    public PuzzleChunkList(Activity context, String[] titles) {
        super(context, R.layout.list_puzzle_chunk_item, titles);
        this.context = context;
        this.titles = titles;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_puzzle_chunk_item, null, true);

        TextView titleText = (TextView) rowView.findViewById(R.id.txt);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);


        String title = titles[position]; //http://vasic.ddns.net/geopuzzle_login_api/chunks/automobil_4.jpg
        String[] split = title.split("/chunks/");
        titleToSet = split[1]; //automobil_4.jpg
        titleToSet = titleToSet.replace(".jpg", "");
        titleText.setText(titleToSet);

        Object[] params = new Object[2];
        params[0] = titles[position];
        params[1] = imageView;
        new DownloadImageTask().execute(params);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titles[position];
                String[] split = title.split("/chunks/");
                String titleToSend = split[1]; //automobil_4.jpg
                titleToSend = titleToSend.replace(".jpg", "");
                Intent intent = new Intent(view.getContext(), ChunkActivity.class);
                intent.putExtra("puzzleChunkTitle", titleToSend);
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
