package org.mb.mte.controller;


import org.json.JSONObject;
import org.mb.mte.service.BlackDuckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BlackDuckController {

    @Autowired
    private BlackDuckService blackDuckService;

    /**
     * Get the list of project names through redis
     * @return
     */
    @GetMapping("/bdProjects")
    public String getBbProjects(){
        return blackDuckService.getBdProjects();
    }

    /**
     * get BD project data through redis
     * @param project
     * @return
     */
    @GetMapping("/bdMetricsByProject")
    public String gwtSqMetricsByProj(@RequestParam String project) {
        return blackDuckService.getBdMetricsByProject(project);
    }


    // TEST BLACK DUCK DATA USING API

    /**
     * Test : Get the list of project names through direct API
     * @return
     */
    @GetMapping("/api/bdProjects")
    public ResponseEntity<?> getBlackDuckProjects(){
        try{
            List<String> data = blackDuckService.getBlackDuckProjectNames();
            return ResponseEntity.ok(data);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Test : Get BD project data through direct API
     * @param project
     * @return
     */
    @GetMapping("/api/bdMetricsByProject")
    public ResponseEntity<?> getBlackDuckMetricsByProject(@RequestParam String project){
        try{
            String data = blackDuckService.getBlackDuckMetricsByProject(project);
            return ResponseEntity.ok(data);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Test : Return entire BD details
     * @return
     */
    @GetMapping("/api/checkbd")
    public ResponseEntity<?> getAllBlackDuckData(){
        try{
            String data = blackDuckService.getBlackDuckData();
            return ResponseEntity.ok(data.toString());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Test : Manually push the BD DATA to redis through API
     * @return boolean
     */
    @GetMapping("/pushbd")
    public Boolean setBdProjectsData(){
        try{
            blackDuckService.setBdProjectsData();
            return true;
        }catch(Exception e){
            return false;
        }
    }

}

