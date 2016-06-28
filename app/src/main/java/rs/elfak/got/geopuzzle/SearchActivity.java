package rs.elfak.got.geopuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SearchActivity extends AppCompatActivity {
    Button mSearchFriendsBtn;
    Button mSearchPuzzlesBtn;
    Button mSearchByDistanceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchByDistanceBtn = (Button) findViewById(R.id.searchByDistanceBtn);
        mSearchByDistanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchByDistance = new Intent(view.getContext(), SearchByDistanceActivity.class);
                startActivity(searchByDistance);
            }
        });
    }
}
