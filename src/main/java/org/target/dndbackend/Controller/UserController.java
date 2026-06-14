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
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(Authentication authentication,@RequestBody ChangePasswordRequest changePasswordRequest) {
        Long userId = Long.parseLong(authentication.getName());
        String oldPassword = changePasswordRequest.getOldPassword();
        boolean passwordMatch = passwordEncoder.matches(
                oldPassword,
                userRepository.findById(userId).get().getPassword()
        );
        if (passwordMatch) {
            User user=userRepository.findById(userId).get();
            user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Change successfully"
            ));
        }
        return ResponseEntity.ok(Map.of(
                "ok", false,
                "message", "Old Password Doesn't Match"
        ));
    }

    @PostMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(Authentication authentication, @RequestBody ForgetPasswordRequest forgetPasswordRequest){
        Long userId = Long.parseLong(authentication.getName());
        User user=userRepository.findById(userId).get();
        boolean codeValid = verificationCodeService.verifyCode(
                user.getEmail(),
                forgetPasswordRequest.getVerificationCode()
        );
        if(codeValid){
            user.setPassword(passwordEncoder.encode(forgetPasswordRequest.getNewPassword()));
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Change successfully"
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "ok", false,
                "message", "Verification code doesn't match"
        ));
    }

    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(Authentication authentication,@RequestBody ChangeEmailRequest changeEmailRequest){
        Long userId = Long.parseLong(authentication.getName());
        boolean codeValid = verificationCodeService.verifyCode(
                changeEmailRequest.getNewEmail(),
                changeEmailRequest.getVerificationCode()
        );
        if(codeValid){
            User user=userRepository.findById(userId).get();
            user.setEmail(changeEmailRequest.getNewEmail());
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Change successfully"
            ));
        }
        return ResponseEntity.badRequest().body(Map.of(
                "ok", false,
                "message", "Verification code doesn't match"
        ));
    }

    @PostMapping("/send-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody SendCodeRequest request) {
        String email = request.getEmail();

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "message", "Email is required"
            ));
        }

        String code = verificationCodeService.generateCode(email);
        emailService.sendVerificationCode(email, code);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "Verification code sent"
        ));
    }
    @PostMapping("/notifications")
    public ResponseEntity<?> notifyChapter(
            Authentication authentication,
            @RequestBody NotifyRequest notifyRequest
    ){
        List<Long> userIds=notifyRequest.getUserIds();
        for(int i = 0; i < userIds.size(); i++) {
            Optional<User> optionalUser = userRepository.findById(userIds.get(i));
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                String email = user.getEmail();
                emailService.sendNotification(email, notifyRequest.getBookId());
            }
        }
        return ResponseEntity.ok(
                Map.of("message", "Notifications sent successfully")
        );
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
    public ResponseEntity<?> getUsers(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        String currentUserEmail = jwtUtil.extractEmail(token);
        List<UserNameAndEmail> res = userRepository
                .findAllNameAndEmail()
                .stream()
                .filter(user -> !user.getEmail().equals(currentUserEmail))
                .toList();
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