package rs.elfak.got.geopuzzle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.UserFunctions;

public class ChunkActivity extends AppCompatActivity {

    private String mPuzzleChunkTitle;
    private String mPuzzleChunkFullPath;

    private Button mShareChunkButton;
    private ImageView mImageView;
    private TextView mPuzzleTitleTextView;
    private TextView mPuzzleChunkTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chunk);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        mShareChunkButton = (Button) findViewById(R.id.shareWithFriendsBtn);
        mImageView = (ImageView) findViewById(R.id.puzzleChunkImage);
        mPuzzleTitleTextView = (TextView) findViewById(R.id.puzzleTitle);
        mPuzzleChunkTitleTextView = (TextView) findViewById(R.id.puzzleChunkTitle);

        mPuzzleChunkTitle = "";

        Intent chunkIntent = getIntent();
        Bundle chunkBundle = chunkIntent.getExtras();
        if(chunkBundle != null) {
            if (chunkBundle.getString("puzzleChunkTitle") != null) {
                mPuzzleChunkTitle = chunkBundle.getString("puzzleChunkTitle");
                mPuzzleChunkTitleTextView.setText(mPuzzleChunkTitle);
            }


        }

        if (!mPuzzleChunkTitle.equalsIgnoreCase("")) {
            mPuzzleChunkFullPath = Cons.KEY_CHUNKS_URL + mPuzzleChunkTitle + ".jpg";
            new DownloadImageTask().execute(mPuzzleChunkFullPath);
        }

        mShareChunkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myFriendsIntent = new Intent(view.getContext(), MyFriendsActivity.class);
                myFriendsIntent.putExtra("puzzleChunkTitle", mPuzzleChunkTitle);
                startActivity(myFriendsIntent);
            }
        });
    }

    private class DownloadImageTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return (Bitmap) UserFunctions.loadImageFromNetwork((String) params[0]);
        }

        @Override
        protected void onPostExecute(Object object) {
            if(object != null)
                mImageView.setImageBitmap((Bitmap) object);
        }
    }
}
