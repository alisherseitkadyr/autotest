package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;

public class ExcelReader {

    public static Object[][] readSheet(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                StringBuilder availableSheets = new StringBuilder();
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    availableSheets.append(workbook.getSheetName(i)).append(", ");
                }
                throw new RuntimeException("Sheet '" + sheetName + "' not found in workbook. Available sheets: " + availableSheets.toString());
            }
            int rows = sheet.getPhysicalNumberOfRows();
            int cols = sheet.getRow(0).getPhysicalNumberOfCells();

            // rows-1 because header row is row0
            Object[][] data = new Object[rows - 1][cols];

            for (int i = 1; i < rows; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < cols; j++) {
                    Cell cell = (row == null) ? null : row.getCell(j);
                    data[i - 1][j] = getCellValueAsString(cell);
                }
            }
            return data;

        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel: " + e.getMessage(), e);
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}
