package com.example.hitorigame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerNameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);

        int gridSize = getIntent().getIntExtra("gridSize", 5);
        EditText prenomInput = findViewById(R.id.firstNameEditText);
        EditText nomInput = findViewById(R.id.lastNameEditText);

        Button startButton = findViewById(R.id.startGameButton);

        startButton.setOnClickListener(v -> {
            String prenom = prenomInput.getText().toString().trim();
            String name = nomInput.getText().toString().trim();

            if (name.isEmpty()&prenom.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, GameActivity.class)
                        .putExtra("playerName", name)
                        .putExtra("playerPrenom", prenom)

                        .putExtra("gridSize", gridSize));
                finish();
            }
        });
    }
}