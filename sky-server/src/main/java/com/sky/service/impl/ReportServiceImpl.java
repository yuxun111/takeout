package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计,给定区间
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        //存放从begin到end的日期
        List<LocalDate> datelist=new ArrayList<>();

        datelist.add(begin);
        while (!begin .equals(end)){
            //计算指定日期的后一天
            begin = begin.plusDays(1);
            datelist.add(begin);
        }

        //存放营业额
        List<Double> turnoverlist=new ArrayList<>();
        for (LocalDate date : datelist) {
            //查询指定日期的营业额,状态为已完成，5
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverlist.add(turnover);

        }

        //封装返回结果
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(datelist, ","))
                .turnoverList(StringUtils.join(turnoverlist, ","))
                .build();
    }

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //存放从begin到end的日期
        List<LocalDate> datelist=new ArrayList<>();

        datelist.add(begin);
        while (!begin .equals(end)){
            //计算指定日期的后一天
            begin = begin.plusDays(1);
            datelist.add(begin);
        }

        //存放新用户，每天的
        List<Integer> newUserList = new ArrayList<>();
        //存放总用户。每天的
        List<Integer> totalUserList = new ArrayList<>();

        for(LocalDate date:datelist){

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();

            map.put("end",endTime);
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin",beginTime);
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);

        }



        return UserReportVO.builder()
                .dateList(StringUtils.join(datelist,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    /**
     * 统计指定区间内的订单数据
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {

        //存放从begin到end的日期
        List<LocalDate> datelist=new ArrayList<>();

        datelist.add(begin);
        while (!begin .equals(end)){
            //计算指定日期的后一天
            begin = begin.plusDays(1);
            datelist.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        //遍历datelist查询有效订单
        for (LocalDate date : datelist) {

            //查询订单总数
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime, endTime, null);


            //查询订单有效数
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        //计算区间内的订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();


        //计算时间区间内有效订单数
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
           orderCompletionRate =validOrderCount.doubleValue() / totalOrderCount;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(datelist, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)

                .build();
    }

    /**
     * 根据条件统计订单数
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private  Integer getOrderCount(LocalDateTime begin, LocalDateTime end,Integer status){
        Map map=new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);

        return  orderMapper.countByMap(map);

    }
}
