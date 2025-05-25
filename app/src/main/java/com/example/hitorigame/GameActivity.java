package com.example.hitorigame;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
import java.util.Stack;

public class GameActivity extends AppCompatActivity {

    private int clicks = 0;
    private int[][] grid;
    private int[][] selected; // 0 = normal, 1 = cercle, 2 = noir
    private CountDownTimer timer;
    private long timeElapsed = 0;
    private String playerName;
    private String playerPrenom;

    private int gridSize; // Taille de la grille (5, 6, 7 ou 8)
    private TextView timeText;
    private TextView clicksText;
    private TextView welcomeText;
    private TableLayout gameGrid;

    // Variables pour Undo/Redo
    private Button btnUndo, btnRedo;
    private Stack<CellState> undoStack = new Stack<>();
    private Stack<CellState> redoStack = new Stack<>();

    // Classe interne pour stocker l'état d'une cellule
    private class CellState {
        int row, col, state;
        CellState(int row, int col, int state) {
            this.row = row;
            this.col = col;
            this.state = state;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialisation des vues
        welcomeText = findViewById(R.id.welcomeText);
        timeText = findViewById(R.id.timeText);
        clicksText = findViewById(R.id.clicksText);
        gameGrid = findViewById(R.id.gameGrid);
        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);

        // Récupérer les données de l'intent
        playerName = getIntent().getStringExtra("playerName");
        playerPrenom = getIntent().getStringExtra("playerPrenom");

        gridSize = getIntent().getIntExtra("gridSize", 5); // 5 par défaut

        // Initialiser les tableaux avec la bonne taille
        grid = new int[gridSize][gridSize];
        selected = new int[gridSize][gridSize];

        welcomeText.setText("Bienvenue " + playerPrenom + " "+playerName+"");

        // Initialiser le jeu
        initializeGame();

        // Configurer les boutons
        btnUndo.setOnClickListener(v -> undoLastAction());
        btnRedo.setOnClickListener(v -> redoLastAction());

        Button checkButton = findViewById(R.id.checkButton);
        Button menuButton = findViewById(R.id.menuButton);

        checkButton.setOnClickListener(v -> checkSolution());
        menuButton.setOnClickListener(v -> finish());

        startTimer();
    }

    private void initializeGame() {
        gameGrid.removeAllViews();
        Random random = new Random();

        // Ajuster la taille du texte en fonction de la taille de la grille
        int textSize = 20;
        if (gridSize > 5) textSize = 18;
        if (gridSize > 6) textSize = 16;
        if (gridSize > 7) textSize = 14;

        for (int i = 0; i < gridSize; i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = random.nextInt(gridSize) + 1; // Valeurs entre 1 et gridSize
                selected[i][j] = 0;

                Button cell = new Button(this);
                TableRow.LayoutParams params = new TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        1f);
                params.setMargins(1, 1, 1, 1);
                cell.setLayoutParams(params);

                cell.setText(String.valueOf(grid[i][j]));
                cell.setBackgroundResource(R.drawable.cell_normal);
                cell.setTextColor(Color.BLACK);
                cell.setTextSize(textSize);

                final int finalI = i;
                final int finalJ = j;
                cell.setOnClickListener(v -> {
                    undoStack.push(new CellState(finalI, finalJ, selected[finalI][finalJ]));
                    redoStack.clear();

                    selected[finalI][finalJ] = (selected[finalI][finalJ] + 1) % 3;
                    updateCellAppearance(finalI, finalJ);

                    clicks++;
                    updateClicks();
                });

                row.addView(cell);
            }
            gameGrid.addView(row);
        }
    }

    private void updateClicks() {
        clicksText.setText("Clicks: " + clicks);
    }

    private void startTimer() {
        timer = new CountDownTimer(300000, 1000) {
            public void onTick(long millisUntilFinished) {
                timeElapsed = 300000 - millisUntilFinished;
                updateTime();
            }

            public void onFinish() {
                timeText.setText("Time: 05:00");
                Toast.makeText(GameActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                checkSolution();
            }
        }.start();
    }

    private void updateTime() {
        int minutes = (int) (timeElapsed / 60000);
        int seconds = (int) (timeElapsed % 60000) / 1000;
        timeText.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    private void updateCellAppearance(int row, int col) {
        TableRow tableRow = (TableRow) gameGrid.getChildAt(row);
        Button cell = (Button) tableRow.getChildAt(col);

        switch (selected[row][col]) {
            case 0:
                cell.setBackgroundResource(R.drawable.cell_normal);
                cell.setTextColor(Color.BLACK);
                break;
            case 1:
                cell.setBackgroundResource(R.drawable.cell_circle);
                cell.setTextColor(Color.BLACK);
                break;
            case 2:
                cell.setBackgroundResource(R.drawable.cell_black_border);
                cell.setTextColor(Color.BLACK);
                break;
        }
    }

    private void undoLastAction() {
        if (!undoStack.isEmpty()) {
            CellState state = undoStack.pop();
            redoStack.push(new CellState(state.row, state.col, selected[state.row][state.col]));

            selected[state.row][state.col] = state.state;
            updateCellAppearance(state.row, state.col);
        }
    }

    private void redoLastAction() {
        if (!redoStack.isEmpty()) {
            CellState state = redoStack.pop();
            undoStack.push(new CellState(state.row, state.col, selected[state.row][state.col]));

            selected[state.row][state.col] = state.state;
            updateCellAppearance(state.row, state.col);
        }
    }

    private void checkSolution() {
        boolean isValid = true;

        // Vérifier les règles du jeu Hitori
        for (int i = 0; i < gridSize && isValid; i++) {
            for (int j = 0; j < gridSize && isValid; j++) {
                if (selected[i][j] != 2) { // Si la cellule n'est pas noire
                    // Vérification des doublons en ligne
                    for (int k = 0; k < gridSize; k++) {
                        if (k != j && selected[i][k] != 2 && grid[i][j] == grid[i][k]) {
                            isValid = false;
                            break;
                        }
                    }
                    // Vérification des doublons en colonne
                    for (int k = 0; k < gridSize; k++) {
                        if (k != i && selected[k][j] != 2 && grid[i][j] == grid[k][j]) {
                            isValid = false;
                            break;
                        }
                    }
                }
            }
        }

        // Vérification que toutes les cellules non noires sont connectées
        if (isValid) {
            isValid = checkConnectivity();
        }

        if (isValid) {
            Toast.makeText(this, "Solution correcte!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Solution incorrecte!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkConnectivity() {
        boolean[][] visited = new boolean[gridSize][gridSize];
        int startX = -1, startY = -1;

        // Trouver une cellule non noire pour démarrer
        outerloop:
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (selected[i][j] != 2) {
                    startX = i;
                    startY = j;
                    break outerloop;
                }
            }
        }

        if (startX == -1) return false; // Toutes les cellules sont noires

        // Parcours en profondeur
        dfs(startX, startY, visited);

        // Vérifier que toutes les cellules non noires ont été visitées
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (selected[i][j] != 2 && !visited[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    private void dfs(int x, int y, boolean[][] visited) {
        if (x < 0 || x >= gridSize || y < 0 || y >= gridSize || visited[x][y] || selected[x][y] == 2) {
            return;
        }

        visited[x][y] = true;

        // Visiter les 4 directions
        dfs(x+1, y, visited);
        dfs(x-1, y, visited);
        dfs(x, y+1, visited);
        dfs(x, y-1, visited);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}