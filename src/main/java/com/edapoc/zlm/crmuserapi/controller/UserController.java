package com.edapoc.zlm.crmuserapi.controller;

import com.edapoc.zlm.crmuserapi.ResponseHandler;
import com.edapoc.zlm.crmuserapi.model.User;
import com.edapoc.zlm.crmuserapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "User", description = "User management APIs")
@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api")
public class UserController {
  @Autowired
  UserService userService;
  @Operation(summary = "Retrieve all users", tags = { "users", "get", "filter" })
  @ApiResponses({
      @ApiResponse(responseCode = "200", content = {
          @Content(schema = @Schema(implementation = User.class), mediaType = "application/json") }),
      @ApiResponse(responseCode = "204", description = "There are no users", content = {
          @Content(schema = @Schema()) }),
      @ApiResponse(responseCode = "500", content = { @Content(schema = @Schema()) }) })
  @GetMapping("/v1/user")
  public ResponseEntity<Object> getAllUsers(@RequestParam(required = false) Long userId){

    try {
      List<User> found = userService.getAllUsers();
      if (userId != null) {
        found = found.stream().filter(user -> user.getId() == (userId)).toList();
      }
      return found.isEmpty()
              ? ResponseHandler.generateResponse("No users found", HttpStatus.NO_CONTENT, null)
              : ResponseHandler.generateResponse(null, HttpStatus.OK, found);
    } catch (Exception e) {
      return ResponseHandler.generateResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, null);
    }
  }

  @Operation(summary = "Retrieve user by id", tags = { "users", "get", "filter" })
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User successfully retrieved by id", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
          @ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
  @GetMapping("/v1/user/{id}")
  public ResponseEntity<Object> getUserById(@PathVariable Long id) {
    try {
      Optional<User> found = userService.getUserById(id);

      return found.map(user -> ResponseHandler.generateResponse(null, HttpStatus.OK, user))
              .orElseGet(() -> ResponseHandler.generateResponse("User with id " + id + " not found", HttpStatus.NOT_FOUND, null));
    } catch (Exception e) {
      return ResponseHandler.generateResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, null);
    }
  }


  @Operation(summary = "Add a new user", tags = { "users", "post", "filter" })
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "User created",content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
          @ApiResponse(responseCode = "400", description = "Bad request",content = @Content),
          @ApiResponse(responseCode = "404", description = "No user found",content = @Content) })
  @PostMapping("/v1/user")
  public ResponseEntity<Object> create(@RequestBody User user) {
    try {
      Optional<User> existingUser = userService.getUserById(user.getId());
      if (existingUser.isPresent()) {
        return ResponseHandler.generateResponse("User with id " + user.getId() + " already exists", HttpStatus.BAD_REQUEST, null);
      }

      User createdUser = userService.create(user.getName(), user.getEmail());

      return ResponseHandler.generateResponse("User created", HttpStatus.CREATED, createdUser);

    } catch (IllegalArgumentException e) {
      return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, null);
    }
  }

  @Operation(summary = "Update an existing user", tags = { "users", "put", "filter" })
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User updated successfully", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
          @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
          @ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
  @PutMapping("/v1/user/{id}")
  public ResponseEntity<Object> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
    try {
      Optional<User> existingUser = userService.getUserById(id);

      if (existingUser.isEmpty()) {
        return ResponseHandler.generateResponse("User with id " + id + " not found", HttpStatus.NOT_FOUND, null);
      }

      User userToUpdate = existingUser.get();
      userToUpdate.setName(updatedUser.getName());
      userToUpdate.setEmail(updatedUser.getEmail());

      User savedUser = userService.save(userToUpdate);

      return ResponseHandler.generateResponse("User updated successfully", HttpStatus.OK, savedUser);
    } catch (IllegalArgumentException e) {
      return ResponseHandler.generateResponse(e.getMessage(), HttpStatus.BAD_REQUEST, null);
    }
  }

  @Operation(summary = "Delete user by id", tags = { "users", "delete", "filter" })
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "User deleted successfully", content = @Content),
          @ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
  @DeleteMapping("/v1/user/{id}")
  public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
    try {
      Optional<User> existingUser = userService.getUserById(id);

      if (existingUser.isEmpty()) {
        return ResponseHandler.generateResponse("User with id " + id + " not found", HttpStatus.NOT_FOUND, null);
      }

      userService.deleteUserById(id);

      return ResponseHandler.generateResponse("User deleted successfully", HttpStatus.NO_CONTENT, null);
    } catch (Exception e) {
      return ResponseHandler.generateResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, null);
    }
  }

  @Operation(summary = "Retrieve user by email", tags = { "users", "get", "filter" })
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "User successfully retrieved by email", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) }),
          @ApiResponse(responseCode = "404", description = "User not found", content = @Content) })
  @GetMapping("/v1/user/email")
  public ResponseEntity<Object> getUserByEmail(@RequestParam String email) {
    try {
      Optional<User> found = userService.findUserByEmail(email);

      return found.map(user -> ResponseHandler.generateResponse(null, HttpStatus.OK, user))
              .orElseGet(() -> ResponseHandler.generateResponse("User with email " + email + " not found", HttpStatus.NOT_FOUND, null));
    } catch (Exception e) {
      return ResponseHandler.generateResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, null);
    }
  }




}
