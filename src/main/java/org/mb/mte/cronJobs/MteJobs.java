package org.mb.mte.cronJobs;

import org.mb.mte.clientrequest.SonarQubeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class MteJobs {

    @Autowired
    SonarQubeClient sonarQubeClient;

    @Autowired
    JiraClient jiraClient;

    @Autowired
    BlackDuckService blackDuckService;

    @Scheduled(cron = "*/50 * * * * *")
    public void mteSq() {
        sonarQubeClient.sqProjects();
        sonarQubeClient.sqMetricsByProject();
    }

    @Scheduled(cron = "*/50 * * * * *")
    public void mteJira() {
        jiraClient.jiraProjects();

    }

    @Scheduled(cron = "*/50 * * * * *")
    public void mteBd() throws Exception {
        blackDuckService.setBdProjectsData();
    }
}
