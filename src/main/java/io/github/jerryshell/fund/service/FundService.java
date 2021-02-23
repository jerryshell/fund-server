package io.github.jerryshell.fund.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.jerryshell.fund.dto.EastmoneyGrowthItem;
import io.github.jerryshell.fund.entity.FundGrowth;
import io.github.jerryshell.fund.util.JerryIndexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FundService {
    @Resource
    private DataSourceService dataSourceService;

    public BigDecimal getJerryIndexByFundCode(
            String fundCode
    ) {
        log.info("fundCode {}", fundCode);

        // get data
        List<EastmoneyGrowthItem> eastmoneyGrowthItemList = dataSourceService.getEastmoneyGrowthItemList(fundCode);
        log.info("eastmoneyGrowthItemList {}", eastmoneyGrowthItemList);

        // get expect growth
        BigDecimal expectGrowth = getExpectGrowthFromEastmoney(fundCode);
        log.info("expectGrowth {}", expectGrowth);

        // build fundGrowthList
        List<FundGrowth> fundGrowthList = buildFundGrowthListByEastmoneyItemList(fundCode, eastmoneyGrowthItemList);
        log.info("fundGrowthList {}", fundGrowthList);

        // handle expect growth
        handleExpectGrowth(fundCode, fundGrowthList, expectGrowth);
        log.info("fundGrowthList after handleExpectGrowth {}", fundGrowthList);

        // result
        return JerryIndexUtil.calculateByFundGrowthList(fundGrowthList);
    }

    // eastmoneyGrowthItemList -> fundGrowthList
    private List<FundGrowth> buildFundGrowthListByEastmoneyItemList(String fundCode, List<EastmoneyGrowthItem> eastmoneyGrowthItemList) {
        return eastmoneyGrowthItemList.parallelStream()
                .map(item -> new FundGrowth(
                        fundCode,
                        item.getX().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        item.getEquityReturn()
                ))
                .collect(Collectors.toList());
    }

    // 如果 fundGrowthList 没有今日数据，则将 expectGrowth 作为今日数据加入到 fundGrowthList 中
    private void handleExpectGrowth(String fundCode, List<FundGrowth> fundGrowthList, BigDecimal expectGrowth) {
        String todayDateStr = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        log.info("todayDateStr {}", todayDateStr);

        boolean match = fundGrowthList.parallelStream()
                .anyMatch(fundGrowth -> todayDateStr.equals(fundGrowth.getDateStr()));
        log.info("match {}", match);

        if (!match) {
            fundGrowthList.add(new FundGrowth(
                    fundCode,
                    todayDateStr,
                    expectGrowth
            ));
        }
    }

    // 从 fundmobapi.eastmoney.com 中获取估算增长率
    private BigDecimal getExpectGrowthFromEastmoney(String fundCode) {
        String url = StrUtil.format(
                "https://fundmobapi.eastmoney.com/FundMNewApi/FundMNFInfo?plat=Android&appType=ttjj&product=EFund&Version=1&deviceid=ssdfsdfsdf&Fcodes={}",
                fundCode
        );
        log.info("url {}", url);

        String responseStr = HttpUtil.get(url);
        log.info("responseStr {}", responseStr);

        JSONObject responseJson = JSONUtil.parseObj(responseStr);
        log.info("responseJson {}", responseJson);

        JSONArray datas = responseJson.getJSONArray("Datas");
        log.info("datas {}", datas);

        Object expectGrowthObj = datas.getJSONObject(0).get("GSZZL");
        log.info("expectGrowthObj {}", expectGrowthObj);

        return new BigDecimal((String) expectGrowthObj);
    }
}
