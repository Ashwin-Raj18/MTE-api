package org.mb.mte.service;

import org.mb.mte.repository.RedisRepository;
import org.mb.mte.util.RedisKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SonarQubeService {

    @Autowired
    RedisRepository redisRepository;

    public String getScMetricsByProject(String project){
       return redisRepository.getData(RedisKeys.scMetricsKey);
    }

    public String getProjects(){
        return redisRepository.getData(RedisKeys.scProjectsKey);
    }
}
