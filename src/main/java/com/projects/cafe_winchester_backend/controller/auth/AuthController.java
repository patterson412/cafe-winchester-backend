package com.projects.cafe_winchester_backend.controller.auth;

import com.projects.cafe_winchester_backend.dto.LoginFormDto;
import com.projects.cafe_winchester_backend.dto.RegisterFormDto;
import com.projects.cafe_winchester_backend.entity.Address;
import com.projects.cafe_winchester_backend.entity.User;
import com.projects.cafe_winchester_backend.service.UserManagementService;
import com.projects.cafe_winchester_backend.service.UserService;
import com.projects.cafe_winchester_backend.util.tokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final UserManagementService userManagementService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          tokenUtil jwtTokenUtil,
                          UserDetailsManager userDetailsManager,
                          UserManagementService userManagementService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsManager = userDetailsManager;
        this.userManagementService = userManagementService;
        this.userService = userService;
    }


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginFormDto loginFormDto) throws Exception {
        authenticate(loginFormDto.getUsername(), loginFormDto.getPassword());
        final UserDetails userDetails = userDetailsManager.loadUserByUsername(loginFormDto.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Create cookie
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false)   // Should be set to true in production to use https
                .path("/")
                .maxAge(7 * 60 * 60) // 7 hours in seconds
                .sameSite("Strict")
                .build();

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("status", "success");
        responseBody.put("message", "Login successful");
        responseBody.put("username", userDetails.getUsername());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());


        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(headers)
                .body(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")   // Set to the same name as the cookie that was created to override it
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)  // Causes the cookie to immediately expire
                .sameSite("Strict")
                .build();

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("status", "success");
        responseBody.put("message", "Logged out successfully");

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
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
        userManagementService.createUser(registerFormDto.getUsername(), registerFormDto.getPassword(), "USER");

        // Create user entity
        User newUser = new User();
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
        User savedUser = userService.createUser(newUser);

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
