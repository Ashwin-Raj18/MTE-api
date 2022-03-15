package org.mb.mte.controller;

import org.mb.mte.service.SonarQubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SonarQubeController {

    @Autowired
    SonarQubeService sonarQubeService;

    @GetMapping("/scProjects")
    public String getScProjects(){
        return sonarQubeService.getProjects();
    }
}
