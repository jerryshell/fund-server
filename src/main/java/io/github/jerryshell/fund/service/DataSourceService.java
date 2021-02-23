package io.github.jerryshell.fund.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import io.github.jerryshell.fund.dto.EastmoneyGrowthItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DataSourceService {
    // 从 fund.eastmoney.com/pingzhongdata 中获取增长率数据
    @Cacheable(cacheNames = "fund-server")
    public List<EastmoneyGrowthItem> getEastmoneyGrowthItemList(String fundCode) {
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

        return eastmoneyGrowthItemList;
    }
}
