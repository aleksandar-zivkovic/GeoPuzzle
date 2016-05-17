package rs.elfak.got.geopuzzle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.Context;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import rs.elfak.got.geopuzzle.library.*;

public class RegisterActivity extends AppCompatActivity {

    private EditText mFirstNameEdit;
    private EditText mLastNameEdit;
    private EditText mEmailEdit;
    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private EditText mConfirmPasswordEdit;
    private EditText mPhoneNumberEdit;
    private Button mUploadPhotoBtn;
    private Button mRegisterBtn;
    private Button mCancelBtn;

    private ImageView ivImage;
    private Boolean upflag = false;
    private ConnectionDetector cd;
    private Uri selectedImage = null;
    private Bitmap bitmap, bitmapRotate;
    private ProgressDialog pDialog;
    String imagepath = "";
    String fname;
    File file;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirstNameEdit = (EditText) findViewById(R.id.firstNameEdit);
        mLastNameEdit = (EditText) findViewById(R.id.lastNameEdit);
        mEmailEdit = (EditText) findViewById(R.id.emailEdit);
        mUsernameEdit = (EditText) findViewById(R.id.usernameEdit);
        mPasswordEdit = (EditText) findViewById(R.id.passwordEdit);
        mConfirmPasswordEdit = (EditText) findViewById(R.id.confirmPasswordEdit);
        mPhoneNumberEdit = (EditText) findViewById(R.id.phoneNumberEdit);
        mUploadPhotoBtn = (Button) findViewById(R.id.uploadPhotoBtn);
        mRegisterBtn = (Button) findViewById(R.id.registerBtn);
        mCancelBtn = (Button) findViewById(R.id.cancelBtn);

