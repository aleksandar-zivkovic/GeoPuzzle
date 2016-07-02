package rs.elfak.got.geopuzzle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.HashMap;

import rs.elfak.got.geopuzzle.library.Cons;
import rs.elfak.got.geopuzzle.library.DatabaseHandler;

public class ServiceActivity extends AppCompatActivity {
    private Button mStartServiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        final AppCompatActivity act = this;

        mStartServiceBtn = (Button) findViewById(R.id.startServiceBtn);

        if(GeoPuzzleService.mState == GeoPuzzleService.STOPPED)
            mStartServiceBtn.setText(R.string.start_service);
        else
            mStartServiceBtn.setText(R.string.stop_service);

        mStartServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(GeoPuzzleService.mState == GeoPuzzleService.STOPPED) {
                    startService(new Intent(getApplicationContext(), GeoPuzzleService.class));
                    mStartServiceBtn.setText(R.string.stop_service);
                    Toast.makeText(getApplicationContext(), "Service started!", Toast.LENGTH_SHORT).show();

                    DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                    HashMap regId = db.getKeyValue(Cons.KEY_REG_ID);
                    if(!regId.containsKey(Cons.KEY_REG_ID))
                        new RegisterApp(getApplicationContext()).execute();
                }
                else {
                    stopService(new Intent(getApplicationContext(), GeoPuzzleService.class));
                    mStartServiceBtn.setText(R.string.start_service);
                    Toast.makeText(getApplicationContext(), "Service stoped!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
