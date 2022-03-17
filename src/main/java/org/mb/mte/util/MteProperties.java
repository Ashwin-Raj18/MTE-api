package org.mb.mte.util;

import lombok.Data;
import org.mb.mte.repository.RedisRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@ConfigurationProperties("mte")
@PropertySource("classpath:mte.properties")
@Data
@Component
public class MteProperties {

    private String redisUrl;
    private String sqUrl;
    private String sqToken;

    private String jiraUrl;
    private String jiraToken;

}
