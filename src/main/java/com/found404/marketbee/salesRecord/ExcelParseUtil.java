package com.found404.marketbee.salesRecord;

import org.apache.poi.ss.usermodel.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.apache.poi.ss.usermodel.DataFormatter;
import java.util.Optional;

public class ExcelParseUtil {
    public static int findColumnIndex(Row headerRow, String... keywords) {
        for (Cell cell : headerRow) {
            String headerValue = getCellStringValue(cell).replaceAll("\\s+", "");
            for (String keyword : keywords) {
                if (headerValue.contains(keyword)) {
                    return cell.getColumnIndex();
                }
            }
        }
        return -1;
    }

    public static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    public static Integer getCellIntValue(Cell cell) {
        if (cell == null) return 0;

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        try {
            String stringValue = new DataFormatter().formatCellValue(cell).trim();
            return Integer.parseInt(stringValue.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static Optional<LocalTime> getCellTimeValue(Cell cell) {
        if (cell == null) return Optional.empty();
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return Optional.of(cell.getLocalDateTimeCellValue().toLocalTime());
            } else if (cell.getCellType() == CellType.STRING) {
                String timeStr = cell.getStringCellValue().trim();
                if (timeStr.matches("\\d{2}:\\d{2}:\\d{2}")) {
                    return Optional.of(LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm:ss")));
                } else if (timeStr.matches("\\d{2}:\\d{2}")) {
                    return Optional.of(LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm")));
                }
            }
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    public static Long getCellLongValue(Cell cell) {
        if (cell == null) return 0L;

        if (cell.getCellType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
            return (long) cell.getNumericCellValue();
        }
        try {
            String stringValue = new DataFormatter().formatCellValue(cell).trim();
            return Long.parseLong(stringValue.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public static boolean isSummaryOrEmptyRow(Row row) {
        if (row == null) return true;
        Cell firstCell = row.getCell(0);
        if (firstCell == null || firstCell.getCellType() == CellType.BLANK) return true;

        return getCellStringValue(firstCell).contains("합계");
    }
}