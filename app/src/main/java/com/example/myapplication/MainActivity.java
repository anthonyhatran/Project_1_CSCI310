package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_SIZE = 10;
    private static final int NUM_MINES = 5;

    private Cell[][] cells = new Cell[GRID_SIZE][GRID_SIZE];
    private Button[][] cellButtons = new Button[GRID_SIZE][GRID_SIZE];
    private GridLayout minefieldGrid;
    private TextView mineCountTextView;
    private TextView timerTextView;
    private ToggleButton modeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minefieldGrid = findViewById(R.id.minefieldGrid);
        mineCountTextView = findViewById(R.id.mineCountTextView);
        timerTextView = findViewById(R.id.timerTextView);
        modeButton = findViewById(R.id.modeButton);

        setupGame();
    }

    private void setupGame() {
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
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        int buttonSize = (screenWidth - 2 * padding) / GRID_SIZE;


        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Button button = new Button(this);
                button.setText("");
                button.setBackgroundColor(Color.LTGRAY);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                button.setLayoutParams(params);

                cellButtons[i][j] = button;
                minefieldGrid.addView(button);
            }
        }
    }
}