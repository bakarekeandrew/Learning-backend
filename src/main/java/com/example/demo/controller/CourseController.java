package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.example.demo.entity.Course;
import com.example.demo.service.CourseService;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "http://localhost:3000") // Add this to allow cross-origin requests
public class CourseController {

    @Autowired
    private CourseService courseService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    public ResponseEntity<?> createCourse(
        @RequestParam("courseName") String courseName,
        @RequestParam("tutor") String tutor,
        @RequestParam("price") int price,
        @RequestParam("description") String description,
        @RequestParam("video") String video,
        @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            // Validate input
            if (courseName == null || courseName.isEmpty()) {
                return ResponseEntity.badRequest().body("Course name is required");
            }

            Course course = new Course();
            course.setCourseName(courseName);
            course.setTutor(tutor);
            course.setPrice(price);
            course.setDescription(description);
            course.setVideo(video);

            // Handle file upload
            if (photo != null && !photo.isEmpty()) {
                try {
                    // Ensure upload directory exists
                    Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
                    Files.createDirectories(uploadPath);

                    // Generate unique filename
                    String originalFilename = photo.getOriginalFilename();
                    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String newFilename = UUID.randomUUID().toString() + fileExtension;

                    // Save file
                    Path targetLocation = uploadPath.resolve(newFilename);
                    Files.copy(photo.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                    // Set photo path
                    course.setPhoto("/uploads/" + newFilename);
                    System.out.println("File saved: " + targetLocation);
                } catch (IOException ex) {
                    System.err.println("Could not store file: " + ex.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Could not upload file: " + ex.getMessage());
                }
            }

            // Save course
            Course savedCourse = courseService.createCourse(course);
            return ResponseEntity.ok(savedCourse);

        } catch (Exception e) {
            // Log full stack trace
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating course: " + e.getMessage());
        }
    }

    @PostMapping("/{id}")
    public ResponseEntity<?> updateCourse(
        @PathVariable Long id,
        @RequestParam("courseName") String courseName,
        @RequestParam("tutor") String tutor,
        @RequestParam("price") int price,
        @RequestParam("description") String description,
        @RequestParam("video") String video,
        @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            // Fetch existing course
            Course existingCourse = courseService.getCourseById(id);

            // Update course details
            existingCourse.setCourseName(courseName);
            existingCourse.setTutor(tutor);
            existingCourse.setPrice(price);
            existingCourse.setDescription(description);
            existingCourse.setVideo(video);

            // Handle file upload
            if (photo != null && !photo.isEmpty()) {
                try {
                    // Ensure upload directory exists
                    Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
                    Files.createDirectories(uploadPath);

                    // Generate unique filename
                    String originalFilename = photo.getOriginalFilename();
                    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String newFilename = UUID.randomUUID().toString() + fileExtension;

                    // Save file
                    Path targetLocation = uploadPath.resolve(newFilename);
                    Files.copy(photo.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                    // Set photo path
                    existingCourse.setPhoto("/uploads/" + newFilename);
                    System.out.println("File saved: " + targetLocation);
                } catch (IOException ex) {
                    System.err.println("Could not store file: " + ex.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Could not upload file: " + ex.getMessage());
                }
            }

            // Save updated course
            Course updatedCourse = courseService.updateCourse(id, existingCourse);
            return ResponseEntity.ok(updatedCourse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating course: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
    }
}