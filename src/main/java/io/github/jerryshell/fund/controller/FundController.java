package io.github.jerryshell.fund.controller;

import io.github.jerryshell.fund.dto.BaiduIndex;
import io.github.jerryshell.fund.exception.QDIIException;
import io.github.jerryshell.fund.service.FundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fund")
public class FundController {
    @Resource
    private FundService fundService;

    @GetMapping("/jerryIndex/fundCode/{fundCode}")
    public Map<String, Object> getJerryIndexByFundCode(
            @PathVariable String fundCode
    ) {
        log.info("fundCode {}", fundCode);
        Map<String, Object> result = new HashMap<>();

        BigDecimal jerryIndex;
        try {
            jerryIndex = fundService.getJerryIndexByFundCode(fundCode);
            result.put("ok", true);
            result.put("message", jerryIndex);
        } catch (QDIIException e) {
            e.printStackTrace();
            result.put("ok", false);
            result.put("message", "QDII 基金暂无净值估算数据");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("ok", false);
            result.put("message", "服务器错误");
        }

        return result;
    }

    @GetMapping("/baiduIndex/keyword/{keyword}")
    public BaiduIndex getBaiduIndexByKeyword(
            @PathVariable String keyword
    ) {
        log.info("keyword {}", keyword);

        return fundService.getBaiduIndexByKeyword(keyword);
    }
}
