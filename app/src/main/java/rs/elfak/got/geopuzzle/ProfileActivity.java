package rs.elfak.got.geopuzzle;

import java.util.HashMap;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import rs.elfak.got.geopuzzle.library.*;

public class ProfileActivity extends AppCompatActivity {
    private Button mLogoutBtn;
    private Button mChangePasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mLogoutBtn = (Button) findViewById(R.id.logoutBtn);
        mChangePasswordBtn = (Button) findViewById(R.id.changePasswordBtn);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        // Hashmap to load data from the Sqlite database
        HashMap user = db.getUserDetails();

        // Logout from the User Panel which clears the data in Sqlite database
        mLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                UserFunctions logout = new UserFunctions();
                logout.logoutUser(getApplicationContext());
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                finish();
            }
        });
        // Start Change Password Activity
        mChangePasswordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent changePass = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePass);
            }
        });

        // Sets user first name and last name in text view
        TextView titleText = (TextView) findViewById(R.id.titleText);
        titleText.setText(user.get(Cons.KEY_FIRSTNAME) + " " + user.get(Cons.KEY_LASTNAME));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }
}
