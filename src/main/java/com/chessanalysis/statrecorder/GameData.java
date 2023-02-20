package com.chessanalysis.statrecorder;


import com.chessanalysis.util.MovePossibilitiesPair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class GameData implements MergeableData<GameData> {

    private int acceptedEnPassant;
    private int declinedEnPassant;
    private int enPassantMates;
    private int missedCheckmatePossibilities;
    private int missedCheckmates;
    private int missedCheckmateGames;
    private int totalGames;
    private final Map<String, FirstMove> moves;
    private final Map<MovePossibilitiesPair, Integer> possibleMoves;

    private final int TOP_LIST_SIZE = 100;
    private final Comparator<SingleGameStats> mostMissedMatesComparator;
    private final SortedSet<SingleGameStats> mostMissedMates;
    private final Comparator<SingleGameStats> mostEnPassantsComparator;
    private final SortedSet<SingleGameStats> mostEnPassants;
    private final Comparator<SingleGameStats> mostDeclinedEnPassantsComparator;
    private final SortedSet<SingleGameStats> mostDeclinedEnPassants;

    public GameData() {
        this.acceptedEnPassant = 0;
        this.declinedEnPassant = 0;
        this.enPassantMates = 0;
        this.missedCheckmates = 0;
        this.totalGames = 0;
        this.moves = new HashMap<>();
        this.possibleMoves = new HashMap<>();

        mostMissedMatesComparator = new MostMissedMatesComparator();
        this.mostMissedMates = new TreeSet<>(mostMissedMatesComparator);
        mostEnPassantsComparator = new MostEnPassantsComparator();
        this.mostEnPassants = new TreeSet<>(mostEnPassantsComparator);
        mostDeclinedEnPassantsComparator = new MostDeclinedEnPassantsComparator();
        this.mostDeclinedEnPassants = new TreeSet<>(mostDeclinedEnPassantsComparator);
    }

    private class MostMissedMatesComparator implements Comparator<SingleGameStats> {
        public int compare(SingleGameStats first, SingleGameStats second) {
            if (first.getMissedCheckmates() != second.getMissedCheckmates()) {
                return Integer.compare(first.getMissedCheckmates(), second.getMissedCheckmates());
            }
            return first.getGameLink().compareTo(second.getGameLink());
        }
    }

    private class MostEnPassantsComparator implements Comparator<SingleGameStats> {
        public int compare(SingleGameStats first, SingleGameStats second) {
            if (first.getAcceptedEnPassant() != second.getAcceptedEnPassant()) {
                return Integer.compare(first.getAcceptedEnPassant(), second.getAcceptedEnPassant());
            }
            return first.getGameLink().compareTo(second.getGameLink());
        }
    }

    private class MostDeclinedEnPassantsComparator implements Comparator<SingleGameStats> {
        public int compare(SingleGameStats first, SingleGameStats second) {
            if (first.getDeclinedEnPassant() != second.getDeclinedEnPassant()) {
                return Integer.compare(first.getDeclinedEnPassant(), second.getDeclinedEnPassant());
            }
            return first.getGameLink().compareTo(second.getGameLink());
        }
    }

    public void recordPossibleMoves(int moveNumber, int numPossibleMoves) {
        MovePossibilitiesPair idx = new MovePossibilitiesPair(moveNumber, numPossibleMoves);
        possibleMoves.merge(idx, 1, Integer::sum);
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

    public Map<String, FirstMove> getMoves() {
        return moves;
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

    public int getMissedCheckmatePossibilities() {
        return missedCheckmatePossibilities;
    }

    public int getMissedCheckmateGames() {
        return missedCheckmateGames;
    }

    public void incrementMissedCheckmates(int amount) {
        missedCheckmates += amount;
    }

    public void incrementMissedCheckmatePossibilities(int amount) {
        missedCheckmatePossibilities += amount;
    }

    public void incrementMissedCheckmateGames(int amount) {
        missedCheckmateGames += amount;
    }

    public void incrementTotalGames(int amount) {
        totalGames += amount;
    }

    public Map<MovePossibilitiesPair, Integer> getPossibleMoves() {
        return possibleMoves;
    }

    public SortedSet<SingleGameStats> getMostMissedMates() {
        return mostMissedMates;
    }

    public SortedSet<SingleGameStats> getMostEnPassants() {
        return mostEnPassants;
    }

    public SortedSet<SingleGameStats> getMostDeclinedEnPassants() {
        return mostDeclinedEnPassants;
    }

    @Override
    public GameData merge(GameData other) {
        incrementTotalGames(other.getTotalGames());
        incrementAcceptedEnPassant(other.getAcceptedEnPassant());
        incrementDeclinedEnPassant(other.getDeclinedEnPassant());
        incrementEnPassantMates(other.getEnPassantMates());
        incrementMissedCheckmates(other.getMissedCheckmates());
        incrementMissedCheckmatePossibilities(other.getMissedCheckmatePossibilities());
        incrementMissedCheckmateGames(other.getMissedCheckmateGames());
        incrementMissedCheckmates(other.getMissedCheckmates());
        for (Map.Entry<String, FirstMove> entry : other.getMoves().entrySet()) {
            String key = entry.getKey();
            FirstMove move = entry.getValue();
            moves.merge(key, move, FirstMove::merge);
        }
        for (SingleGameStats s : other.getMostMissedMates()) {
            checkTopList(mostMissedMates, mostMissedMatesComparator, s);
        }
        for (SingleGameStats s : other.getMostEnPassants()) {
            checkTopList(mostEnPassants, mostEnPassantsComparator, s);
        }
        for (SingleGameStats s : other.getMostDeclinedEnPassants()) {
            checkTopList(mostDeclinedEnPassants, mostDeclinedEnPassantsComparator, s);
        }
        for (Map.Entry<MovePossibilitiesPair, Integer> entry : other.getPossibleMoves().entrySet()) {
            MovePossibilitiesPair key = entry.getKey();
            possibleMoves.merge(key, entry.getValue(), Integer::sum);
        }
        return this;
    }

    private void checkTopList(SortedSet<SingleGameStats> set, Comparator<SingleGameStats> comparator,
            SingleGameStats singleGameStats) {
        if (set.size() < TOP_LIST_SIZE) {
            set.add(singleGameStats);
        } else if (comparator.compare(singleGameStats, set.first()) > 0) {
            set.remove(set.first());
            set.add(singleGameStats);
        }
    }

    public void addGame(SingleGameStats singleGameStats) {
        incrementTotalGames(singleGameStats.getTotalGames());
        incrementAcceptedEnPassant(singleGameStats.getAcceptedEnPassant());
        incrementDeclinedEnPassant(singleGameStats.getDeclinedEnPassant());
        incrementEnPassantMates(singleGameStats.getEnPassantMates());
        incrementMissedCheckmates(singleGameStats.getMissedCheckmates());
        incrementMissedCheckmateGames(singleGameStats.isHasMissedMate() ? 1 : 0);
        incrementMissedCheckmatePossibilities(singleGameStats.getMissedCheckmatePossibilities());
        checkTopList(mostMissedMates, mostMissedMatesComparator, singleGameStats);
        checkTopList(mostEnPassants, mostEnPassantsComparator, singleGameStats);
        checkTopList(mostDeclinedEnPassants, mostDeclinedEnPassantsComparator, singleGameStats);
    }
}
