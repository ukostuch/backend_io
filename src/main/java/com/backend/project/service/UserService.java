package com.backend.project.service;

import com.backend.project.dto.*;
import com.backend.project.exceptions.*;
import com.backend.project.model.PhotoUser;
import com.backend.project.model.Roles;
import com.backend.project.model.UserEntity;
import com.backend.project.repository.RoleRepository;
import com.backend.project.repository.UserRepository;
import com.backend.project.security.JWTGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JWTGenerator jwtGenerator;
    private final UserPhotoService userPhotoService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository, UserPhotoService userPhotoService,
                       JWTGenerator jwtGenerator, PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.jwtGenerator = jwtGenerator;
        this.userPhotoService = userPhotoService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
    }

    public UserEntity getUserByUsername(String username){
        return userRepository.findAll()
                .stream()
                .filter(userEntity -> userEntity.getUsername().equals(username))
                .findFirst().orElseThrow(() -> new UserNotFoundException(username));
    }

    public UserDto getUserByRequest(HttpServletRequest request) throws FileException, UserNotFoundException, InvalidToken {
        UserEntity user = getUserFromToken(request);

        PhotoUser usph = null;

        if(user.getPhoto() != null){
            usph = userPhotoService.getPhotoById(user.getPhoto()).orElse(null);
        }

        String content;
        if(usph != null){
            try{
                Resource photo = userPhotoService.asResource(usph);
                byte[] imageBytes = photo.getContentAsByteArray();
                content = Base64.getEncoder().encodeToString(imageBytes);
            }catch(Exception e){
                throw new FileException("Cannot load user picture",e);
            }
        }else{
            content = null;
        }

        return new UserDto(user.getUsername(), user.getName(), user.getSurname(), user.getMail(), content, user.getSalutation(), user.getCountry());
    }


    public UserDto updateUser(String username, UserPatchDto userDto, HttpServletRequest request) throws InvalidToken, NotAllowedException, EmailTakenException, UsernameTakenException, UsernameForbiddenException {
        // String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(t -> t.startsWith("Bearer "))
                .map(t -> t.substring(7))
                .orElseThrow(() -> new InvalidToken("Invalid or missing token."));


        String requesterUsername  = jwtGenerator.getUsernameFromJWT(token);
        List<String> roles = jwtGenerator.getRolesFromJWT(token);

        if(!roles.contains("ADMIN") && !Objects.equals(requesterUsername, username)){
            throw new NotAllowedException(username);
        }

        UserEntity existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Update only specific fields
        if(userDto.getUsername() != null){
            UserEntity ue = userRepository.findByUsername(userDto.getUsername()).orElse(null);

            if(userDto.getUsername().toLowerCase().startsWith("admin") && !roles.contains("ADMIN")){
                throw new UsernameForbiddenException(userDto.getUsername());
            }

            if(ue == null || Objects.equals(ue.getUsername(), existingUser.getUsername())){
                existingUser.setUsername(userDto.getUsername());
            }
            else{
                throw new UsernameTakenException(userDto.getUsername());
            }
        }
        if(userDto.getName() != null) existingUser.setName(userDto.getName());
        if (userDto.getSurname() != null) existingUser.setSurname(userDto.getSurname());

        if(userDto.getMail() != null){
            UserEntity ue = userRepository.findByMail(userDto.getMail()).orElse(null);

            if(ue == null || Objects.equals(ue.getMail(),existingUser.getMail())) {
                existingUser.setMail(userDto.getMail());
            }
            else{
                throw new EmailTakenException(userDto.getMail());
            }
        }

        if (userDto.getCountry() != null) existingUser.setCountry(userDto.getCountry());
        if (userDto.getSalutation() != null) existingUser.setSalutation(userDto.getSalutation());

        existingUser.setUpdatedAt(LocalDateTime.now());

        UserEntity userEntity = userRepository.save(existingUser);
        return mapToDto(userEntity);
    }

    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> "USER".equals(role.getName()))).map(this::mapToDto)
                .toList();
    }

    public void removeByUsername(String username){
        UserEntity userToDelete =
                userRepository.findAll()
                        .stream()
                        .filter(user -> user.getUsername().equals(username))
                        .findFirst().
                        orElseThrow(() -> new UserNotFoundException(username));
        if(userToDelete.getPhoto() != null){
            userPhotoService.deletePhotoById(userToDelete.getPhoto());
        }
        userRepository.deleteById(userToDelete.getId());
    }

    public UserEntity registerUser(RegisterDto registerDto) throws UsernameTakenException, EmailTakenException {
        if(userRepository.existsByUsername(registerDto.getUsername())){
            throw new UsernameTakenException(registerDto.getUsername());
        }

        if(userRepository.existsByMail(registerDto.getMail())){
            throw new EmailTakenException(registerDto.getMail());
        }

        UserEntity user = new UserEntity(registerDto.getName(), registerDto.getSurname(),
                registerDto.getMail(), registerDto.getUsername(),passwordEncoder.encode(registerDto.getPassword()),
                registerDto.getSalutation(), registerDto.getCountry());

        Roles roles;

        if(registerDto.getUsername().startsWith("admin")){
            roles = roleRepository.findByName("ADMIN").orElse(null);
        }
        else{
            roles = roleRepository.findByName("USER").orElse(null);
        }

        user.setRoles(Collections.singletonList(roles));
        return userRepository.save(user);
    }


    public String changePassword(ChangePasswordDto changePasswordDto, String token) throws InvalidToken, UsernameNotFoundException, InvalidCredentialsException {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtGenerator.validateToken(jwt)) {
            throw new InvalidToken("Invalid or expired token");
        }

        String username = jwtGenerator.getUsernameFromJWT(jwt);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(changePasswordDto.oldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        String newPassword = passwordEncoder.encode(changePasswordDto.newPassword());
        user.setPassword(newPassword);
        userRepository.save(user);
        return newPassword;
    }

    public String changeEmail(changeEmailDto emailDto, String token) throws InvalidToken, UsernameNotFoundException, InvalidCredentialsException, EmailTakenException {
        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;

        if(!jwtGenerator.validateToken(jwt)) {
            throw new InvalidToken("Invalid or expired token");
        }

        String username = jwtGenerator.getUsernameFromJWT(jwt);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(!passwordEncoder.matches(emailDto.password(), user.getPassword())){
            throw new InvalidCredentialsException("Password is incorrect");
        }

        //Check if email is already used by another account
        if(userRepository.existsByMail(emailDto.newEmail())){
            throw new EmailTakenException(emailDto.newEmail());
        }

        user.setMail(emailDto.newEmail());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return "Email successfully changed to " + emailDto.newEmail();


    }

    public AuthResponseDto login(LoginDto loginDto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.username(),
                        loginDto.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);

        return new AuthResponseDto(token);
    }

    private UserDto mapToDto(UserEntity user) {
        PhotoUser userPhoto = null;
        if(user.getPhoto() != null){
            userPhoto = userPhotoService.getPhotoById(user.getPhoto()).orElse(null);
        }

        String content;
        if(userPhoto != null){
            try{
                Resource photo = userPhotoService.asResource(userPhoto);
                byte[] imageBytes = photo.getContentAsByteArray();
                content = Base64.getEncoder().encodeToString(imageBytes);
            }catch(Exception e){
                throw new FileException("Cannot load user picture",e);
            }
        }else{
            content = null;
        }
        return new UserDto(
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getMail(),
                content,
                user.getSalutation(),
                user.getCountry()
        );
    }

    private UserEntity getUserFromToken(HttpServletRequest request) throws InvalidToken {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw new InvalidToken("Token body does not comply with assumed format and therefore cannot be validated");
        }

        String username = jwtGenerator.getUsernameFromJWT(token);
        UserEntity user;

        user = userRepository.findByUsername(username).orElse(null);
        if(user == null){
            throw new UserNotFoundException("User could not have been found.");
        }

        return user;
    }
}
