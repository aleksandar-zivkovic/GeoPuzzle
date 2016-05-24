package rs.elfak.got.geopuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;

import rs.elfak.got.geopuzzle.library.*;

public class HomeActivity extends AppCompatActivity {
    Button mMyProfileBtn;
    Button mShowMapBtn;
    Button mUploadPuzzleBtn;
    Button mGeoPuzzleServiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        HashMap keyValue = db.getKeyValue(Cons.KEY_LOGGED_IN);

        if(keyValue.size() == 0  || keyValue.get(Cons.KEY_LOGGED_IN).toString().equals("false")) {
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
        }
        else {
            mMyProfileBtn = (Button) findViewById(R.id.myProfileBtn);
            mMyProfileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent myProfile = new Intent(view.getContext(), ProfileActivity.class);
                    startActivity(myProfile);
                }
            });

            mShowMapBtn = (Button) findViewById(R.id.showMapBtn);
            mShowMapBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent map = new Intent(view.getContext(), MapActivity.class);
                    startActivity(map);
                }
            });

            mUploadPuzzleBtn = (Button) findViewById(R.id.uploadPuzzleBtn);
            mUploadPuzzleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent upload = new Intent(view.getContext(), CreatePuzzle.class);
                    startActivity(upload);
                }
            });

            mGeoPuzzleServiceBtn = (Button) findViewById(R.id.geoPuzzleServiceBtn);
            mGeoPuzzleServiceBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent service = new Intent(view.getContext(), ServiceActivity.class);
                    startActivity(service);
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        HashMap keyValue = db.getKeyValue(Cons.KEY_KEEP_LOGGED_IN);

        if(keyValue.size() > 0) {
            String keepLoggedIn = keyValue.get(Cons.KEY_KEEP_LOGGED_IN).toString();
            if(keepLoggedIn.equals("false"))
                new UserFunctions().logoutUser(getApplicationContext());
        }
    }
}
