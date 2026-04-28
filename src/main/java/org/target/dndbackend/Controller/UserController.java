package org.target.dndbackend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.target.dndbackend.Dto.*;
import org.target.dndbackend.Entity.User;
import org.target.dndbackend.Repository.UserRepository;
import org.target.dndbackend.Service.EmailService;
import org.target.dndbackend.Service.VerificationCodeService;
import org.target.dndbackend.Utils.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody SendCodeRequest request) {
        String email = request.getEmail();

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "Email is required"
            ));
        }

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "Email already exists"
            ));
        }

        String code = verificationCodeService.generateCode(email);
        emailService.sendVerificationCode(email, code);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "Verification code sent"
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (
                request.getUsername() == null || request.getUsername().isBlank() ||
                        request.getEmail() == null || request.getEmail().isBlank() ||
                        request.getPassword() == null || request.getPassword().isBlank() ||
                        request.getVerificationCode() == null || request.getVerificationCode().isBlank()
        ) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "Please fill in all fields"
            ));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "Email already exists"
            ));
        }

        boolean codeValid = verificationCodeService.verifyCode(
                request.getEmail(),
                request.getVerificationCode()
        );

        if (!codeValid) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "Invalid or expired verification code"
            ));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "Register successfully"
        ));
    }
    @GetMapping("/all")
    public ResponseEntity<?> getUsers() {
        List<UserNameAndEmail> res=userRepository.findAllNameAndEmail();
        return ResponseEntity.ok(res);
    }
    @GetMapping
    public ResponseEntity<?> getUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Unauthorized"
            ));
        }

        Long userId = Long.parseLong(authentication.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
        ));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean passwordMatch = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatch) {
            return ResponseEntity.status(401).body("Wrong password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        user.getId(),
                        user.getEmail(),
                        user.getUsername()
                )
        );
    }

}