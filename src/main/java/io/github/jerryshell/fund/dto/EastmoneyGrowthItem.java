package io.github.jerryshell.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EastmoneyGrowthItem {
    private LocalDateTime x;
    private BigDecimal equityReturn;
}
