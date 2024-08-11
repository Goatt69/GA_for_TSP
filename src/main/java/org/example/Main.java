/*
*   Author: Quốc Duy
*   Created: 25/7/2024
*
* */
package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static JFrame frame;
    private static JTextField populationSizeField;
    private static JTextField generationsField;
    private static File coordFile;
    private static File matrixFile;
    private static boolean useCostMatrix = true;

    public static void main(String[] args) {
        frame = new JFrame("TSP Route Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel fileLabel = new JLabel("Dữ liệu thành phố");
        JButton browseCoordButton = new JButton("Browse");
        JLabel matrixLabel = new JLabel("Chọn file ma trận Cost/Distance:");
        JButton browseMatrixButton = new JButton("Browse");
        JLabel populationLabel = new JLabel("Kích thước quần thể:");
        populationSizeField = new JTextField("50");
        JLabel generationsLabel = new JLabel("Số thế hệ:");
        generationsField = new JTextField("100");

        // Checkboxes with ButtonGroup to allow only one selection
        JCheckBox costMatrixCheckBox = new JCheckBox("Tối ưu chi phí", true);
        JCheckBox distanceMatrixCheckBox = new JCheckBox("Tối ưu quãng đường", false);
        ButtonGroup optimizationGroup = new ButtonGroup();
        optimizationGroup.add(costMatrixCheckBox);
        optimizationGroup.add(distanceMatrixCheckBox);

        JButton generateButton = new JButton("Generate Route");

        controlPanel.add(fileLabel);
        controlPanel.add(browseCoordButton);
        controlPanel.add(matrixLabel);
        controlPanel.add(browseMatrixButton);
        controlPanel.add(populationLabel);
        controlPanel.add(populationSizeField);
        controlPanel.add(generationsLabel);
        controlPanel.add(generationsField);
        controlPanel.add(costMatrixCheckBox);
        controlPanel.add(distanceMatrixCheckBox);
        controlPanel.add(generateButton);

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(generateButton, BorderLayout.SOUTH);
        frame.add(scrollPane, BorderLayout.CENTER);

        browseCoordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    coordFile = fileChooser.getSelectedFile();
                }
            }
        });

        browseMatrixButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    matrixFile = fileChooser.getSelectedFile();
                }
            }
        });

        costMatrixCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useCostMatrix = costMatrixCheckBox.isSelected();
            }
        });

        distanceMatrixCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (distanceMatrixCheckBox.isSelected()) {
                    useCostMatrix = false;
                }
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (coordFile != null && matrixFile != null) {
                    try {
                        List<double[]> coords = new ArrayList<>();
                        List<String> names = new ArrayList<>();
                        BufferedReader coordReader = new BufferedReader(new FileReader(coordFile));
                        String line;
                        while ((line = coordReader.readLine()) != null) {
                            String[] parts = line.trim().split("\\s+",3);
                            if (parts.length < 3) {
                                System.err.println("Invalid line: " + line);
                                continue;
                            }
                            try {
                                double lat = Double.parseDouble(parts[0]);
                                double lon = Double.parseDouble(parts[1]);
                                String name = parts[2];
                                coords.add(new double[]{lat, lon});
                                names.add(name);
                            } catch (NumberFormatException nfe) {
                                System.err.println("Invalid coordinates: " + parts[0] + ", " + parts[1]);
                            }
                        }
                        coordReader.close();

                        // Read matrix file
                        double[][] matrix = readMatrixFile(matrixFile, coords.size());

                        int populationSize = Integer.parseInt(populationSizeField.getText());
                        int generations = Integer.parseInt(generationsField.getText());

                        GeneticAlgorithm ga = new GeneticAlgorithm(coords, matrix, populationSize, generations, 0.01, true, useCostMatrix);
                        long[] executionTimes = ga.evolve();


                        int[] bestSolution = ga.getBestSolution();
                        double totalCostOrDistance = ga.getBestCostOrDistance();

                        MapViewer.displayMap(coords, names, bestSolution);
                        outputArea.setText(getRouteDetails(names, bestSolution, totalCostOrDistance, useCostMatrix, executionTimes));

                    } catch (IOException | NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Please select both a coordinates file and a matrix file!");
                }
            }
        });

        frame.setVisible(true);
    }

    private static double[][] readMatrixFile(File matrixFile, int size) throws IOException {
        double[][] matrix = new double[size][size];
        BufferedReader matrixReader = new BufferedReader(new FileReader(matrixFile));
        String line;
        int row = 0;
        while ((line = matrixReader.readLine()) != null && row < size) {
            String[] parts = line.trim().split("\\s+");
            for (int col = 0; col < parts.length; col++) {
                matrix[row][col] = Double.parseDouble(parts[col]);
            }
            row++;
        }
        matrixReader.close();
        return matrix;
    }

    private static String getRouteDetails(List<String> names, int[] bestSolution, double totalCostOrDistance, boolean isCostMatrix, long[] executionTimes) {
        long startTime = executionTimes[0];
        long endTime = executionTimes[1];
        long executionTime = endTime - startTime;

        StringBuilder details = new StringBuilder("Route details:\n");
        for (int i = 0; i < bestSolution.length; i++) {
            int index = bestSolution[i];
            details.append(names.get(index));
            if (i < bestSolution.length - 1) {
                details.append(" --> ");
            } else {
                details.append(" --> ").append(names.get(bestSolution[0])); // Return to start point
            }
        }
        String metric = isCostMatrix ? "Total Cost: " : "Total Distance: ";
        details.append("\n").append(metric).append(totalCostOrDistance);
        details.append("\nTotal Execution Time: ").append(executionTime).append(" ms");
        return details.toString();
    }
}