package com.example.hitorigame;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private int selectedGridSize = 5;
    private Button lastSelectedButton = null;
    private Drawable tickIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tickIcon = ContextCompat.getDrawable(this, R.drawable.ic_tick);
        if (tickIcon != null) {
            tickIcon.setBounds(0, 0, tickIcon.getIntrinsicWidth(), tickIcon.getIntrinsicHeight());
        }

        initSizeButtons();
        setupActionButtons();
    }

    private void initSizeButtons() {
        int[] buttonIds = {R.id.btn5x5, R.id.btn6x6, R.id.btn7x7, R.id.btn8x8};
        for (int id : buttonIds) {
            Button btn = findViewById(id);
            btn.setOnClickListener(v -> selectSizeButton(btn, Integer.parseInt(btn.getText().toString().charAt(0) + "")));
        }
        selectSizeButton(findViewById(R.id.btn5x5), 5); // Sélection par défaut
    }

    private void selectSizeButton(Button button, int size) {
        if (lastSelectedButton != null) {
            lastSelectedButton.setSelected(false);
            lastSelectedButton.setCompoundDrawables(null, null, null, null);
        }

        button.setSelected(true);
        button.setCompoundDrawables(null, null, tickIcon, null);
        selectedGridSize = size;
        lastSelectedButton = button;
    }

    private void setupActionButtons() {
        findViewById(R.id.playButton).setOnClickListener(v -> {
            if (selectedGridSize == 0) {
                Toast.makeText(this, "Please select grid size", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(this, PlayerNameActivity.class)
                    .putExtra("gridSize", selectedGridSize));
        });

        findViewById(R.id.rulesButton).setOnClickListener(v ->
                startActivity(new Intent(this, RulesActivity.class)));
    }
}