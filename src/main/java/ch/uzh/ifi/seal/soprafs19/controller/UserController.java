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
    User createUser(@RequestBody User newUser) {
        return service.createUser(newUser);
    }

    @GetMapping(value="/user")
    User single(@PathVariable("userId") long userId){
        return this.service.getUserById(userId);
    }

    @GetMapping(value="/authentication")
    ResponseEntity<?> authentication(@RequestParam String username, @RequestParam String password){
        User user = service.getUserByName(username);
        if(user != null && user.getPassword().equals(password)){
            return new ResponseEntity<User>(user, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<String>(String.format("Invalid Username or Password! be %s and %s",username, password) , HttpStatus.FORBIDDEN);

    }

}