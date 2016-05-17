package rs.elfak.got.geopuzzle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

public class MyFriendsActivity extends AppCompatActivity {

    private ListView mUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_friends);

        mUserList = (ListView) findViewById(R.id.userListView);
    }
}
