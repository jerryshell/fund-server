package io.github.jerryshell.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaiduIndex {
    private List<String> baiduDateList;
    private List<Integer> baiduAllIndexList;
    private Integer baiduAllIndexListSum;
    private Double baiduAllIndexListAvg;
}
