package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/users")
    Iterable<User> all(@RequestHeader(value = "token") String token) {
        if(service.getUserByToken(token)!= null)
            return service.getUsers();
        else{
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not logged in");
        }
    }

    @PostMapping("/users")
    ResponseEntity<?> createUser(@RequestBody User newUser) {
        User user = service.getUserByUsername(newUser.getUsername());
        if(user == null){
            user = service.createUser(newUser);
            return new ResponseEntity<User>(user, HttpStatus.CREATED);
        }else{
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already Taken!");
        }
    }

    @GetMapping(value="/user/{userId}")
    ResponseEntity<User> single(@RequestHeader(value = "token") String token, @PathVariable("userId") long userId){
        // Check for validity of request:
        if(service.getUserByToken(token) != null){
        User user = service.getUserById(userId);
        if(user != null) {
            return new ResponseEntity<User>(user, HttpStatus.ACCEPTED);
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Id is not Valid");
        }
        }else{
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not logged in");
        }

    }

    //TODO Chane to POST
    @GetMapping(value="/authentication")
    ResponseEntity<User> authentication(@RequestParam String username, @RequestParam String password){
        User user = service.getUserByUsername(username);
        if(user != null && user.getPassword().equals(password)){
            return new ResponseEntity<User>(user, HttpStatus.ACCEPTED);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Username or Password");

    }

    @CrossOrigin
    @PutMapping(value = "/users/{userId}")
    ResponseEntity<?> changeUser(@RequestHeader(value = "token") String token, @PathVariable long userId, @RequestBody User user){
        // Check if the user is logged in
        if(service.getUserByToken(token) != null) {
            if (service.getUserById(userId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not Found");
            } else if ((service.getUserByUsername(user.getUsername()) == null) || (service.getUserByUsername(user.getUsername()).getId().equals(user.getId()))) {
                // If the username is not yet taken OR the username is taken by the given user from the Body. (thus username was not changed)
                service.updateUser(userId, user);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
            }
        }else{
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not logged in");
        }
    }

}