package com.sky.service.impl;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate>dateList=new ArrayList<>();

        dateList.add(begin);
        for(int i=0;begin.plusDays(i).isBefore(end)||begin.plusDays(i).equals(end);i++){
                dateList.add(begin.plusDays(i));
        }
        List<Double>turnoverList=new ArrayList<>();
        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            turnover=turnover==null?0.0:turnover;
            turnoverList.add(turnover);
        }


        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate>dateList=new ArrayList<>();

        dateList.add(begin);
        for(int i=0;begin.plusDays(i).isBefore(end)||begin.plusDays(i).equals(end);i++){
            dateList.add(begin.plusDays(i));
        }
        List<Integer>newUserList=new ArrayList<>();
        List<Integer>totalUserList=new ArrayList<>();
        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Integer newUser = userMapper.selectNewUser(map);
            Integer totalUser = userMapper.selectTotalUser(map);
            newUserList.add(newUser);
            totalUserList.add(totalUser);
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate>dateList=new ArrayList<>();

        dateList.add(begin);
        for(int i=0;begin.plusDays(i).isBefore(end)||begin.plusDays(i).equals(end);i++){
            dateList.add(begin.plusDays(i));
        }

        List<Integer> validOrderCountList=new ArrayList<>();
        List<Integer> orderCountList=new ArrayList<>();

        Integer orderCount=0;
        Integer validOrderCount=0;
        for(LocalDate date:dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map=new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);

            //查询订单数量
            Integer orderCountTmp =orderMapper.getOrdersStatistics(map);
            orderCount+=orderCountTmp;
            map.put("status", Orders.COMPLETED);
            orderCountList.add(orderCountTmp);
            //查询有效订单数量
            Integer validOrderCountTemp=orderMapper.getOrdersStatistics(map);
            validOrderCount+=validOrderCountTemp;
            validOrderCountList.add(validOrderCountTemp);
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalOrderCount(orderCount)
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .validOrderCount(validOrderCount)
                .orderCompletionRate((double)validOrderCount/orderCount)
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        List<LocalDate>dateList=new ArrayList<>();

        dateList.add(begin);
        for(int i=0;begin.plusDays(i).isBefore(end)||begin.plusDays(i).equals(end);i++){
            dateList.add(begin.plusDays(i));
        }

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        Map map=new HashMap();
        map.put("begin", beginTime);
        map.put("end", endTime);

        List<String>dishs=orderDetailMapper.getTop10DishName(map);
        List<Integer>counts=orderDetailMapper.getTop10DishCount(map);

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(dishs,","))
                .numberList(StringUtils.join(counts,","))
                .build();

    }

    @Override
    public void export(HttpServletResponse response) {
       LocalDate begin=LocalDate.now().minusDays(30);
       LocalDate end=LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO=workspaceService.getBusinessData(LocalDateTime.of(begin,LocalTime.MIN),LocalDateTime.of(end,LocalTime.MAX));

        InputStream in= this.getClass().getClassLoader().getResourceAsStream("template/report.xlsx");


        try {
            XSSFWorkbook excel=new XSSFWorkbook(in);

            //时间
            XSSFSheet sheet=excel.getSheet("sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间："+begin+"-"+end);

            //营业额
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

             row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());


            for(int i=0;i<30;i++){
                LocalDate date=begin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row = sheet.getRow(7 + i);

                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
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
