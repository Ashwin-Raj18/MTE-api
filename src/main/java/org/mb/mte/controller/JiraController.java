package org.mb.mte.controller;

import org.mb.mte.service.JiraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JiraController {

    @Autowired
    JiraService jiraService;

}
