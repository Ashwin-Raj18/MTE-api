package org.mb.mte.cronJobs;

import org.mb.mte.clientrequest.JiraClient;
import org.mb.mte.clientrequest.SonarQubeClient;
import org.mb.mte.service.BlackDuckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class MteJobs {


    private static final Logger logger = LoggerFactory.getLogger(MteJobs.class);
    @Autowired
    SonarQubeClient sonarQubeClient;

    @Autowired
    JiraClient jiraClient;

    @Autowired
    BlackDuckService blackDuckService;

    @Scheduled(cron = "*/50 * * * * *")
    public void mteSq() {
        sonarQubeClient.sqProjects();
        sonarQubeClient.sqMetrics();
        sonarQubeClient.sqIssues();
        sonarQubeClient.sqHotSpots();
    }

    @Scheduled(cron = "*/50 * * * * *")
    public void mteJira() {
        logger.info("Jira job started");
        jiraClient.jiraProjects();
        jiraClient.jiraIssuesProject();

    }

    @Scheduled(cron = "*/50 * * * * *")
    public void mteBd() throws Exception {
        blackDuckService.setBdProjectsData();
    }
}
