package org.mb.mte.controller;

import org.mb.mte.service.BlackDuckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BlackDuckController {

    @Autowired
    private BlackDuckService blackDuckService;

    @GetMapping("/bdProjects")
    public String getBbProjects(){
        return blackDuckService.getBdProjects();
    }

    @GetMapping("/bdMetricsByProject")
    public String gwtSqMetricsByProj(@RequestParam String project) {
        return blackDuckService.getBdMetricsByProject(project);
    }

}

