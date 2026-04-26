package io.polaris.sebrae.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.polaris.sebrae.dto.LoginRequestDTO;
import io.polaris.sebrae.dto.LoginResponseDTO;
import io.polaris.sebrae.model.User;
import io.polaris.sebrae.repository.UserRepository;
import io.polaris.sebrae.security.JwtService;
import io.polaris.sebrae.security.UserDetailsImpl;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;
	
	public AuthController(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
	}
	
	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
		User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BadCredentialsException("Invalid Credentials"));
		
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new BadCredentialsException("Invalid Credentials");
		}
		
		UserDetailsImpl userDetails = new UserDetailsImpl(user);
		String token = jwtService.generateToken(userDetails);
		
		return ResponseEntity.ok(new LoginResponseDTO(token, user.getRole().name()));
	}
}
