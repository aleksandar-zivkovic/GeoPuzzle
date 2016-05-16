package rs.elfak.got.geopuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;

import java.util.HashMap;

public class RegisteredActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered);

        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        HashMap user = db.getUserDetails();

        // Displays the registration details in Text view
        final TextView firstNameEdit = (TextView)findViewById(R.id.firstNameText);
        final TextView lastNameEdit = (TextView)findViewById(R.id.lastNameText);
        final TextView emailEdit = (TextView)findViewById(R.id.emailText);
        final TextView usernameEdit = (TextView)findViewById(R.id.usernameText);
        final TextView phoneNumber = (TextView)findViewById(R.id.phoneNumberText);
        final TextView createdAtEdit = (TextView)findViewById(R.id.registredAtText);

        firstNameEdit.setText((String)user.get(Cons.KEY_FIRSTNAME));
        lastNameEdit.setText((String)user.get(Cons.KEY_LASTNAME));
        emailEdit.setText((String)user.get(Cons.KEY_EMAIL));
        usernameEdit.setText((String)user.get(Cons.KEY_USERNAME));
        phoneNumber.setText((String)user.get(Cons.KEY_PHONE_NUMBER));
        createdAtEdit.setText((String)user.get(Cons.KEY_CREATED_AT));

        Button goToLogin = (Button) findViewById(R.id.goToLoginBtn);
        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
                // Close all views before launching Registered screen
                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent);
                finish();
            }
        });
    }
}