        ivImage = (ImageView) findViewById(R.id.ivImage);
        cd = new ConnectionDetector(getApplicationContext());

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFirstNameEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_first_name_empty, Toast.LENGTH_SHORT).show();
                    if (mFirstNameEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mLastNameEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_last_name_empty, Toast.LENGTH_SHORT).show();
                    if (mLastNameEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mEmailEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_email_empty, Toast.LENGTH_SHORT).show();
                    if (mEmailEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mUsernameEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_username_empty, Toast.LENGTH_SHORT).show();
                    if (mUsernameEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mUsernameEdit.getText().toString().length() < 6) {
                    Toast.makeText(getApplicationContext(), R.string.msg_username_length, Toast.LENGTH_SHORT).show();
                    if (mUsernameEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mPasswordEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_password_empty, Toast.LENGTH_SHORT).show();
                    if (mPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mPasswordEdit.getText().toString().length() < 6) {
                    Toast.makeText(getApplicationContext(), R.string.msg_password_length, Toast.LENGTH_SHORT).show();
                    if (mPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mConfirmPasswordEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_password_confirm_empty, Toast.LENGTH_SHORT).show();
                    if (mConfirmPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(!mConfirmPasswordEdit.getText().toString().equals(mPasswordEdit.getText().toString())) {
                    Toast.makeText(getApplicationContext(), R.string.msg_password_not_match, Toast.LENGTH_SHORT).show();
                    if (mConfirmPasswordEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else if(mPhoneNumberEdit.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), R.string.msg_phone_number_empty, Toast.LENGTH_SHORT).show();
                    if (mPhoneNumberEdit.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
                else {
                    if (cd.isConnectingToInternet()) {
                        if (!upflag) {
                            Toast.makeText(RegisterActivity.this, "Image Not Captured..!", Toast.LENGTH_LONG).show();
                        } else {
                            saveFile(bitmapRotate, file);
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "No Internet Connection !", Toast.LENGTH_LONG).show();
                    }
                    NetAsync(view);
                }
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
//                startActivityForResult(myIntent, 0);
                finish();
            }
        });
        mUploadPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 101);
            }
        });
    }

    private class NetCheck extends AsyncTask {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(RegisterActivity.this);
            nDialog.setTitle(R.string.msg_checking_network);
            nDialog.setMessage("Loading...");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            // Gets current device state and checks for working internet connection by trying Google.
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                }
//                catch (MalformedURLException e1) {
//                    e1.printStackTrace();
//                }
//                catch (IOException e) {
//                    e.printStackTrace();
//                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Object o) {
            if((Boolean)o){
                nDialog.dismiss();
                new ProcessRegister().execute();
            }
            else {
                nDialog.dismiss();
                Toast.makeText(getApplicationContext(), R.string.msg_network_error, Toast.LENGTH_SHORT).show();
            }
        }

        private class ProcessRegister extends AsyncTask {
            private ProgressDialog pDialog;
            String firstName, lastName, email, username, password, phoneNumber;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                firstName = mFirstNameEdit.getText().toString();
                lastName = mLastNameEdit.getText().toString();
                email = mEmailEdit.getText().toString();
                username = mUsernameEdit.getText().toString();
                password = mPasswordEdit.getText().toString();
                phoneNumber = mPhoneNumberEdit.getText().toString();

                pDialog = new ProgressDialog(RegisterActivity.this);
                pDialog.setTitle(R.string.msg_contacting_servers);
                pDialog.setMessage("Registering ...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }

            @Override
            protected Object doInBackground(Object[] params) {
                UserFunctions userFunction = new UserFunctions();
                return userFunction.registerUser(firstName, lastName, email, username, password, phoneNumber);
            }

            @Override
            protected void onPostExecute(Object o) {
                JSONObject json = (JSONObject)o;
                try {
                    if (json.getString(Cons.KEY_SUCCESS) != null) {
                        String res = json.getString(Cons.KEY_SUCCESS);
                        String red = json.getString(Cons.KEY_ERROR);

                        if(Integer.parseInt(res) == 1){
                            pDialog.setTitle(R.string.msg_getting_data);
                            pDialog.setMessage("Loading Info");
                            Toast.makeText(getApplicationContext(), R.string.msg_registration_success, Toast.LENGTH_SHORT).show();

                            DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                            JSONObject json_user = json.getJSONObject(Cons.KEY_USER);

                            // Removes all the previous data in the SQlite database
                            UserFunctions logout = new UserFunctions();
                            logout.logoutUser(getApplicationContext());
                            db.addUser(json_user.getString(Cons.KEY_FIRSTNAME),
                                    json_user.getString(Cons.KEY_LASTNAME),
                                    json_user.getString(Cons.KEY_EMAIL),
                                    json_user.getString(Cons.KEY_USERNAME),
                                    json_user.getString(Cons.KEY_PHONE_NUMBER),
                                    json_user.getString(Cons.KEY_UID),
                                    json_user.getString(Cons.KEY_CREATED_AT));

                            // Stores registered data in SQlite Database
                            Intent registered = new Intent(getApplicationContext(), RegisteredActivity.class);

                            // Close all views before launching Registered screen
                            registered.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            pDialog.dismiss();
                            startActivity(registered);

                            finish();
                        }

                        else if (Integer.parseInt(red) == 2){
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.msg_user_exist, Toast.LENGTH_SHORT).show();
                        }
                        else if (Integer.parseInt(red) == 3){
                            pDialog.dismiss();
                            Toast.makeText(getApplicationContext(), R.string.msg_invalid_email, Toast.LENGTH_SHORT).show();
                        }
                    }

                    else {
                        pDialog.dismiss();
                        Toast.makeText(getApplicationContext(), R.string.msg_registration_error, Toast.LENGTH_SHORT).show();
                    }

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void NetAsync(View view){
        new NetCheck().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            switch (requestCode) {
                case 101:
                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            selectedImage = data.getData(); // the uri of the image taken
                            if (String.valueOf((Bitmap) data.getExtras().get("data")).equals("null")) {
                                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                            } else {
                                bitmap = (Bitmap) data.getExtras().get("data");
                            }
                            if (Float.valueOf(getImageOrientation()) >= 0) {
                                //bitmapRotate = rotateImage(bitmap, Float.valueOf(getImageOrientation()));
                                bitmapRotate = bitmap;
                            } else {
                                bitmapRotate = bitmap;
                                bitmap.recycle();
                            }

                            ivImage.setVisibility(View.VISIBLE);
                            ivImage.setImageBitmap(bitmapRotate);

                            //Saving image to mobile internal memory for sometime
                            String root = getApplicationContext().getFilesDir().toString();
                            File myDir = new File(root + "/androidlift");
                            myDir.mkdirs();

                            Random generator = new Random();
                            int n = 10000;
                            n = generator.nextInt(n);

                            //Give the file name that u want
                            fname = mEmailEdit.getText().toString() + ".jpg";

                            imagepath = root + "/androidlift/" + fname;
                            file = new File(myDir, fname);
                            upflag = true;
                        }
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    //    In some mobiles image will get rotate so to correting that this code will help us
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
        } else {
            return 0;
        }
    }

    //    Saving file to the mobile internal memory
    private void saveFile(Bitmap sourceUri, File destination) {
        if (destination.exists()) destination.delete();
        try {
            FileOutputStream out = new FileOutputStream(destination);
            sourceUri.compress(Bitmap.CompressFormat.JPEG, 60, out);
            out.flush();
            out.close();
            if (cd.isConnectingToInternet()) {
                new DoFileUpload().execute();
            } else {
                Toast.makeText(RegisterActivity.this, "No Internet Connection..", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class DoFileUpload extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("wait uploading Image..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                // Set your file path here
                FileInputStream fstrm = new FileInputStream(imagepath);
                // Set your server page url (and the file title/description)
                HttpFileUpload hfu = new HttpFileUpload("http://vasic.ddns.net/geopuzzle_login_api/file_upload.php", "ftitle", "fdescription", fname);
                upflag = hfu.Send_Now(fstrm);
            } catch (FileNotFoundException e) {
                // Error: File not found
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            if (upflag) {
                Toast.makeText(getApplicationContext(), "Uploading Complete", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Unfortunately file is not Uploaded..", Toast.LENGTH_LONG).show();
            }
        }
    }

}
