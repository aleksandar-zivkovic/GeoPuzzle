package rs.elfak.got.geopuzzle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.UserFunctions;

public class PuzzleSolvedActivity extends AppCompatActivity {

    String mPuzzleTitle;

    ImageView mSolvedPuzzleImageView;
    Button mBackToMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle_solved);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        mBackToMapButton = (Button) findViewById(R.id.backToMapBtn);
        mBackToMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent map = new Intent(view.getContext(), MapActivity.class);
                startActivity(map);
            }
        });

        if (getIntent().hasExtra("puzzleTitle")) {
            mPuzzleTitle = getIntent().getStringExtra("puzzleTitle");
        }
        else {
            mPuzzleTitle = "";
            Toast.makeText(this, "No puzzle title received!! ", Toast.LENGTH_SHORT).show();
        }

        mSolvedPuzzleImageView = (ImageView) findViewById(R.id.solvedPuzzleImage);

        if (!mPuzzleTitle.equalsIgnoreCase("")) {
            new DownloadImageTask().execute(mPuzzleTitle);
        }

    }

    private class DownloadImageTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return (Bitmap) UserFunctions.loadImageFromNetwork((String) params[0]);
        }

        @Override
        protected void onPostExecute(Object object) {
            if(object != null)
                mSolvedPuzzleImageView.setImageBitmap((Bitmap) object);
        }
    }
}