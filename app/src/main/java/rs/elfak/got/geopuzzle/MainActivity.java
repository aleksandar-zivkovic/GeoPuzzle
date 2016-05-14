package rs.elfak.got.geopuzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import rs.elfak.got.geopuzzle.library.UserFunctions;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;

import java.util.HashMap;

public class MainActivity extends Activity {

    Button btnLogout;
    Button changepas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        changepas = (Button) findViewById(R.id.btchangepass);
        btnLogout = (Button) findViewById(R.id.logout);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());

        /**
         * Hashmap to load data from the Sqlite database
         **/
        HashMap user = new HashMap();
        user = db.getUserDetails();

        /**
         * Change Password Activity Started
         **/
        changepas.setOnClickListener(new View.OnClickListener(){
            public void onClick(View arg0){

                Intent chgpass = new Intent(getApplicationContext(), ChangePasswordActivity.class);

                startActivity(chgpass);
            }

        });

        /**
         *Logout from the User Panel which clears the data in Sqlite database
         **/
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

        /**
         * Sets user first name and last name in text view.
         **/
        final TextView login = (TextView) findViewById(R.id.textwelcome);
        login.setText("Welcome  "+user.get("fname"));
        final TextView lname = (TextView) findViewById(R.id.lname);
        lname.setText((String)user.get("lname"));
    }
}
