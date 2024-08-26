package GrowthModel_SSDiff;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class DataLogger {

    public DataLogger() {
    }

    public void log(ArrayList<double[]> list, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))) {
            for (double[] element : list) {
                writer.write(convertToCSV(element));
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    public void saveFigureTotal(String fileName, DaVinci drawer) {
        BufferedImage img = new BufferedImage(drawer.gridWin.xDim, drawer.gridWin.yDim, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < drawer.gridWin.xDim; x++) {
            for (int y = 0; y < drawer.gridWin.yDim; y++) {
                int color = drawer.gridWin.GetPix(x,y);
                img.setRGB(x, y, color);
            }
        }
        try{
            ImageIO.write(img,"png",new File(fileName));
        }catch (IIOException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Image Saved ...");
    }

//    public void saveFigureOx(String fileName, DaVinci drawer) {
//        BufferedImage img = new BufferedImage(drawer.gridWinOxygen.xDim, drawer.gridWinOxygen.yDim, BufferedImage.TYPE_INT_RGB);
//        for (int x = 0; x < drawer.gridWinOxygen.xDim; x++) {
//            for (int y = 0; y < drawer.gridWinOxygen.yDim; y++) {
//                int color = drawer.gridWinOxygen.GetPix(x,y);
//                img.setRGB(x, y, color);
//            }
//        }
//        try{
//            ImageIO.write(img,"png",new File(fileName));
//        }catch (IIOException e){
//            e.printStackTrace();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("Image Saved ...");
//    }

//    public void saveFigureVessels(String fileName, DaVinci drawer) {
//        BufferedImage img = new BufferedImage(drawer.gridWinVessel.xDim, drawer.gridWinVessel.yDim, BufferedImage.TYPE_INT_RGB);
//        for (int x = 0; x < drawer.gridWinVessel.xDim; x++) {
//            for (int y = 0; y < drawer.gridWinVessel.yDim; y++) {
//                int color = drawer.gridWinVessel.GetPix(x,y);
//                img.setRGB(x, y, color);
//            }
//        }
//        try{
//            ImageIO.write(img,"png",new File(fileName));
//        }catch (IIOException e){
//            e.printStackTrace();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        System.out.println("Image Saved ...");
//    }

    public void saveVessel(String fileName, DaVinci drawer) {
        BufferedImage img = new BufferedImage(drawer.gridWin.xDim, drawer.gridWin.yDim, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < drawer.gridWin.xDim; x++) {
            for (int y = 0; y < drawer.gridWin.yDim; y++) {
                int color = drawer.gridWin.GetPix(x,y);
                img.setRGB(x, y, color);
            }
        }
        try{
            ImageIO.write(img,"png",new File(fileName));
        }catch (IIOException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Image Saved ...");
    }

    private String convertToCSV(double[] element) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < element.length; i++) {
            sb.append(element[i]);
            if (i < element.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

//    public static void main(String[] args) {
//        DataLogger logger = new DataLogger();
//        ArrayList<double[]> testList = new ArrayList<>();
//
//        testList.add(new double[]{1.1, 2.2, 3.3});
//        testList.add(new double[]{4.4, 5.5, 6.6});
//
//        logger.log(testList);
//
//        testList.add(new double[]{7.7, 8.8, 9.9});
//
//        logger.log(testList);
//    }
}
