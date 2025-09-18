package com.cn.dayliapi.scheduled;

import com.cn.dayliapi.util.DayliUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@Slf4j
public class DayliScheduled {

    private final DayliUtil dayliUtil;
    public DayliScheduled(DayliUtil dayliUtil) {
        this.dayliUtil = dayliUtil;
    }

    /**
     * 每天定时执行任务，发送每日新闻摘要。
     */
    @Scheduled(cron = "0 0 8 * * ?") // 每天早上8点0分0秒执行
//    @Scheduled(cron = "0/5 * * * * ?")
    public void executeTask() {
        log.info("开始每日新闻任务...");
        try {
            dayliUtil.sendDayliNews();
            log.info("每日新闻任务顺利完成。");
        } catch (Exception e) {
            log.error("每日新闻任务期间出错: {}", e.getMessage(), e);
        }
    }
}
