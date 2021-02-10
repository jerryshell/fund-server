package io.github.jerryshell.fund.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundGrowth {
    private String code;
    // yyyy-MM-dd
    private String dateStr;
    private BigDecimal growth;
}
