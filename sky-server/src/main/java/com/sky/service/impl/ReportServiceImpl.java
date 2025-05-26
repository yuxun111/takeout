package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
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
}
