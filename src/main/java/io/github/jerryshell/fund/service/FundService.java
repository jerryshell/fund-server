package io.github.jerryshell.fund.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.github.jerryshell.fund.dto.BaiduIndex;
import io.github.jerryshell.fund.dto.EastmoneyGrowthItem;
import io.github.jerryshell.fund.entity.FundGrowth;
import io.github.jerryshell.fund.exception.QDIIException;
import io.github.jerryshell.fund.util.JerryIndexUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FundService {
    // jerryIndexCache timeout: 10s
    private static final int JERRY_INDEX_CACHE_TIMEOUT = 1000 * 10;
    // baiduIndexCache timeout: 600s
    private static final int BAIDU_INDEX_CACHE_TIMEOUT = 1000 * 60 * 10;

    private final LRUCache<String, BigDecimal> jerryIndexCache = CacheUtil.newLRUCache(1024, JERRY_INDEX_CACHE_TIMEOUT);
    private final LRUCache<String, BaiduIndex> baiduIndexCache = CacheUtil.newLRUCache(1024, BAIDU_INDEX_CACHE_TIMEOUT);

    @Resource
    private DataSourceService dataSourceService;

    public BaiduIndex getBaiduIndexByKeyword(String keyword) {
        // cache
        BaiduIndex cache = baiduIndexCache.get(keyword);
        log.info("baiduIndex cache {}", cache);
        if (cache != null) {
            return cache;
        }

        String response = HttpUtil.get("https://index.chinaz.com/" + keyword + "/180");
        if (StrUtil.isBlank(response)) {
            return null;
        }

        String baiduDate = response.split("indexchart.baiduDate = \\[")[1];
        baiduDate = baiduDate.split("];")[0];
        log.info("baiduDate {}", baiduDate);

        List<String> baiduDateList = Arrays.stream(baiduDate.split(","))
                .map(item -> item.replaceAll("\"", "").trim())
                .collect(Collectors.toList());
        log.info("baiduDateList {}", baiduDateList);

        String baiduAllIndex = response.split("indexchart.baiduAllIndex = \\[")[1];
        baiduAllIndex = baiduAllIndex.split("];")[0];
        log.info("baiduAllIndex {}", baiduAllIndex);

        List<Integer> baiduAllIndexList = Arrays.stream(baiduAllIndex.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        log.info("baiduAllIndexList {}", baiduAllIndexList);

        Integer baiduAllIndexListSum = baiduAllIndexList.stream().reduce(Integer::sum).orElse(0);
        log.info("baiduAllIndexListSum {}", baiduAllIndexListSum);

        double baiduAllIndexListAvg = baiduAllIndexListSum * 1.0 / baiduAllIndexList.size();
        log.info("baiduAllIndexListAvg {}", baiduAllIndexListAvg);

        BaiduIndex baiduIndex = new BaiduIndex(
                baiduDateList,
                baiduAllIndexList,
                baiduAllIndexListSum,
                baiduAllIndexListAvg
        );

        // put cache
        baiduIndexCache.put(keyword, baiduIndex);

        return baiduIndex;
    }

    public BigDecimal getJerryIndexByFundCode(String fundCode) throws QDIIException {
        // cache
        BigDecimal cache = jerryIndexCache.get(fundCode);
        log.info("jerryIndex cache {}", cache);
        if (cache != null) {
            return cache;
        }

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

        // calculate
        BigDecimal jerryIndex = JerryIndexUtil.calculateByFundGrowthList(fundGrowthList);
        log.info("jerryIndex {}", jerryIndex);

        // put cache
        jerryIndexCache.put(fundCode, jerryIndex);

        return jerryIndex;
    }

    // eastmoneyGrowthItemList -> fundGrowthList
    private List<FundGrowth> buildFundGrowthListByEastmoneyItemList(
            String fundCode,
            List<EastmoneyGrowthItem> eastmoneyGrowthItemList
    ) {
        return eastmoneyGrowthItemList.parallelStream()
                .map(item -> new FundGrowth(
                        fundCode,
                        item.getX().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        item.getEquityReturn()
                ))
                .collect(Collectors.toList());
    }

    // 如果 fundGrowthList 没有今日数据，则将 expectGrowth 作为今日数据加入到 fundGrowthList 中
    private void handleExpectGrowth(
            String fundCode,
            List<FundGrowth> fundGrowthList,
            BigDecimal expectGrowth
    ) {
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
    private BigDecimal getExpectGrowthFromEastmoney(String fundCode) throws QDIIException {
        String url = StrUtil.format(
                "https://fundmobapi.eastmoney.com/FundMNewApi/FundMNFInfo?plat=Android&appType=ttjj&product=EFund&Version=1&deviceid=ssdfsdfsdf&Fcodes={}",
                fundCode
        );
        log.info("url {}", url);

        String responseStr;
        try {
            responseStr = HttpUtil.get(url);
            log.info("responseStr {}", responseStr);
        } catch (IORuntimeException e) {
            log.error(e.getMessage());
            return BigDecimal.ZERO;
        }

        JSONObject responseJson = JSONUtil.parseObj(responseStr);
        log.info("responseJson {}", responseJson);

        JSONArray datas = responseJson.getJSONArray("Datas");
        log.info("datas {}", datas);

        Object expectGrowthObj = datas.getJSONObject(0).get("GSZZL");
        log.info("expectGrowthObj {}", expectGrowthObj);

        // 当前产品为 QDII 基金，投资于境外市场，暂无净值估算数据。
        if ("--".equals(expectGrowthObj)) {
            throw new QDIIException();
        }

        return new BigDecimal((String) expectGrowthObj);
    }
}
