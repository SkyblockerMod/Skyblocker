package me.xmrvizzy.skyblocker.utils.tictactoe;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TicTacToeUtils {

    public static int getBestMove(char[][] board) {
        HashMap<Integer, Integer> moves = new HashMap<>();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] != '\0') continue;
                board[row][col] = 'O';
                int score = minimax(board, false, 0);
                board[row][col] = '\0';
                moves.put(row * 3 + col + 1, score);
            }
        }
        return Collections.max(moves.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public static boolean hasMovesLeft(char[][] board) {
        for (char[] rows : board) {
            for (char col : rows) {
                if (col == '\0') return true;
            }
        }
        return false;
    }

    public static int getBoardRanking(char[][] board) {
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][0] == board[row][2]) {
                if (board[row][0] == 'X') {
                    return -10;
                } else if (board[row][0] == 'O') {
                    return 10;
                }
            }
        }

        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[0][col] == board[2][col]) {
                if (board[0][col] == 'X') {
                    return -10;
                } else if (board[0][col] == 'O') {
                    return 10;
                }
            }
        }

        if (board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
            if (board[0][0] == 'X') {
                return -10;
            } else if (board[0][0] == 'O') {
                return 10;
            }
        } else if (board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
            if (board[0][2] == 'X') {
                return -10;
            } else if (board[0][2] == 'O') {
                return 10;
            }
        }

        return 0;
    }

    public static int minimax(char[][] board, boolean max, int depth) {
        int score = getBoardRanking(board);
        if (score == 10 || score == -10) return score;
        if (!hasMovesLeft(board)) return 0;

        if (max) {
            int bestScore = -1000;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (board[row][col] == '\0') {
                        board[row][col] = 'O';
                        bestScore = Math.max(bestScore, minimax(board, false, depth + 1));
                        board[row][col] = '\0';
                    }
                }
            }
            return bestScore - depth;
        } else {
            int bestScore = 1000;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    if (board[row][col] == '\0') {
                        board[row][col] = 'X';
                        bestScore = Math.min(bestScore, minimax(board, true, depth + 1));
                        board[row][col] = '\0';
                    }
                }
            }
            return bestScore + depth;
        }
    }

}