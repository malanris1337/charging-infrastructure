package main;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Stream;

public class Main {


    public static void main(String[] args) throws Exception {
        fillFile();

        fillFileFull();
    }

    static void fillFile() throws IOException {
        try (
                Reader reader = Files.newBufferedReader(Paths.get("sad_kol (1).csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
                FileInputStream fis = new FileInputStream("plotnost.xlsx");
                Workbook wb = new XSSFWorkbook(fis);
        ) {
            Sheet sheet = wb.getSheet("Лист1");
            Row lastRow = sheet.getRow(sheet.getLastRowNum());
            double BC = lastRow.getCell(1).getNumericCellValue();
            double TC = lastRow.getCell(2).getNumericCellValue();
            double HOUSE = lastRow.getCell(3).getNumericCellValue();
            double UNIVER = lastRow.getCell(4).getNumericCellValue();
            double ROAD = lastRow.getCell(5).getNumericCellValue();
            double SHOP = lastRow.getCell(6).getNumericCellValue();

            List<String> dataLines = new ArrayList<>();
            String firstLine = ",width,longitude,type,average_people_value";
            dataLines.add(firstLine);
            boolean first = true;

            List<Coords> coords = new ArrayList<>();

            for (CSVRecord csvRecord : csvParser) {
                if (first) {
                    first = false;
                    continue;
                }

                String index = csvRecord.get(0);
                String coord1 = csvRecord.get(1);
                String coord2 = csvRecord.get(2);
                String type = csvRecord.get(3);
                String ludishki = "0";
                double peopleValue = 0.0;

                coords.add(new Coords(Double.parseDouble(coord1), Double.parseDouble(coord2), Double.parseDouble(ludishki)));

                if (type.equals("university")) {
                    peopleValue = UNIVER;
                } else if (type.equals("apartments")) {
                    peopleValue = HOUSE;
                } else if (type.equals("office")) {
                    peopleValue = BC;
                } else if (type.equals("road")) {
                    peopleValue = ROAD;
                } else if (type.equals("commercial")) {
                    peopleValue = TC;
                } else if (type.equals("shop")) {
                    peopleValue = SHOP;
                }else {
                    continue;
                }

                String line = String.join(",", index, coord1, coord2, type, String.valueOf(peopleValue));
                dataLines.add(line);
            }

            File csvOutputFile = new File("new.csv");
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                dataLines.forEach(pw::println);
            }
            System.out.println("success");
        }
    }


    static void fillFileFull() throws IOException {
        try (
                Reader reader = Files.newBufferedReader(Paths.get("new.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {

            List<Coords> coords = new ArrayList<>();

            boolean first = true;
            for (CSVRecord csvRecord : csvParser) {
                if (first) {
                    first = false;
                    continue;
                }

                String coord1 = csvRecord.get(1);
                String coord2 = csvRecord.get(2);
                String ludishki = csvRecord.get(4);

                coords.add(new Coords(Double.parseDouble(coord1), Double.parseDouble(coord2), Double.parseDouble(ludishki)));

            }

            double step = 0.001;

            Double minX = coords.stream().min(Comparator.comparing(v -> v.x)).get().x;
            Double maxX = coords.stream().max(Comparator.comparing(v -> v.x)).get().x;
            Double minY = Math.floor(coords.stream().min(Comparator.comparing(v -> v.y)).get().y * 1000) / 1000;
            Double maxY = coords.stream().max(Comparator.comparing(v -> v.y)).get().y;

            double startX = Math.floor(minX * 1000) / 1000;
            double startY = Math.floor(minY * 1000) / 1000;

            int stepXCount = (int) ((maxX - minX) / step) + 2;
            int stepYCount = (int) ((maxY - minY) / step) + 2;

            Data[][] twoDimArray = new Data[stepXCount][stepYCount];
            int x = 0, y = 0;
            for (double currentX = startX; currentX < maxX; currentX += step) {
                y = 0;
                for (double currentY = startY; currentY < maxY; currentY += step) {
                    Double loh = 0d;
                    for (int i = 0; i < coords.size(); i++) {
                        Coords currentCoord = coords.get(i);
                        if (currentCoord.x > currentX && currentCoord.x < currentX + step
                                && currentCoord.y > currentY && currentCoord.y < currentY + step) {
                            loh += currentCoord.ludishki;
                        }
                    }
                    twoDimArray[x][y] = new Data(currentX + step / 2, currentY + step / 2, loh);
                    y++;
                }
                x++;
            }

            String str = "";
            for (int i = 0; i < twoDimArray.length; i++) {
                for (int j = 0; j < twoDimArray[i].length; j++) {
                    if (twoDimArray[i][j] != null) {
                        str += String.join(";", twoDimArray[i][j].loh.toString(), twoDimArray[i][j].x.toString(), twoDimArray[i][j].y.toString()) + ',';
                    }
                }
                str += '\n';
            }


            File csvOutputFile = new File("result.csv");
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                pw.println(str);
            }
            /*File csvOutputFile = new File("new.csv");
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                dataLines.forEach(pw::println);
            }*/
            System.out.println("success");
        }
    }

    static class Coords {
        public Double x;
        public Double y;
        public Double ludishki;

        public Coords(Double x, Double y, Double ludishki) {
            this.x = x;
            this.y = y;
            this.ludishki = ludishki;
        }
    }


    static class Data {
        public Double x;
        public Double y;
        public Double loh;

        public Data(Double x, Double y, Double loh) {
            this.x = x;
            this.y = y;
            this.loh = loh;
        }
    }

}
