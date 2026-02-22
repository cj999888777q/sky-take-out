package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.service.WorkSpaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkSpaceService workSpaceService;


    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> list = new ArrayList<>();

        list.add(begin);

        while (!begin.equals(end))
        {
            begin = begin.plusDays(1);

            list.add(begin);
        }

        List<Double> doubleList = new ArrayList<>();

        for(LocalDate date : list)
        {
            LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();

            map.put("status", Orders.COMPLETED);
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);

            Double num =orderMapper.turnoverStatistics(map);

            if(num==null)
            {
                num=0.0;
            }

            doubleList.add(num);

        }

        String dateList = StringUtils.join(list,",");
        String turnoverList=StringUtils.join(doubleList,",");

        return TurnoverReportVO.builder()
                .dateList(dateList)
                .turnoverList(turnoverList)
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
           List<LocalDate> list = new ArrayList<>();

           list.add(begin);

           while(!begin.equals(end))
           {
               begin = begin.plusDays(1);
               list.add(begin);
           }

           List<Integer> newUserList = new ArrayList<>();
           List<Integer> totalUserList = new ArrayList<>();

           for(LocalDate date : list)
           {
               LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);
               LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

               Map map1 = new HashMap();

               map1.put("beginTime",beginTime);
               map1.put("endTime",endTime);

               Map map2 = new HashMap();
               map2.put("endTime",endTime);

               Integer num = userMapper.userStatistics(map1);
               Integer num2 = userMapper.userStatistics(map2);

               if(num==null)
               {
                   num=0;
               }

               if(num2==null)
               {
                   num2=0;
               }

               newUserList.add(num);
               totalUserList.add(num2);

           }

           return UserReportVO.builder()
                   .dateList(StringUtils.join(list,","))
                   .newUserList(StringUtils.join(newUserList,","))
                   .totalUserList(StringUtils.join(totalUserList,","))
                   .build();

    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;

        for(LocalDate date : dateList)
        {
            LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map1 = new HashMap();

            map1.put("beginTime",beginTime);
            map1.put("endTime",endTime);

            Integer num1 = orderMapper.ordersStatistics(map1);
            if(num1==null)
            {
                num1=0;
            }

            totalOrderCount += num1;
            orderCountList.add(num1);

            Map map2 = new HashMap();
            map2.put("status",Orders.COMPLETED);
            map2.put("beginTime",beginTime);
            map2.put("endTime",endTime);

            Integer num2 = orderMapper.ordersStatistics(map2);

            if(num2==null)
            {
                num2=0;
            }
            validOrderCount+=num2;
            validOrderCountList.add(num2);

        }

        Double orderCompletionRate = ((double)validOrderCount/totalOrderCount);

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);

        Map map = new HashMap();
        map.put("status",Orders.COMPLETED);
        map.put("beginTime",beginTime);
        map.put("endTime",endTime);


        List<GoodsSalesDTO> lsit = orderMapper.top10(map);

        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();

        for(GoodsSalesDTO goodsSalesDTO : lsit )
        {
            nameList.add(goodsSalesDTO.getName());
            Integer number = goodsSalesDTO.getNumber();
            if(number==null)
            {
                number=0;
            }
            numberList.add(number);
        }

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();



    }

    @Override
    public void exportBusinessDate(HttpServletResponse response) {

        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        BusinessDataVO businessDataVO = workSpaceService.businessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营.xlsx");

        try {

                XSSFWorkbook excel = new XSSFWorkbook(inputStream);

                XSSFSheet sheet = excel.getSheet("sheet1");

                // 4. 创建居中样式（基于已有workbook，关键！）
                XSSFCellStyle centerStyle = excel.createCellStyle();

                centerStyle.setAlignment(HorizontalAlignment.CENTER);
                centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

                XSSFCell cell = sheet.getRow(1).getCell(1);

                cell.setCellStyle(centerStyle);
                cell.setCellValue("时间:"+begin+"至"+end);


                XSSFRow row = sheet.getRow(3);

                row.getCell(2).setCellValue(businessDataVO.getTurnover());
                row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                row.getCell(6).setCellValue(businessDataVO.getNewUsers());

                row = sheet.getRow(4);
                row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

                for(int i=0;i<30;i++)
                {
                    XSSFRow row1 = sheet.getRow(i+7);

                    LocalDate gg = begin.plusDays(i);

                    BusinessDataVO businessDataVO1 = workSpaceService.businessData(LocalDateTime.of(gg, LocalTime.MIN), LocalDateTime.of(gg, LocalTime.MAX));

                    row1.getCell(1).setCellValue(String.valueOf(gg));
                    row1.getCell(2).setCellValue(businessDataVO1.getTurnover());
                    row1.getCell(3).setCellValue(businessDataVO1.getValidOrderCount());
                    row1.getCell(4).setCellValue(businessDataVO1.getOrderCompletionRate());
                    row1.getCell(5).setCellValue(businessDataVO1.getUnitPrice());
                    row1.getCell(6).setCellValue(businessDataVO1.getNewUsers());

                }

                ServletOutputStream outputStream = response.getOutputStream();

                excel.write(outputStream);

                outputStream.close();
                excel.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


}
