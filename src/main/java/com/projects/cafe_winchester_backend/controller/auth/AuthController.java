package com.projects.cafe_winchester_backend.controller.auth;

import com.projects.cafe_winchester_backend.dto.LoginFormDto;
import com.projects.cafe_winchester_backend.dto.RegisterFormDto;
import com.projects.cafe_winchester_backend.entity.Address;
import com.projects.cafe_winchester_backend.entity.User;
import com.projects.cafe_winchester_backend.service.UserService;
import com.projects.cafe_winchester_backend.util.tokenUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final tokenUtil jwtTokenUtil;
    private final UserDetailsManager userDetailsManager;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, tokenUtil jwtTokenUtil, UserDetailsManager userDetailsManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsManager = userDetailsManager;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginFormDto loginFormDto) throws Exception {
        authenticate(loginFormDto.getUsername(), loginFormDto.getPassword());
        final UserDetails userDetails = userDetailsManager.loadUserByUsername(loginFormDto.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("username", userDetails.getUsername());
        responseBody.put("accessToken", token);


        return ResponseEntity
                .ok()
                .body(responseBody);
    }

    @PostMapping("/register")
    @Transactional // ensures that all database operations are treated as a single unit of work
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterFormDto registerFormDto) throws Exception {
        // Check if email exists
        if (userService.existsByEmail(registerFormDto.getUsername())) {
            throw new Exception("Email username already registered");
        }

        // Create user for spring authentication/authorization
        userService.createUser(registerFormDto.getUsername(), registerFormDto.getPassword(), new String[]{"USER"});

        User newUser = userService.getUserById(registerFormDto.getUsername());
        newUser.setEmail(registerFormDto.getUsername());
        newUser.setPhoneNumber(registerFormDto.getPhoneNumber());

        // Create and set up address
        Address address = new Address();
        address.setLatitude(registerFormDto.getLatitude());
        address.setLongitude(registerFormDto.getLongitude());

        // Set up bidirectional relationship
        address.setUser(newUser);
        newUser.setAddress(address);

        // Save the user
        User savedUser = userService.saveUser(newUser);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", "success");
        responseBody.put("message", "User registered successfully");
        responseBody.put("data", Map.of(
                "userId", savedUser.getUserId(),
                "email", savedUser.getEmail()
        ));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseBody);

    }


    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

}
