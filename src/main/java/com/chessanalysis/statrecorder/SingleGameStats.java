package com.chessanalysis.statrecorder;

public class SingleGameStats {

    private int acceptedEnPassant;
    private int declinedEnPassant;
    private int enPassantMates;
    private int missedCheckmatePossibilities;
    private int missedCheckmates;
    private boolean hasMissedMate;
    private int totalGames;
    private final String gameLink;

    public SingleGameStats(String gameLink) {
        this.acceptedEnPassant = 0;
        this.declinedEnPassant = 0;
        this.enPassantMates = 0;
        this.missedCheckmates = 0;
        this.totalGames = 0;
        this.gameLink = gameLink;
        this.hasMissedMate = false;
    }
    public int getMissedCheckmatePossibilities() {
        return missedCheckmatePossibilities;
    }

    public boolean isHasMissedMate() {
        return hasMissedMate;
    }

    public String getGameLink() {
        return gameLink;
    }

    public int getAcceptedEnPassant() {
        return acceptedEnPassant;
    }

    public int getDeclinedEnPassant() {
        return declinedEnPassant;
    }

    public int getEnPassantMates() {
        return enPassantMates;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public void incrementAcceptedEnPassant(int amount) {
        acceptedEnPassant += amount;
    }

    public void incrementDeclinedEnPassant(int amount) {
        declinedEnPassant += amount;
    }

    public void incrementEnPassantMates(int amount) {
        enPassantMates += amount;
    }

    public int getMissedCheckmates() {
        return missedCheckmates;
    }

    public void incrementMissedCheckmates(int amount) {
        missedCheckmates += amount;
    }

    public void incrementMissedCheckmatePossibilities(int amount) {
        missedCheckmatePossibilities += amount;
    }

    public void setHasMissedMate(boolean value) {
        hasMissedMate = value;
    }

    public void incrementTotalGames(int amount) {
        totalGames += amount;
    }
}
