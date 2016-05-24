package rs.elfak.got.geopuzzle;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class CreatePuzzle extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_puzzle);

        final ImageView pattern22Img = (ImageView) findViewById(R.id.pattern22Img);
        final ImageView pattern22ChkImg = (ImageView) findViewById(R.id.pattern22ChkImg);
        final ImageView pattern23Img = (ImageView) findViewById(R.id.pattern23Img);
        final ImageView pattern23ChkImg = (ImageView) findViewById(R.id.pattern23ChkImg);
        final ImageView pattern33Img = (ImageView) findViewById(R.id.pattern33Img);
        final ImageView pattern33ChkImg = (ImageView) findViewById(R.id.pattern33ChkImg);

        pattern22Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pattern22ChkImg.setVisibility(View.VISIBLE);
                pattern23ChkImg.setVisibility(View.INVISIBLE);
                pattern33ChkImg.setVisibility(View.INVISIBLE);
            }
        });

        pattern23Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pattern23ChkImg.setVisibility(View.VISIBLE);

                pattern22ChkImg.setVisibility(View.INVISIBLE);
                pattern33ChkImg.setVisibility(View.INVISIBLE);
            }
        });

        pattern33Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pattern33ChkImg.setVisibility(View.VISIBLE);

                pattern22ChkImg.setVisibility(View.INVISIBLE);
                pattern23ChkImg.setVisibility(View.INVISIBLE);
            }
        });
    }
}
