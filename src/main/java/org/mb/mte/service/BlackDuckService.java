package org.mb.mte.service;

import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.RedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class BlackDuckService {

    private static final Logger logger = LoggerFactory.getLogger(RedisRepository.class);

    @Autowired
    RedisRepository redisRepository;


    public String getBdProjects(){
        return redisRepository.getData(RedisKeys.blackDuckProjectsKey);
    }

    public String getBdMetricsByProject(String project) {
        return  redisRepository.getData(RedisKeys.blackDuckMetricsKey+"_"+project);
    }

}
