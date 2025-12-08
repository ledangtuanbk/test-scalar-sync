package com.demo.springdemotest;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class TestService {


    public String test(String id){
        System.out.println("Executing test method for id: " + id);
//        cachedTest(id);
        return "Hello " + id;
    }

//    @Cacheable(value = "testCache", key = "#id")
//    public String cachedTest(String id){
//        System.out.println("Executing cachedTest method for id: " + id);
//        return "Cached Hello " + id;
//    }


}
