package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final int GRID_SIZE = 10;
    private static final int NUM_MINES = 5;

    private Cell[][] cells = new Cell[GRID_SIZE][GRID_SIZE];
    private Button[][] cellButtons = new Button[GRID_SIZE][GRID_SIZE];
    private GridLayout minefieldGrid;
    private TextView mineCountTextView;
    private TextView timerTextView;
    private Button modeButton;
    private boolean isFlaggingMode = false;

    private int flagsRemaining = NUM_MINES;
    private boolean isGameOver = false;

    private Handler timerHandler = new Handler();
    private int secondsElapsed = 0;
    private boolean isTimerStarted = false;
    private boolean hasWon = false;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            secondsElapsed++;
            timerTextView.setText(getString(R.string.clock) + " " + secondsElapsed);
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minefieldGrid = findViewById(R.id.minefieldGrid);
        mineCountTextView = findViewById(R.id.mineCountTextView);
        timerTextView = findViewById(R.id.timerTextView);
        modeButton = findViewById(R.id.modeButton);

        modeButton.setOnClickListener(v -> {
            isFlaggingMode = !isFlaggingMode;
            if (isFlaggingMode) {
                modeButton.setText(getString(R.string.flag));
            } else {
                modeButton.setText(getString(R.string.pick));
            }
        });

        setupGame();
    }

    private void setupGame() {
        isGameOver = false;
        hasWon = false;
        flagsRemaining = NUM_MINES;
        mineCountTextView.setText(getString(R.string.flag) + " " + flagsRemaining);

        isTimerStarted = false;
        secondsElapsed = 0;
        timerTextView.setText(getString(R.string.clock) + " 0");
        timerHandler.removeCallbacks(timerRunnable);

        isFlaggingMode = false;
        modeButton.setText(getString(R.string.pick));

        initializeCells();
        placeMines();
        calculateAdjacentMines();
        createCellButtons();
    }

    private void initializeCells() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                cells[i][j] = new Cell();
            }
        }
    }

    private void placeMines() {
        Random random = new Random();
        int minesPlaced = 0;
        while (minesPlaced < NUM_MINES) {
            int row = random.nextInt(GRID_SIZE);
            int col = random.nextInt(GRID_SIZE);
            if (!cells[row][col].isMine()) {
                cells[row][col].setMine(true);
                minesPlaced++;
            }
        }
    }

    private void calculateAdjacentMines() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!cells[i][j].isMine()) {
                    int count = 0;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            int ni = i + dr;
                            int nj = j + dc;
                            if (ni >= 0 && ni < GRID_SIZE && nj >= 0 && nj < GRID_SIZE && cells[ni][nj].isMine()) {
                                count++;
                            }
                        }
                    }
                    cells[i][j].setAdjacentMines(count);
                }
            }
        }
    }

    private void createCellButtons() {
        minefieldGrid.removeAllViews();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        int buttonSize = (screenWidth - 2 * padding) / GRID_SIZE;

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Button button = new Button(this);
                button.setText("");
                button.setBackgroundColor(ContextCompat.getColor(this, R.color.cell_hidden));

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                params.setMargins(1, 1, 1, 1);

                button.setLayoutParams(params);

                final int row = i;
                final int col = j;
                button.setOnClickListener(v -> onCellClicked(row, col));

                cellButtons[i][j] = button;
                minefieldGrid.addView(button);
            }
        }
    }

    private void onCellClicked(int row, int col) {
        if (isGameOver) {
            Intent intent = new Intent(MainActivity.this, ResultActivity.class);
            intent.putExtra("GAME_WON", hasWon);
            intent.putExtra("TIME_USED", secondsElapsed);
            startActivity(intent);
            finish();
            return;
        }

        if (cells[row][col].isRevealed()) {
            return;
        }

        if (isFlaggingMode) {
            toggleFlag(row, col);
        } else {
            revealCell(row, col);
        }
    }

    private void toggleFlag(int row, int col) {
        Cell cell = cells[row][col];
        cell.setFlagged(!cell.isFlagged());

        if (cell.isFlagged()) {
            cellButtons[row][col].setText(getString(R.string.flag));
            flagsRemaining--;
        } else {
            cellButtons[row][col].setText("");
            flagsRemaining++;
        }
        mineCountTextView.setText(getString(R.string.flag) + " " + flagsRemaining);
    }

    private void revealCell(int row, int col) {
        if (!isTimerStarted && !isFlaggingMode) {
            timerHandler.postDelayed(timerRunnable, 1000);
            isTimerStarted = true;
        }

        Cell cell = cells[row][col];
        if (cell.isFlagged() || cell.isRevealed()) {
            return;
        }
        cell.setRevealed(true);
        cellButtons[row][col].setBackgroundColor(ContextCompat.getColor(this, R.color.cell_revealed));

        if (cell.isMine()) {
            gameOver(false);
            return;
        }

        if (cell.getAdjacentMines() > 0) {
            cellButtons[row][col].setText(String.valueOf(cell.getAdjacentMines()));
            cellButtons[row][col].setTextColor(Color.BLACK);
        } else {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int ni = row + dr;
                    int nj = col + dc;
                    if (ni >= 0 && ni < GRID_SIZE && nj >= 0 && nj < GRID_SIZE && !cells[ni][nj].isRevealed()) {
                        revealCell(ni, nj);
                    }
                }
            }
        }
        checkWinCondition();
    }

    private void gameOver(boolean won) {
        isGameOver = true;
        this.hasWon = won;
        timerHandler.removeCallbacks(timerRunnable);

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (cells[i][j].isMine()) {
                    cellButtons[i][j].setText(getString(R.string.mine));
                    cellButtons[i][j].setBackgroundColor(ContextCompat.getColor(this, R.color.cell_mine));
                }
            }
        }
        Toast.makeText(this, "Game Over. Tap any cell to see results.", Toast.LENGTH_SHORT).show();
    }

    private void checkWinCondition() {
        int unrevealedCells = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!cells[i][j].isRevealed()) {
                    unrevealedCells++;
                }
            }
        }

        if (unrevealedCells == NUM_MINES) {
            gameOver(true);
        }
    }
}