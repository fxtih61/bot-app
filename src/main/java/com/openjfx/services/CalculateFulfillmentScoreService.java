package com.openjfx.services;

import java.util.List;

public class CalculateFulfillmentScoreService {

    public double calculateFulfillmentScore(List<List<Integer>> studentWishes) {
        int totalStudents = studentWishes.size();
        int totalMaxScore = totalStudents * 20;
        int totalScore = 0;

        for (List<Integer> wishes : studentWishes) {
            int studentScore = 0;
            for (int i = 0; i < wishes.size(); i++) {
                int weight = 6 - i; // Gewichtung: 1. Wunsch = 6 Punkte, 2. Wunsch = 5 Punkte, ..., 6. Wunsch = 1 Punkt
                studentScore += wishes.get(i) * weight;
            }
            totalScore += studentScore;
        }

        if (totalMaxScore == 0) {
            return 0.0; // Vermeidung der Division durch Null
        }

        return (double) totalScore / totalMaxScore * 100; // Prozentualer Score
    }
}