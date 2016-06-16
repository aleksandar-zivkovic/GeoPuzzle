package rs.elfak.got.geopuzzle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import rs.elfak.got.geopuzzle.library.*;

public class ProfileActivity extends AppCompatActivity {
    private static final int STATE_USER_PROFILE = 0;
    private static final int STATE_FRIEND_PROFILE = 1;

    private Button mLogoutBtn;
    private Button mChangePasswordBtn;
    private Button mViewMyFriendsBtn;
    private Button mSearchForFriendsBtn;
    private Button mViewMyPuzzlesBtn;
    private ImageView mUserImageView;
    private ProgressDialog pDialog;
    private int state;

    private String mEmail;

    private Bitmap bitmap, bitmapRotate;
    private File photo;
    private String imagepath = "";
    private String fname;
    private File file;
    private Uri mImageUri;
    private String selectedImagePath;
    private String filemanagerstring;

    private Boolean upflag = false;
    private ConnectionDetector cd;

    private Bitmap bitmapToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        pDialog = new ProgressDialog(ProfileActivity.this);
        pDialog.setTitle(R.string.msg_contacting_servers);
        pDialog.setMessage("Downloading data...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        mUserImageView = (ImageView) findViewById(R.id.userImg);
        mLogoutBtn = (Button) findViewById(R.id.logoutBtn);
        mChangePasswordBtn = (Button) findViewById(R.id.changePasswordBtn);
        mViewMyFriendsBtn = (Button) findViewById(R.id.viewMyFriendsBtn);
        mSearchForFriendsBtn = (Button) findViewById(R.id.searchForFriendsBtn);
        mViewMyPuzzlesBtn = (Button) findViewById(R.id.viewMyPuzzlesBtn);
        TextView titleText = (TextView) findViewById(R.id.titleText);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        // Hashmap to load data from the Sqlite database or server
        HashMap user;

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.getString(Cons.KEY_EMAIL) != null) {
            state = STATE_FRIEND_PROFILE;
            this.setTitle(R.string.title_activity_friend);

            mLogoutBtn.setVisibility(View.INVISIBLE);
            mChangePasswordBtn.setVisibility(View.INVISIBLE);
            mViewMyFriendsBtn.setVisibility(View.INVISIBLE);
            mViewMyPuzzlesBtn.setVisibility(View.INVISIBLE);
            mSearchForFriendsBtn.setVisibility(View.INVISIBLE);

            // Sets user first name and last name in text view
            titleText.setText(bundle.getString(Cons.KEY_FULLNAME));
            mEmail = bundle.getString(Cons.KEY_EMAIL);
        }
        else {
            state = STATE_USER_PROFILE;
            user = db.getUserDetails();

            // Start Search For Friends Activity
            mSearchForFriendsBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    Intent searchForFriends = new Intent(getApplicationContext(), SearchForFriendsActivity.class);
                    startActivity(searchForFriends);
                }
            });
            // Start My Friends Activity
            mViewMyFriendsBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    Intent myFriends = new Intent(getApplicationContext(), MyFriendsActivity.class);
                    startActivity(myFriends);
                }
            });
            // Start My Puzzles Activity
            mViewMyPuzzlesBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    Intent myFriends = new Intent(getApplicationContext(), MyPuzzlesActivity.class);
                    startActivity(myFriends);
                }
            });
            // Start Change Password Activity
            mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    Intent changePass = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                    startActivity(changePass);
                }
            });
            // Logout from the User Panel which clears the data in Sqlite database
            mLogoutBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    UserFunctions logout = new UserFunctions();
                    logout.logoutUser(getApplicationContext());
                    Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                    login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(login);
                    finish();
                }
            });

            // Sets user first name and last name in text view
            titleText.setText(user.get(Cons.KEY_FIRSTNAME) + " " + user.get(Cons.KEY_LASTNAME));

            // Gets email
            mEmail = user.get(Cons.KEY_EMAIL).toString();
        }

        new DownloadImageTask().execute(Cons.KEY_UPLOADS_URL + mEmail + ".jpg");


        mUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final CharSequence[] items = { "Take Photo", "Choose from Gallery", "Cancel" };

                TextView title = new TextView(getApplicationContext());
                title.setText("Change Profile Photo?");
                title.setBackgroundColor(Color.BLACK);
                title.setPadding(10, 15, 15, 10);
                title.setGravity(Gravity.CENTER);
                title.setTextColor(Color.WHITE);
                title.setTextSize(22);
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setCustomTitle(title);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Take Photo")) {
                            Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                            try
                            {
                                // place where to store camera taken picture
                                photo = createTemporaryFile("picture", ".jpg");
                                photo.delete();

                                mImageUri = Uri.fromFile(photo);
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                                //start camera intent
                                startActivityForResult(cameraIntent, 101);
                            }
                            catch(Exception e)
                            {
                                Toast.makeText(getApplicationContext(), "Please check SD card! Image shot is impossible!", Toast.LENGTH_SHORT);
                            }
                        }
                        else if (items[item].equals("Choose from Gallery")) {
                            // in onCreate or any event where your want the user to
                            // select a file
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 102);
                        }
                        else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            try {
                this.getContentResolver().notifyChange(mImageUri, null);
                bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);

                if (Float.valueOf(getImageOrientation()) >= 0) {
                    bitmapRotate = rotateImage(bitmap, Float.valueOf(getImageOrientation()));
                    //bitmapRotate = bitmap;
                }
                else {
                    bitmapRotate = bitmap;
                    bitmap.recycle();
                }

                mUserImageView.setVisibility(View.VISIBLE);
                float aspect = ((float)bitmapRotate.getWidth())/((float)bitmapRotate.getHeight());
                mUserImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmapRotate, Cons.KEY_MAX_WIDTH, (int)(Cons.KEY_MAX_WIDTH/aspect), false));
                upflag = true;
                bitmapToShow = bitmapRotate;

                //Saving image to mobile internal memory for sometime
                String root = getApplicationContext().getFilesDir().toString();
                File myDir = new File(root + "/androidlift");
                myDir.mkdirs();
                fname = mEmail + ".jpg";
                imagepath = root + "/androidlift/" + fname;
                file = new File(myDir, fname);

                ProcessImageChanging processImageChanging = new ProcessImageChanging();
                processImageChanging.execute();
            }
            catch (Exception e) {
                e.getMessage();
            }
        }
        else if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            Uri selectedImageUri = data.getData();
            selectedImagePath = getPath(selectedImageUri);

            Bitmap bitmap = null;

            if (selectedImagePath != null){
                File f= new File(selectedImagePath);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                try {
                    bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            mUserImageView.setVisibility(View.VISIBLE);
            float aspect = ((float)bitmap.getWidth())/((float)bitmap.getHeight());
            mUserImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, Cons.KEY_MAX_WIDTH, (int)(Cons.KEY_MAX_WIDTH/aspect), false));
            upflag = true;
            bitmapToShow = bitmap;

            //Saving image to mobile internal memory for sometime
            String root = getApplicationContext().getFilesDir().toString();
            File myDir = new File(root + "/androidlift");
            myDir.mkdirs();
            fname = mEmail + ".jpg";
            imagepath = root + "/androidlift/" + fname;
            file = new File(myDir, fname);

            ProcessImageChanging processImageChanging = new ProcessImageChanging();
            processImageChanging.execute();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else
            return null;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    //    In some mobiles image will get rotate - > so this solves that
    private int getImageOrientation() {
        final String[] imageColumns = {MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
        final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageColumns, null, null, imageOrderBy);

        if (cursor.moveToFirst()) {
            int orientation = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION));
            System.out.println("orientation===" + orientation);
            cursor.close();
            return orientation;
        }
        else {
            return 0;
        }
    }

    //    Saves image to the mobile internal memory
    private void saveFile(Bitmap sourceUri, File destination) {
        if (destination.exists()) destination.delete();
        try {
            FileOutputStream out = new FileOutputStream(destination);
            sourceUri.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.flush();
            out.close();

            try {
                // Set your file path here
                FileInputStream fstrm = new FileInputStream(imagepath);
                // Set your server page url (and the file title/description)
                HttpFileUpload hfu = new HttpFileUpload("http://vasic.ddns.net/geopuzzle_login_api/file_upload.php", "ftitle", "fdescription", fname);
                upflag = hfu.Send_Now(fstrm);
            }
            catch (FileNotFoundException e) {
                // Error: File not found
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ProcessImageChanging extends AsyncTask {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setTitle(R.string.msg_contacting_servers);
            pDialog.setMessage("Uploading image...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {

            saveFile(bitmapToShow, file);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            pDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(state == STATE_USER_PROFILE)
            getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        else
            getMenuInflater().inflate(R.menu.menu_friend_profile, menu);
        return true;
    }

    private class DownloadImageTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            return (Bitmap) UserFunctions.loadImageFromNetwork((String) params[0]);
        }

        @Override
        protected void onPostExecute(Object o) {
            if(o != null)
                mUserImageView.setImageBitmap((Bitmap) o);

            pDialog.dismiss();
        }
    }
}
