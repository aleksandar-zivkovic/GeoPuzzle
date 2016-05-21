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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        HashMap keyValue = db.getKeyValue(Cons.KEY_LOGGED_IN);

        String loggedIn = keyValue.get(Cons.KEY_LOGGED_IN).toString();

        if(loggedIn.equals("false")) {
            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
            login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(login);
        }

        mMyProfileBtn = (Button) findViewById(R.id.myProfileBtn);
        mMyProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myProfile = new Intent(view.getContext(), ProfileActivity.class);
                startActivity(myProfile);
            }
        });
    }
}
