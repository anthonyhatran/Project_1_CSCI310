package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResultActivity extends AppCompatActivity {

    private TextView resultTextView;
    private TextView timeTextView;
    private Button playAgainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);

        resultTextView = findViewById(R.id.resultTextView);
        timeTextView = findViewById(R.id.timeTextView);
        playAgainButton = findViewById(R.id.playAgainButton);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        boolean gameWon = intent.getBooleanExtra("GAME_WON", false);
        int timeUsed = intent.getIntExtra("TIME_USED", 0);

        if (gameWon) {
            resultTextView.setText(getString(R.string.game_won));
        } else {
            resultTextView.setText(getString(R.string.game_lost));
        }

        timeTextView.setText("Time used: " + timeUsed + " seconds");

        playAgainButton.setOnClickListener(v -> {
            Intent mainActivityIntent = new Intent(ResultActivity.this, MainActivity.class);
            startActivity(mainActivityIntent);
            finish();
        });
    }
}