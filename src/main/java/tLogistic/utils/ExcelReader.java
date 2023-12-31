package tLogistic.utils;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExcelReader {
    public HashMap<Integer, List<Object>> read(String filename) throws IOException {
        HashMap<Integer, List<Object>> articleData = new HashMap<>();
        Workbook workbook = loadWorkbook(filename);
        var sheetIterator = workbook.sheetIterator();
        while (sheetIterator.hasNext()) {
            Sheet sheet = sheetIterator.next();
            articleData.putAll(processSheet(sheet));
        }
        return articleData;
    }

    private Workbook loadWorkbook(String filename) throws IOException {
        var extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        var file = new FileInputStream(new File(filename));
        switch (extension) {
            case "xls":
                // old format
                return new HSSFWorkbook(file);
            case "xlsx":
                // new format
                return new XSSFWorkbook(file);
            default:
                throw new RuntimeException("Unknown Excel file extension: " + extension);
        }
    }

    private HashMap<Integer, List<Object>> processSheet(Sheet sheet) {
        var data = new HashMap<Integer, List<Object>>();
        var iterator = sheet.rowIterator();
        for (var rowIndex = 0; iterator.hasNext(); rowIndex++) {
            Row row = iterator.next();
            processRow(data, rowIndex, row);
        }
        return data;
    }

    private void processRow(HashMap<Integer, List<Object>> data, int rowIndex, Row row) {
        data.put(rowIndex, new ArrayList<>());
        for (Cell cell : row) {
            processCell(cell, data.get(rowIndex));
        }
    }

    private void processCell(Cell cell, List<Object> dataRow) {
        switch (cell.getCellType()) {
            case STRING:
                dataRow.add(cell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    dataRow.add(cell.getLocalDateTimeCellValue());
                } else {
                    dataRow.add(NumberToTextConverter.toText(cell.getNumericCellValue()));
                }
                break;
            case BOOLEAN:
                dataRow.add(cell.getBooleanCellValue());
                break;
            case FORMULA:
                dataRow.add(cell.getCellFormula());
                break;
            default:
                dataRow.add(" ");
        }
    }


}
