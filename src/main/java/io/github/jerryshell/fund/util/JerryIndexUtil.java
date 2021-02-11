package io.github.jerryshell.fund.util;

import io.github.jerryshell.fund.entity.FundGrowth;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class JerryIndexUtil {
    public static BigDecimal calculateByFundGrowthList(List<FundGrowth> fundGrowthList) {
        log.info("fundGrowthList {}", fundGrowthList);

        // dateStr 倒序排序
        fundGrowthList.sort((fundGrowth1, fundGrowth2) -> fundGrowth2.getDateStr().compareTo(fundGrowth1.getDateStr()));
        log.info("fundGrowthList after sort {}", fundGrowthList);

        // fundGrowthList -> fundGrowthValueList
        List<BigDecimal> fundGrowthValueList = fundGrowthList.stream()
                .map(FundGrowth::getGrowth)
                .collect(Collectors.toList());
        log.info("fundGrowthValueList {}", fundGrowthValueList);

        return calculateByFundGrowthValueList(fundGrowthValueList);
    }

    // 要求 fundGrowthValueList 的排序顺序为时间倒序
    public static BigDecimal calculateByFundGrowthValueList(List<BigDecimal> fundGrowthValueList) {
        List<BigDecimal> d5 = fundGrowthValueList.parallelStream()
                .limit(5L)
                .collect(Collectors.toList());
        log.info("d5 {}", d5);

        BigDecimal d5sum = d5.parallelStream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("d5sum {}", d5sum);

        List<BigDecimal> d123 = fundGrowthValueList.parallelStream()
                .limit(123L)
                .collect(Collectors.toList());
        log.info("d123 {}", d123);

        BigDecimal d123SumD5Avg = d123.parallelStream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(25), RoundingMode.HALF_UP);
        log.info("d123SumD5Avg {}", d123SumD5Avg);

        return d5sum.subtract(d123SumD5Avg);
    }
}
