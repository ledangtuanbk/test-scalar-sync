package com.demo.springdemotest;

import com.demo.mylib.domain.UserEntity;
import com.demo.mylib.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping
    public void create(){
        UserEntity userEntity = new UserEntity();
        userEntity.setName("John Doe");
        userRepository.save(userEntity);

        System.out.println("User created with ID: " + userEntity.toString());
    }

    @GetMapping
    public List<UserEntity> findAll(){
        return userRepository.findAll();
    }
}
