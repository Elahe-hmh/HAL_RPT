package GrowthModel_SSDiff;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VesselConfigConvertor {
    public double[][] grid;

    // Constructor to initialize the grid from a CSV file
    public VesselConfigConvertor(String filePath) throws IOException {
        List<double[]> tempGrid = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                double[] row = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    // Parse the value as a double
                    row[i] = Double.parseDouble(values[i]);
                }
                tempGrid.add(row);
            }
        }
        grid = tempGrid.toArray(new double[0][]);
    }

    // Method to print the grid
    public void printGrid() {
        for (double[] row : grid) {
            for (double val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }

    // Main method to test the functionality
    public static void main(String[] args) {
        try {
            // Create a Grid object from the CSV file and print the grid
            VesselConfigConvertor grid = new VesselConfigConvertor("array.csv");
            grid.printGrid();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

