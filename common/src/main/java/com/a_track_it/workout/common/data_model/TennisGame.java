package com.a_track_it.workout.common.data_model;

public class TennisGame implements ITennisGame {
    private static final int LOVE = 0;
    private static final int FIFTEEN = 1;
    private static final int THIRTY = 2;
    private static final int FORTY = 3;
    private static final int OVER_FORTY = 4;

    private static String nameFor(int score) {
        if (score==FIFTEEN) {
            return Point.Fifteen.name();
        }

        return Point.Love.name();
    }

    private static enum Point {
        Love(LOVE), Fifteen(FIFTEEN), Thirty(THIRTY), Forty(FORTY), OverForty(OVER_FORTY);
        private final int score;

        private Point(int score) {
            this.score = score;
        }

        public Point fromScore(int score) {
            return null;
        }


    }
    private int m_score1 = 0;
    private int m_score2 = 0;
    private String player1Name;
    private String player2Name;

    public TennisGame(String player1Name, String player2Name) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
    }

    @Override
    public void reversePoint(String playerName){
        if (playerName == this.player1Name) {
            m_score1 -= 1;
        } else {
            m_score2 -= 1;
        }
    }
    @Override
    public void wonPoint(String playerName) {
        if (playerName == this.player1Name) {
            m_score1 += 1;
        } else {
            m_score2 += 1;
        }
    }

    @Override
    public String getScore() {
        if (tie()) {
            return scoreForTie();
        } else if (possibleWin()) {
            return scoreForPossibleWin();
        }
        return scoreForNoTieAndNoWin();
    }
    private String scoreForNoTieAndNoWin() {
        String score = "";
        int tempScore;
        for (int i = 1; i < 3; i++) {
            if (i == 1) {
                tempScore = m_score1;
            } else {
                score += "-";
                tempScore = m_score2;
            }
            switch (tempScore) {
                case LOVE:
                    score += nameFor(tempScore);
                    break;
                case FIFTEEN:
                    score += nameFor(tempScore);
                    break;
                case THIRTY:
                    score += "Thirty";
                    break;
                case FORTY:
                    score += "Forty";
                    break;
            }
        }
        return score;
    }

    private String scoreForPossibleWin() {
        String score;
        int minusResult = m_score1 - m_score2;
        if (minusResult == 1) {
            score = "Advantage " + this.player1Name;
        } else if (minusResult == -1) {
            score = "Advantage " + this.player2Name;
        } else if (minusResult >= 2) {
            score = "Win for " + this.player1Name;
        } else {
            score = "Win for " + this.player2Name;
        }
        return score;
    }

    private String scoreForTie() {
        switch (m_score1) {
            case LOVE:
                return "Love-All";
            case FIFTEEN:
                return "Fifteen-All";
            case THIRTY:
                return "Thirty-All";
            case FORTY:
                return "Forty-All";
            default:
                return "Deuce";
        }
    }

    private boolean tie() {
        return m_score1 == m_score2;
    }

    private boolean possibleWin() {
        return m_score1 >= OVER_FORTY || m_score2 >= OVER_FORTY;
    }
}
