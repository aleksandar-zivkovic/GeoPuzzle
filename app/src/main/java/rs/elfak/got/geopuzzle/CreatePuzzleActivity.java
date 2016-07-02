package rs.elfak.got.geopuzzle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;
import rs.elfak.got.geopuzzle.library.HttpFileUpload;
import rs.elfak.got.geopuzzle.library.UserFunctions;

import org.apache.commons.lang3.StringUtils;

public class CreatePuzzleActivity extends AppCompatActivity {

    String mFileName;
    String mImagePath;
    String mEmail;
    HashMap mUser;
    File mFile;

    String puzzleName;
    Bitmap puzzleBitmap;
    Bitmap scaledPuzzleBitmap;
    int numberOfPieces;
    ArrayList<Bitmap> chunkedImages;

    ImageView uploadPuzzleImage;
    Button publishPuzzleBtn;
    EditText firstNameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_puzzle);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        mUser = db.getUserDetails();
        mEmail = (String) mUser.get(Cons.KEY_EMAIL);;

        uploadPuzzleImage = (ImageView) findViewById(R.id.uploadPuzzleImage);
        publishPuzzleBtn = (Button) findViewById(R.id.publishPuzzleBtn);
        firstNameEdit = (EditText) findViewById(R.id.firstNameEdit);

        numberOfPieces = 0;

        final ImageView pattern22Img = (ImageView) findViewById(R.id.pattern22Img);
        final ImageView pattern22ChkImg = (ImageView) findViewById(R.id.pattern22ChkImg);
        final ImageView pattern21Img = (ImageView) findViewById(R.id.pattern21Img);
        final ImageView pattern21ChkImg = (ImageView) findViewById(R.id.pattern21ChkImg);
        final ImageView pattern33Img = (ImageView) findViewById(R.id.pattern33Img);
        final ImageView pattern33ChkImg = (ImageView) findViewById(R.id.pattern33ChkImg);

        pattern21Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pattern21ChkImg.setVisibility(View.VISIBLE);
                pattern22ChkImg.setVisibility(View.INVISIBLE);
                pattern33ChkImg.setVisibility(View.INVISIBLE);
                numberOfPieces = 2;
                Toast.makeText(getApplicationContext(), "Chunks: 2", Toast.LENGTH_SHORT).show();
            }
        });

        pattern22Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pattern21ChkImg.setVisibility(View.INVISIBLE);
                pattern22ChkImg.setVisibility(View.VISIBLE);
                pattern33ChkImg.setVisibility(View.INVISIBLE);
                numberOfPieces = 4;
                Toast.makeText(getApplicationContext(), "Chunks: 4", Toast.LENGTH_SHORT).show();
            }
        });

        pattern33Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pattern22ChkImg.setVisibility(View.INVISIBLE);
                pattern21ChkImg.setVisibility(View.INVISIBLE);
                pattern33ChkImg.setVisibility(View.VISIBLE);
                numberOfPieces = 9;
                Toast.makeText(getApplicationContext(), "Chunks: 9", Toast.LENGTH_SHORT).show();
            }
        });

        uploadPuzzleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, Cons.KEY_RESULT_LOAD_IMAGE);
                }
                catch(Exception e) {
                    e.getMessage();
                }
            }
        });

        publishPuzzleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (puzzleBitmap != null) {
                    if (numberOfPieces != 0) {
                        puzzleName = firstNameEdit.getText().toString();
                        if (puzzleName.length() >= 3) {
                            if (StringUtils.isAlphanumeric(puzzleName)) {
                                // ** split the puzzle according to the number of pieces **

                                int rows, cols;

                                //For height and width of the small image chunks
                                int chunkHeight, chunkWidth;

                                //To store all the small image chunks in bitmap format in this list
                                chunkedImages = new ArrayList<Bitmap>(numberOfPieces);

                                //Getting the scaled bitmap of the source image (scale it to 300x300 for test)
                                scaledPuzzleBitmap = Bitmap.createScaledBitmap(puzzleBitmap, 900, 900, true);

                                rows = cols = (int) Math.sqrt(numberOfPieces);

                                if (numberOfPieces == 2) {
                                    cols = 2;
                                }

                                chunkHeight = scaledPuzzleBitmap.getHeight() / rows;
                                chunkWidth = scaledPuzzleBitmap.getWidth() / cols;

                                //xCoord and yCoord are the pixel positions of the image chunks
                                int yCoord = 0;
                                for (int x = 0; x < rows; x++) {
                                    int xCoord = 0;
                                    for (int y = 0; y < cols; y++) {
                                        chunkedImages.add(Bitmap.createBitmap(scaledPuzzleBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                                        xCoord += chunkWidth;
                                    }
                                    yCoord += chunkHeight;
                                }

                                //Saving image to mobile internal memory for sometime
                                String root = getApplicationContext().getFilesDir().toString();
                                File myDir = new File(root + "/androidlift");
                                myDir.mkdirs();
                                mFileName = puzzleName + ".jpg";
                                mImagePath = root + "/androidlift/" + mFileName;
                                mFile = new File(myDir, mFileName);

                                // insert PUZZLE into puzzles
                                ProcessPuzzleImageUpload processPuzzleImageUpload = new ProcessPuzzleImageUpload();
                                processPuzzleImageUpload.execute();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Puzzle title can only contain numbers and/or letters!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Puzzle title has to have at least 3 characters!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "You have to choose puzzle pattern!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "You have to upload puzzle image!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Cons.KEY_RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            puzzleBitmap = BitmapFactory.decodeFile(picturePath);
            uploadPuzzleImage.setImageBitmap(puzzleBitmap);
        }
    }

    // Async Task to get and send data to My Sql database through JSON response
    private class ProcessPuzzleUpload extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(CreatePuzzleActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Uploading puzzle created info...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            return userFunction.updateUserPuzzleCreated(mEmail);
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {
                    String res = json.getString(Cons.KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1) {
                        pDialog.setTitle(R.string.msg_getting_data);
                        pDialog.setMessage("Puzzle creation info successfully uploaded!");

                        pDialog.dismiss();
                    }
                    else {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Problem while uploading puzzle creation info!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // Async Task to get and send data to My Sql database through JSON response
    private class ProcessPuzzleImageUpload extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(CreatePuzzleActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Uploading puzzle image...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            UserFunctions userFunction = new UserFunctions();
            return userFunction.uploadPuzzle(mEmail, puzzleName, numberOfPieces);
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = (JSONObject)o;
            try {
                if (json.getString(Cons.KEY_SUCCESS) != null) {
                    String res = json.getString(Cons.KEY_SUCCESS);

                    if(Integer.parseInt(res) == 1) {
                        pDialog.setTitle(R.string.msg_getting_data);
                        pDialog.setMessage("Puzzle successfully uploaded!");

                        // increment puzzles_created field of author
                        ProcessPuzzleUpload processPuzzleUpload = new ProcessPuzzleUpload();
                        processPuzzleUpload.execute();

                        // upload full puzzle image
                        ProcessImageUpload processImageUpload = new ProcessImageUpload();
                        processImageUpload.execute();

                        // upload chunked puzzle images
                        ProcessChunkedImageUpload processChunkedImageUpload = new ProcessChunkedImageUpload();
                        processChunkedImageUpload.execute();

                        pDialog.dismiss();

                        //start My Puzzles Activity
                        Intent myPuzzles = new Intent(getApplicationContext(), MyPuzzlesActivity.class);
                        startActivity(myPuzzles);
                    }
                    else {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Problem while uploading puzzle - Try different puzzle title!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ProcessImageUpload extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(CreatePuzzleActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Uploading image...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            // upload image to server
            saveFile(scaledPuzzleBitmap, mFile);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            pDialog.dismiss();
        }
    }

    private void saveFile(Bitmap sourceUri, File destination) {
        if (destination.exists()) destination.delete();
        try {
            FileOutputStream out = new FileOutputStream(destination);
            sourceUri.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.flush();
            out.close();

            try {
                // Set your file path here
                FileInputStream fileInputStream = new FileInputStream(mImagePath);
                // Set your server page url (and the file title/description)
                HttpFileUpload httpFileUpload = new HttpFileUpload("http://vasic.ddns.net/geopuzzle_login_api/puzzle_upload.php", "ftitle", "fdescription", mFileName);
                httpFileUpload.Send_Now(fileInputStream);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ProcessChunkedImageUpload extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(CreatePuzzleActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Uploading image...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            // upload chunks to server
            for (int i=0; i < chunkedImages.size(); i++) {

                //Saving chunk to mobile internal memory for sometime
                String root = getApplicationContext().getFilesDir().toString();
                File myDir = new File(root + "/androidlift");
                myDir.mkdirs();
                String mChunkFileName = puzzleName + "_" + String.valueOf(i) + ".jpg";
                String mChunkImagePath = root + "/androidlift/" + mChunkFileName;
                File mChunkFile = new File(myDir, mChunkFileName);

                saveChunkFile(chunkedImages.get(i), mChunkFile, mChunkFileName, mChunkImagePath);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            pDialog.dismiss();
        }
    }

    private void saveChunkFile(Bitmap sourceUri, File destination, String mChunkFileName, String mChunkImagePath) {
        if (destination.exists()) destination.delete();
        try {
            FileOutputStream out = new FileOutputStream(destination);
            sourceUri.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.flush();
            out.close();

            try {
                // Set your file path here
                FileInputStream fileInputStream = new FileInputStream(mChunkImagePath);
                // Set your server page url (and the file title/description)
                HttpFileUpload httpFileUpload = new HttpFileUpload("http://vasic.ddns.net/geopuzzle_login_api/chunks_upload.php", "ftitle", "fdescription", mChunkFileName);
                httpFileUpload.Send_Now(fileInputStream);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
