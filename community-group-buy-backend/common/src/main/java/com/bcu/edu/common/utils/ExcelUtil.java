package com.bcu.edu.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel导出工具类
 * 使用Apache POI实现Excel文件导出
 */
@Slf4j
public class ExcelUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出操作日志为Excel
     *
     * @param logs 日志数据列表
     * @return Excel文件的字节数组
     */
    public static <T> byte[] exportOperationLogs(List<T> logs) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 创建工作表
            Sheet sheet = workbook.createSheet("操作日志");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建数据样式
            CellStyle dataStyle = createDataStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "操作时间", "操作人", "操作内容", "操作模块",
                    "操作结果", "IP地址", "执行时长(ms)", "错误信息"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据（使用反射获取字段）
            for (int i = 0; i < logs.size(); i++) {
                Row row = sheet.createRow(i + 1);
                T logData = logs.get(i);

                try {
                    // 操作时间
                    Object createTime = getFieldValue(logData, "createTime");
                    Cell cell0 = row.createCell(0);
                    if (createTime instanceof LocalDateTime) {
                        cell0.setCellValue(((LocalDateTime) createTime).format(DATE_FORMATTER));
                    } else {
                        cell0.setCellValue(createTime != null ? createTime.toString() : "");
                    }
                    cell0.setCellStyle(dataStyle);

                    // 操作人
                    Object username = getFieldValue(logData, "username");
                    Cell cell1 = row.createCell(1);
                    cell1.setCellValue(username != null ? username.toString() : "");
                    cell1.setCellStyle(dataStyle);

                    // 操作内容
                    Object operation = getFieldValue(logData, "operation");
                    Cell cell2 = row.createCell(2);
                    cell2.setCellValue(operation != null ? operation.toString() : "");
                    cell2.setCellStyle(dataStyle);

                    // 操作模块
                    Object module = getFieldValue(logData, "module");
                    Cell cell3 = row.createCell(3);
                    cell3.setCellValue(module != null ? module.toString() : "");
                    cell3.setCellStyle(dataStyle);

                    // 操作结果
                    Object result = getFieldValue(logData, "result");
                    Cell cell4 = row.createCell(4);
                    cell4.setCellValue(result != null ? result.toString() : "");
                    cell4.setCellStyle(dataStyle);

                    // IP地址
                    Object ip = getFieldValue(logData, "ip");
                    Cell cell5 = row.createCell(5);
                    cell5.setCellValue(ip != null ? ip.toString() : "");
                    cell5.setCellStyle(dataStyle);

                    // 执行时长
                    Object duration = getFieldValue(logData, "duration");
                    Cell cell6 = row.createCell(6);
                    if (duration != null) {
                        cell6.setCellValue(duration.toString());
                    } else {
                        cell6.setCellValue("");
                    }
                    cell6.setCellStyle(dataStyle);

                    // 错误信息
                    Object errorMsg = getFieldValue(logData, "errorMsg");
                    Cell cell7 = row.createCell(7);
                    cell7.setCellValue(errorMsg != null ? errorMsg.toString() : "");
                    cell7.setCellStyle(dataStyle);

                } catch (Exception e) {
                    log.error("导出Excel行数据失败: row={}", i, e);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小宽度和最大宽度
                int columnWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(Math.max(columnWidth, 3000), 15000));
            }

            // 写入字节数组输出流
            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("导出Excel失败", e);
            throw e;
        }
    }

    /**
     * 创建表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        return style;
    }

    /**
     * 创建数据样式
     */
    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // 设置自动换行
        style.setWrapText(true);

        return style;
    }

    /**
     * 使用反射获取对象字段值
     */
    private static Object getFieldValue(Object obj, String fieldName) {
        try {
            // 优先使用getter方法
            String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            return obj.getClass().getMethod(getterName).invoke(obj);
        } catch (Exception e) {
            try {
                // 尝试直接访问字段
                var field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception ex) {
                log.warn("获取字段值失败: {}", fieldName);
                return null;
            }
        }
    }
}

