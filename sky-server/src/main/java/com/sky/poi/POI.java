package com.sky.poi;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class POI {


    public static void write() throws IOException {

        XSSFWorkbook excel = new XSSFWorkbook();

        XSSFSheet sheet = excel.createSheet("111");

        XSSFRow row0 = sheet.createRow(0);

        row0.createCell(0).setCellValue("姓名");
        row0.createCell(1).setCellValue("陈剑");

        XSSFRow row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("城市");
        row1.createCell(1).setCellValue("九江");

        FileOutputStream stream = new FileOutputStream(new File("E:\\111.xlsx"));

        excel.write(stream);

        stream.close();
        excel.close();




    }

    public static void read() throws IOException {

        FileInputStream stream = new FileInputStream(new File("E:\\111.xlsx"));

        XSSFWorkbook excel = new XSSFWorkbook(stream);

        XSSFSheet sheet = excel.getSheetAt(0);

        int lastRowNum = sheet.getLastRowNum();

        for(int i=0;i<=lastRowNum;i++){

            XSSFRow row = sheet.getRow(i);

            String a = row.getCell(0).getStringCellValue();
            String b = row.getCell(1).getStringCellValue();

            System.out.println(a+":"+b);
        }

        stream.close();
        excel.close();
    }

    public static void main(String[] args) throws IOException {
        //write();
        read();
    }
}
