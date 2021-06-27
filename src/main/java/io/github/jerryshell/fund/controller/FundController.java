package io.github.jerryshell.fund.controller;

import io.github.jerryshell.fund.dto.BaiduIndex;
import io.github.jerryshell.fund.dto.Result;
import io.github.jerryshell.fund.exception.QDIIException;
import io.github.jerryshell.fund.service.FundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/fund")
public class FundController {
    @Resource
    private FundService fundService;

    @GetMapping("/jerryIndex/fundCode/{fundCode}")
    public Result<BigDecimal> getJerryIndexByFundCode(@PathVariable String fundCode) {
        log.info("fundCode {}", fundCode);
        try {
            BigDecimal jerryIndex = fundService.getJerryIndexByFundCode(fundCode);
            return Result.success(jerryIndex);
        } catch (QDIIException e) {
            e.printStackTrace();
            return Result.error("QDII 基金暂无净值估算数据");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("服务器错误");
        }
    }

    @GetMapping("/baiduIndex/keyword/{keyword}")
    public Result<BaiduIndex> getBaiduIndexByKeyword(@PathVariable String keyword) {
        log.info("keyword {}", keyword);

        BaiduIndex baiduIndex = fundService.getBaiduIndexByKeyword(keyword);
        log.info("baiduIndex {}", baiduIndex);

        return Result.success(baiduIndex);
    }
}
