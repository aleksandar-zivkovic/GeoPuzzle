package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import rs.elfak.got.geopuzzle.library.UserFunctions;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Button btnLogout;
    Button btnChangePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnChangePass = (Button) findViewById(R.id.btnChangePass);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        // Hashmap to load data from the Sqlite database
        HashMap user = new HashMap();
        user = db.getUserDetails();

        // Change Password Activity Started
        btnChangePass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent changePass = new Intent(getApplicationContext(), ChangePasswordActivity.class);
                startActivity(changePass);
            }
        });

        // Logout from the User Panel which clears the data in Sqlite database
        btnLogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                UserFunctions logout = new UserFunctions();
                logout.logoutUser(getApplicationContext());
                Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                finish();
            }
        });

        // Sets user first name and last name in text view
        final TextView login = (TextView) findViewById(R.id.textWelcome);
        login.setText("Welcome  "+user.get("fname"));
        final TextView lname = (TextView) findViewById(R.id.lastName);
        lname.setText((String)user.get("lname"));
    }
}
