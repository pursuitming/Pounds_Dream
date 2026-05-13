package com.poundsdream.vo;

import lombok.Data;

import java.util.List;

@Data
public class RankVO {
    private Integer points;
    private Integer rankLevel;
    private String rankName;
    private String rankEnName;
    private Integer currentStreak;
    private Integer maxStreak;
    private Integer nextRankDays;
    private String nextRankName;
    private Integer dailyPoints;
    private Integer milestonePoints;
    private List<RankConfigItem> allRanks;

    @Data
    public static class RankConfigItem {
        private Integer level;
        private String name;
        private String enName;
        private Integer requiredDays;
        private Integer milestoneReward;
        private String icon;
    }
}
