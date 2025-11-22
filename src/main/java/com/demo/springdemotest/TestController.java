package com.demo.springdemotest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "/test")
@RequiredArgsConstructor
public class TestController {
    private final TestService testService;

    @GetMapping("/{id}")
    public ResponseEntity<String> test(@PathVariable String id){
        String test = testService.test(id);
        return ResponseEntity.ok(test);
    }

    @GetMapping("/clear/{id}")
    public ResponseEntity<String> clearCache(@PathVariable String id) {
        return ResponseEntity.ok("Cache cleared for id: " + id);
    }
}
