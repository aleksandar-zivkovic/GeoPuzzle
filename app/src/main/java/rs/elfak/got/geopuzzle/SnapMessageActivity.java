package rs.elfak.got.geopuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import rs.elfak.got.geopuzzle.library.Cons;

public class SnapMessageActivity extends AppCompatActivity {

    private TextView mSenderEmailTextView;
    private TextView mSnapMessageTextView;
    private Button mGoToProfileButton;

    private String mSenderEmail;
    private String mSnapMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_message);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        mSenderEmailTextView = (TextView) findViewById(R.id.snapMessageTitle);
        mSnapMessageTextView = (TextView) findViewById(R.id.snapMessageText);
        mGoToProfileButton = (Button) findViewById(R.id.goToFriendsProfileBtn);


        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            if (bundle.getString("senderEmail") != null) {
                mSenderEmail = bundle.getString("senderEmail");
            }

            if (bundle.getString("snapMessage") != null) {
                mSnapMessage = bundle.getString("snapMessage");
            }
        }

        mSenderEmailTextView.setText("Your friend " + mSenderEmail + " sent you a snap message!");
        mSnapMessageTextView.setText(mSnapMessage);

        mGoToProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent friendsProfile = new Intent(view.getContext(), ProfileActivity.class);
                friendsProfile.putExtra(Cons.KEY_EMAIL, mSenderEmail);
                startActivity(friendsProfile);
            }
        });


    }
}
