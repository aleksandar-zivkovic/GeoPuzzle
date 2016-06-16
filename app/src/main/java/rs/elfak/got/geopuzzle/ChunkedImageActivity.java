package rs.elfak.got.geopuzzle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.ImageAdapter;
import rs.elfak.got.geopuzzle.library.UserFunctions;

public class ChunkedImageActivity extends AppCompatActivity {

    String numOfChunksString;
    int numOfChunks;
    String puzzleTitle;
    Bitmap mChunkedBitmap;
    ArrayList<Bitmap> mImageChunks;
    ImageAdapter imageAdapter;

    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chunked_image);

        // Getting the image chunks sent from the previous activity
        puzzleTitle = getIntent().getStringExtra("puzzleTitle");

        // Getting the image chunks sent from the previous activity
        numOfChunksString = getIntent().getStringExtra("numOfChunks");
        numOfChunks = Integer.decode(numOfChunksString);

        mImageChunks = new ArrayList<Bitmap>();
        imageAdapter = new ImageAdapter(this, mImageChunks);
        // get imageChunks from Database
        for (int i=0; i< numOfChunks; i++) {

            Object[] params = new Object[1];
            params[0] = Cons.KEY_CHUNKS_URL + puzzleTitle + "_" + String.valueOf(i) + ".jpg";
            new DownloadChunkImageTask().execute(params);
        }

        //Getting the grid view and setting an adapter to it
        gridView = (GridView) findViewById(R.id.gridview);
        if (numOfChunks == 2) {
            gridView.setNumColumns(2);
        }
        else {
            gridView.setNumColumns((int) Math.sqrt(numOfChunks));
        }
        gridView.setAdapter(imageAdapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String title = Cons.KEY_CHUNKS_URL + puzzleTitle + "_" + position + ".jpg";

                // Check if puzzle chunk is already put on map
                Bitmap chunk;

                ProcessMyPuzzleChunk processMyPuzzleChunk = new ProcessMyPuzzleChunk();
                Object[] params = new Object[1];
                params[0] = title;
                processMyPuzzleChunk.execute(params);

            }
        });

    }

    private class DownloadChunkImageTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return (Bitmap) loadImageFromNetwork((String) params[0]);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            mChunkedBitmap = (Bitmap)o;
            // after this -> each Chunked Bitmap is written into mChunkedBitmap
            mImageChunks.add(mChunkedBitmap);
            imageAdapter.notifyDataSetChanged();

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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ChunkedImageActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Fetching puzzle chunk...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
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

                        // do not start map activity
                        Toast.makeText(getApplicationContext(), "That puzzle chunk is already on map!", Toast.LENGTH_SHORT).show();

                        pDialog.dismiss();
                    }
                    else {
                        // error -> there is no puzzle
                        pDialog.dismiss();

                        // start map activity
                        Intent intent = new Intent(ChunkedImageActivity.this, MapActivity.class);
                        intent.putExtra("puzzleTitle", title);
                        startActivity(intent);
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
