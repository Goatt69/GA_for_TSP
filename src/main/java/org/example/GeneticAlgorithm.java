/*
 *   Author: Văn Quyết
 *   Created: 21/7/2024
 *
 * */
package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithm {
    private List<double[]> coords; // Danh sách tọa độ của các thành phố
    private double bestValue; // Giá trị tốt nhất (chi phí hoặc khoảng cách)
    private int generations; // Số thế hệ

    private int populationSize; // Kích thước quần thể
    private List<int[]> population; // Quần thể

    private double mutationRate; // Tỷ lệ đột biến
    private boolean elitism; // Sử dụng elitism hay không
    private int sampleSize; // Số lượng mẫu (điểm)
    private double[][] matrix; // Ma trận chi phí hoặc khoảng cách

    private int[] bestSolution; // Giải pháp tốt nhất
    private List<int[]> solutionHistory; // Lịch sử các giải pháp
    private boolean useCostMatrix; // Có sử dụng ma trận chi phí hay không

    public GeneticAlgorithm(List<double[]> coords, double[][] matrix, int populationSize, int generations, double mutationRate, boolean elitism, boolean useCostMatrix) {
        this.coords = coords; // Thiết lập tọa độ
        this.matrix = matrix; // Thiết lập ma trận (chi phí hoặc khoảng cách)
        this.populationSize = populationSize; // Thiết lập kích thước quần thể
        this.generations = generations; // Thiết lập số thế hệ
        this.mutationRate = mutationRate; // Thiết lập tỷ lệ đột biến
        this.elitism = elitism; // Thiết lập cờ elitism
        this.useCostMatrix = useCostMatrix; // Thiết lập cờ sử dụng ma trận chi phí

        this.sampleSize = coords.size(); // Thiết lập số lượng mẫu
        this.population = createPopulation(); // Tạo quần thể ban đầu

        this.bestSolution = null; // Khởi tạo giải pháp tốt nhất là null
        this.bestValue = Double.MAX_VALUE; // Khởi tạo giá trị tốt nhất là giá trị double tối đa
        this.solutionHistory = new ArrayList<>(); // Khởi tạo lịch sử các giải pháp
    }

    // Hàm tạo một lộ trình ngẫu nhiên (giải pháp)
    private int[] createRoute() {
        List<Integer> route = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            route.add(i);
        }
        Collections.shuffle(route); // Xáo trộn các điểm ngẫu nhiên
        return route.stream().mapToInt(i -> i).toArray(); // Chuyển đổi sang mảng int[]
    }

    // Hàm tạo quần thể ban đầu
    private List<int[]> createPopulation() {
        List<int[]> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            population.add(createRoute());
        }
        return population;
    }

    // Hàm tính toán độ thích nghi của một lộ trình
    private double fitness(int[] route) {
        double totalValue = 0;
        for (int i = 0; i < route.length - 1; i++) {
            totalValue += matrix[route[i]][route[i + 1]];
        }
        totalValue += matrix[route[route.length - 1]][route[0]]; // Quay lại điểm xuất phát
        return 1 / totalValue; // Nghịch đảo giá trị tổng để tính độ thích nghi
    }

    // Hàm chọn một cá thể dựa trên độ thích nghi
    private int[] selectParent(double totalFitness, double[] fitnesses) {
        double pick = new Random().nextDouble() * totalFitness;
        double current = 0;
        for (int i = 0; i < fitnesses.length; i++) {
            current += fitnesses[i];
            if (current > pick) {
                return population.get(i);
            }
        }
        return population.get(0); // Nếu không chọn được, trả về cá thể đầu tiên
    }

    // Hàm crossover để kết hợp hai cá thể thành một đứa con
    private int[] crossover(int[] parent1, int[] parent2) {
        Random rand = new Random();
        int start = rand.nextInt(parent1.length);
        int end = rand.nextInt(parent1.length - start) + start;
        int[] child = new int[parent1.length];
        for (int i = 0; i < child.length; i++) {
            child[i] = -1; // Khởi tạo với giá trị -1
        }

        // Sao chép một đoạn từ parent1
        for (int i = start; i < end; i++) {
            child[i] = parent1[i];
        }

        // Điền các gen còn lại từ parent2
        int j = 0;
        for (int i = 0; i < child.length; i++) {
            if (child[i] == -1) {
                while (contains(child, parent2[j])) {
                    j++;
                }
                child[i] = parent2[j++];
            }
        }

        return child;
    }

    // Kiểm tra xem một mảng có chứa một giá trị cụ thể không
    private boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }

    // Hàm đột biến một lộ trình
    private void mutate(int[] route) {
        Random rand = new Random();
        for (int i = 0; i < route.length; i++) {
            if (rand.nextDouble() < mutationRate) {
                int j = rand.nextInt(route.length);
                int temp = route[i];
                route[i] = route[j];
                route[j] = temp;
            }
        }
    }

    // Tiến hóa quần thể qua các thế hệ
    public long[] evolve() {
        long startTime = System.currentTimeMillis();
        for (int generation = 0; generation < generations; generation++) {
            List<int[]> newPopulation = new ArrayList<>();
            double[] fitnesses = new double[populationSize];
            double totalFitness = 0;

            for (int i = 0; i < populationSize; i++) {
                fitnesses[i] = fitness(population.get(i));
                totalFitness += fitnesses[i];
            }

            if (elitism) {
                int bestIndex = 0;
                for (int i = 1; i < populationSize; i++) {
                    if (fitnesses[i] > fitnesses[bestIndex]) {
                        bestIndex = i;
                    }
                }
                newPopulation.add(population.get(bestIndex));
            }

            while (newPopulation.size() < populationSize) {
                int[] parent1 = selectParent(totalFitness, fitnesses);
                int[] parent2 = selectParent(totalFitness, fitnesses);
                int[] child = crossover(parent1, parent2);
                mutate(child);
                newPopulation.add(child);
            }


            population = newPopulation;
            double currentBestValue = 1 / fitness(population.get(0));
            if (currentBestValue < bestValue) {
                bestValue = currentBestValue;
                bestSolution = population.get(0);
                solutionHistory.add(bestSolution);
            }

            System.out.printf("Generation %d: Best %s = %.2f \n", generation, useCostMatrix ? "Cost" : "Distance",bestValue);
        }
        long endTime = System.currentTimeMillis(); // End time
        long executionTime = endTime - startTime; // Total execution time
        System.out.printf("Total Execution Time: %d ms\n", executionTime);
        return new long[]{startTime, endTime};
    }

    // Trả về giải pháp tốt nhất
    public int[] getBestSolution() {
        return bestSolution;
    }

    // Trả về chi phí hoặc khoảng cách tốt nhất
    public double getBestCostOrDistance() {
        return bestValue;
    }
}
