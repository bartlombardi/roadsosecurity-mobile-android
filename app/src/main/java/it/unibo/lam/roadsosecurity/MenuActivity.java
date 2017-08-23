package it.unibo.lam.roadsosecurity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private Button anomalyButton;
    private Button aboutButton;
    private Button numberButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        Boolean isFirstRun = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {
            startActivity(new Intent(this, NumberActivity.class));
        }

        anomalyButton = (Button) findViewById(R.id.anomalyButton);
        aboutButton = (Button) findViewById(R.id.aboutButton);
        numberButton = (Button) findViewById(R.id.numberButton);

        anomalyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AnomalyActivity.class);
                startActivity(i);
            }
        });

        numberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), NumberActivity.class);
                startActivity(i);
            }
        });

        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(i);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }
}
