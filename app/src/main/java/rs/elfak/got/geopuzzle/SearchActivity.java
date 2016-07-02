package rs.elfak.got.geopuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SearchActivity extends AppCompatActivity {
    Button mSearchFriendsBtn;
    Button mSearchPuzzlesBtn;
    Button mSearchByDistanceBtn;
    EditText mSearchCriteriumEdit;
    String mSearchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.logo);

        mSearchCriteriumEdit = (EditText) findViewById(R.id.searchCriteriumEdit);

        mSearchFriendsBtn = (Button) findViewById(R.id.searchFriendsBtn);
        mSearchFriendsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchText = mSearchCriteriumEdit.getText().toString();
                Intent searchFriendsIntent = new Intent(view.getContext(), SearchFriendsActivity.class);
                searchFriendsIntent.putExtra("searchText", mSearchText);
                startActivity(searchFriendsIntent);
            }
        });

        mSearchPuzzlesBtn = (Button) findViewById(R.id.searchPuzzlesBtn);
        mSearchPuzzlesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchText = mSearchCriteriumEdit.getText().toString();
                Intent searchPuzzlesIntent = new Intent(view.getContext(), SearchPuzzlesActivity.class);
                searchPuzzlesIntent.putExtra("searchText", mSearchText);
                startActivity(searchPuzzlesIntent);
            }
        });

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
