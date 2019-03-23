package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ch.uzh.ifi.seal.soprafs19.service.GameService;
import java.util.LinkedHashSet;
import java.util.Map;

@RestController
public class SantoriniController {


    //Constants
    private final UserService service;
    private final GameService gameservice;

    //Constructor
    SantoriniController(UserService service, GameService gameservice) {
        this.service = service;
        this.gameservice = gameservice;
    }


    //USER LOGIN

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
            if(newUser.getUsername().equals("")){
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"The username can not be empty");
            }
            if(newUser.getPassword().equals("")){
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,"The password can not be empty");
            }
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

    @CrossOrigin
    @PostMapping(value="/authentication")
    ResponseEntity<User> authentication(@RequestBody User userToAuthenticate){
        User user = service.getUserByUsername(userToAuthenticate.getUsername());
        if(user != null && user.getPassword().equals(userToAuthenticate.getPassword())){
            return new ResponseEntity<User>(user, HttpStatus.ACCEPTED);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Username or Password");

    }

    @CrossOrigin
    @PutMapping(value = "/users/{userId}")
    ResponseEntity<?> changeUser(@RequestHeader(value = "token") String token, @PathVariable long userId, @RequestBody User user){
        // Check if the user is logged in
        if(service.getUserByToken(token) == service.getUserById(userId)) {
            // Either right user or both null. -> Either invalid token and invalid id or token matching the ID
            // Check token first, since otherwise one could get whether the id is used or not
            if(service.getUserByToken(token) == null){
                // invalid token
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to alter this user");
            } else if ((service.getUserByUsername(user.getUsername()) == null) || service.getUserById(userId).getUsername().equals(user.getUsername())) {
                // If the username is not yet taken OR the username is taken by the given user from the Body. (thus username was not changed)
                service.updateUser(userId, user);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
            }
        }
        else if(service.getUserByToken(token) == null){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not logged in");
        }
        else if (service.getUserById(userId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not Found");
        }
        else{
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to alter this user");
        }
    }


    //SANTORINI GAME

    @PostMapping("/invitation")
    public void sendInvitation(@RequestBody Map<Long, Long> json){
        Long inviterId= json.get("inviterId");
        Long receiverId= json.get("receiverId");
        this.service.add_invitation(inviterId, receiverId);
    }

    @GetMapping("/invitation/{userId}")
    public LinkedHashSet<Long> seeInvitations(@PathVariable long userId){
        return(this.service.get_all_Invitations(userId));
    }

    //Weiss nicht wie? ->Answer Object?
    /*@PutMapping("/invitation")
    public void sendersponse(@RequestBody ){

    }*/

    @GetMapping("/game/{gameid}")
    public Game sendsgameback(@PathVariable Long gameid){
        return(this.gameservice.getGame(gameid));
    }

    @PutMapping("/game/{gameid}")
    public void updateGame(@PathVariable long gameId, @RequestBody Game game){
        this.gameservice.update(gameId,game);
    }


    //how to extract one thing
    /*
    @PostMapping("/quit/{gameid}")
    public void quitgame(@PathVariable long gameId, @RequestBody  Object obj){
        this.gameservice.leaveGame(userId, gameId);
    }*/

    @PostMapping("/matchmaking")
    public void startmatchmaking(long userId){
        this.service.startmatchmaking(userId);
    }

    /*how to extract one thing
    @PutMapping("/godcard/{gameId}")
    public void selectGodPower(@PathVariable long gameId, @RequestBody userId, cardId ){
        this.service.updateGodcard(gameid,userId,cardId)
     }*/

    /*how to extract only four things
    @PostMapping("/movement/{gameId}")
    public Game moveWorker(@Pathvariable long gameId @RequestBody ){
        return(this.gameservice.move(all_the_stuff));

    }

    @PostMapping("/movement/{gameId}")
    public Game build(@PathVariable long gameId @RequestBody ){
        return(this.gameservice.build.(all_the_stuff));
    }*/

    /*@PostMapping("/waive/{gameId")
    public void cancelGodPower(@PathVariable long gameId, @RequestBody ?){
        this.gameservice.waiveGodcard(gameId,userId);
    }*/


    @GetMapping("/turn/{gameId}")
    public Game turn(@PathVariable long gameId){
        return(this.gameservice.changeturn(gameId));
    }


    //highlight?? zweimal move und build..
}