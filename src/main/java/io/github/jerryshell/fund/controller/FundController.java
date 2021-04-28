package io.github.jerryshell.fund.controller;

import io.github.jerryshell.fund.service.FundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fund")
public class FundController {
    @Resource
    private FundService fundService;

    @GetMapping("/jerryIndex/fundCode/{fundCode}")
    public BigDecimal getJerryIndexByFundCode(
            @PathVariable String fundCode
    ) {
        log.info("fundCode {}", fundCode);

        return fundService.getJerryIndexByFundCode(fundCode);
    }

    @GetMapping("/baiduIndex")
    public Map<String, Object> getBaiduIndex() {
        return fundService.getBaiduIndex();
    }
}
