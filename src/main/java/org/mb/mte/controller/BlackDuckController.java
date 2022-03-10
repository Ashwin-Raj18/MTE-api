package org.mb.mte.controller;


import org.json.JSONObject;
import org.mb.mte.service.BlackDuckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlackDuckController {

    @Autowired
    private BlackDuckService blackDuckService;

    @GetMapping("/blackduck")
    public ResponseEntity<?> getBlackDuckData(@RequestHeader("token") String token){
        try{
            Object data = blackDuckService.getBlackDuckData(token);
            return ResponseEntity.ok(data.toString());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }
}

