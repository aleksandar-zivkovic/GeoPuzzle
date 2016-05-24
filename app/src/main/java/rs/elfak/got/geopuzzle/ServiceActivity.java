package rs.elfak.got.geopuzzle;

import android.content.ComponentName;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ServiceActivity extends AppCompatActivity {
    private Button mStartServiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

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
