package rs.elfak.got.geopuzzle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;
import rs.elfak.got.geopuzzle.library.UserFunctions;

public class ReceivedPuzzleChunkActivity extends AppCompatActivity {

    private String mPuzzleChunkTitle;
    private String mPuzzleChunkFullPath;
    private String mFriendsEmail;

    private Button mAcceptButton;
    private Button mDeclineButton;
    private ImageView mImageView;
    private TextView mPuzzleTitleTextView;
    private TextView mPuzzleChunkTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_puzzle_chunk);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        mAcceptButton = (Button) findViewById(R.id.acceptBtn);
        mDeclineButton = (Button) findViewById(R.id.declineBtn);
        mImageView = (ImageView) findViewById(R.id.puzzleChunkImage);
        mPuzzleTitleTextView = (TextView) findViewById(R.id.puzzleTitle);
        mPuzzleChunkTitleTextView = (TextView) findViewById(R.id.puzzleChunkTitle);

        mFriendsEmail = "";
        mPuzzleChunkTitle = "";

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            if (bundle.getString("email") != null) {
                mFriendsEmail = bundle.getString("email");
            }
            if (bundle.getString("sentPuzzleChunk") != null) {
                mPuzzleChunkTitle = bundle.getString("sentPuzzleChunk");

            }
            mPuzzleChunkTitleTextView.setText("Your friend " + mFriendsEmail + " sent you " + mPuzzleChunkTitle + " puzzle chunk.");
        }

        if (!mPuzzleChunkTitle.equalsIgnoreCase("")) {
            mPuzzleChunkFullPath = Cons.KEY_CHUNKS_URL + mPuzzleChunkTitle + ".jpg";
            new DownloadImageTask().execute(mPuzzleChunkFullPath);
        }

        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // accept
                String[] split = mPuzzleChunkFullPath.split("/chunks/"); //http://vasic.ddns.net/geopuzzle_login_api/chunks/automobil_4.jpg
                String mPuzzleTitle1 = split[1]; // automobil_4.jpg
                split = mPuzzleTitle1.split("_");
                String mPuzzleTitle = split[0]; // automobil
                mPuzzleTitle = Cons.KEY_PUZZLES_URL + mPuzzleTitle + ".jpg";

                DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                HashMap user = db.getUserDetails();
                String email = user.get(Cons.KEY_EMAIL).toString();

                CollectPuzzleChunk collectPuzzleChunk = new CollectPuzzleChunk();
                Object[] params = new Object[3];
                params[0] = email;
                params[1] = mPuzzleTitle;
                params[2] = mPuzzleChunkFullPath;
                collectPuzzleChunk.execute(params); // redirects to Solved or MyChunks


            }
        });

        mDeclineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // decline -> dissmiss
                Toast.makeText(getApplicationContext(), "You have declined this puzzle chunk!", Toast.LENGTH_LONG).show();
                Intent homeActivityIntent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(homeActivityIntent);
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

    private class CollectPuzzleChunk extends AsyncTask {
        private ProgressDialog pDialog;
        String email;
        String puzzleTitle;
        String puzzleChunkTitle;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ReceivedPuzzleChunkActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Sending collected chunk to server...");
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
                        else {
                            Toast.makeText(getApplicationContext(),"You have collected " + puzzleChunkTitle + " puzzle chunk!", Toast.LENGTH_SHORT).show();
                            Intent myPuzzleChunksIntent = new Intent(getApplicationContext(), MyPuzzleChunksActivity.class);
                            startActivity(myPuzzleChunksIntent);
                        }
                        pDialog.dismiss();
                    }
                    else {
                        // error

                        int alreadyCollected = json.getInt("alreadyCollected");
                        if (alreadyCollected == 1) {
                            // puzzle with puzzleTitle is already collected by  you!
                            Toast.makeText(getApplicationContext(),"You already have " + puzzleChunkTitle + " puzzle chunk!", Toast.LENGTH_SHORT).show();
                            Intent myPuzzleChunksIntent = new Intent(getApplicationContext(), MyPuzzleChunksActivity.class);
                            startActivity(myPuzzleChunksIntent);
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

            pDialog = new ProgressDialog(ReceivedPuzzleChunkActivity.this);
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
}
