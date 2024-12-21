package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Password hashing method
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User createUser(User user) {
        long userCount = userRepository.count();      
        
        if (userCount == 0) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.USER);
        }

        user.setPassword(hashPassword(user.getPassword()));
        
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser != null) {
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            return userRepository.save(existingUser);
        }
        return null;
    }
    
   public User getUserByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
}
   public User authenticateUser(String email, String password) {
    User user = userRepository.findByEmail(email).orElse(null);
    
    String hashedInputPassword = hashPassword(password);
    
    if (user != null && hashedInputPassword.equals(user.getPassword())) {
        return user;
    }
    return null;
}

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // Password Reset Methods
    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        String token = UUID.randomUUID().toString();
        
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        
        return token;
    }
    
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        if (user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }
        
        user.setPassword(hashPassword(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }
    
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String token = generatePasswordResetToken(email);
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("your-email@gmail.com", "your-app-password");
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("your-email@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Password Reset Request");
            message.setText("Click the link to reset your password: " + resetLink);
            
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}