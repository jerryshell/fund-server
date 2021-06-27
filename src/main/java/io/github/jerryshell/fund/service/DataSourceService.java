package io.github.jerryshell.fund.service;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.LRUCache;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.github.jerryshell.fund.dto.EastmoneyGrowthItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DataSourceService {
    // cache timeout: 1h
    private static final int CACHE_TIMEOUT = 1000 * 60 * 60;

    private final LRUCache<String, List<EastmoneyGrowthItem>> cache = CacheUtil.newLRUCache(1024, CACHE_TIMEOUT);

    // 从 fund.eastmoney.com/pingzhongdata 中获取增长率数据
    public List<EastmoneyGrowthItem> getEastmoneyGrowthItemList(String fundCode) {
        log.info("fundCode {}", fundCode);

        // cache
        List<EastmoneyGrowthItem> cacheData = cache.get(fundCode);
        if (cacheData != null) {
            return cacheData;
        }

        String url = StrUtil.format(
                "https://fund.eastmoney.com/pingzhongdata/{}.js",
                fundCode
        );
        String responseStr = HttpUtil.get(url);
        log.info("responseStr {}", responseStr);

        int jsonStrBeginIndex = responseStr.indexOf("Data_netWorthTrend = ") + "Data_netWorthTrend = ".length();
        log.info("jsonStrBeginIndex {}", jsonStrBeginIndex);

        int jsonStrEndIndex = responseStr.indexOf(";/*累计净值走势*/var Data_ACWorthTrend");
        log.info("jsonStrEndIndex {}", jsonStrEndIndex);

        String jsonStr = responseStr.substring(jsonStrBeginIndex, jsonStrEndIndex);
        log.info("jsonStr {}", jsonStr);

        List<EastmoneyGrowthItem> eastmoneyGrowthItemList = JSONUtil.toList(jsonStr, EastmoneyGrowthItem.class);
        log.info("eastmoneyItemList {}", eastmoneyGrowthItemList);

        // put cache
        cache.put(fundCode, eastmoneyGrowthItemList);

        return eastmoneyGrowthItemList;
    }
}
