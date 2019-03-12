package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/users")
    Iterable<User> all() {

        return service.getUsers();
    }

    @PostMapping("/users")
    ResponseEntity<?> createUser(@RequestBody User newUser) {
        User user = service.getUserByUsername(newUser.getUsername());
        if(user == null){
            user = service.createUser(newUser);
            return new ResponseEntity<User>(user, HttpStatus.CREATED);
        }else{
            return new ResponseEntity<String>("Username already Taken!", HttpStatus.CONFLICT);
        }
    }

    @GetMapping(value="/user/{userId}")
    ResponseEntity<?> single(@PathVariable("userId") long userId){
        User user = service.getUserById(userId);
        return new ResponseEntity<User>(user, HttpStatus.ACCEPTED);
    }
    //TODO Chane to POST
    @GetMapping(value="/authentication")
    ResponseEntity<?> authentication(@RequestParam String username, @RequestParam String password){
        User user = service.getUserByUsername(username);
        if(user != null && user.getPassword().equals(password)){
            return new ResponseEntity<User>(user, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<String>("Invalid Username or Password!" , HttpStatus.FORBIDDEN);

    }

    @CrossOrigin
    @PutMapping(value = "/users/{userId}")
    ResponseEntity<?> changeUser(@PathVariable long userId, @RequestBody User user){
        if(service.getUserById(userId) == null){
            return new ResponseEntity<String>("User not Found", HttpStatus.NOT_FOUND);
        }else if((service.getUserByUsername(user.getUsername()) == null) || (service.getUserByUsername(user.getUsername()).getId().equals(user.getId()))){
            // If the username is not yet taken OR the username is taken by the given user from the Body. (thus username was not changed)
            service.updateUser(userId, user);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        else{
            return new ResponseEntity<String>("Username already taken", HttpStatus.CONFLICT);
        }
    }

}