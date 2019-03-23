package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ch.uzh.ifi.seal.soprafs19.service.GameService;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;


@RestController
public class SantoriniController {

    //LOGGER
    private final Logger log = LoggerFactory.getLogger(UserService.class);


    //Constants
    private final UserService service;
    private final GameService gameservice;

    //Constructor
    SantoriniController(UserService service, GameService gameservice) {
        this.service = service;
        this.gameservice = gameservice;
    }

    //USEFUL FUNCTIONS
    //converts a string into a long
    //mostly used for Id
    public long convert_to_long(String x){
        try {
            long result = Long.parseLong(x);
            return (result);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is not a Long");
        }
    }

    public boolean convert_to_boolean(String x){
        try {
            boolean result = Boolean.parseBoolean(x);
            return (result);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is not a Boolean");
        }
    }

    public int convert_to_int(String x){
        try {
            int result = Integer.parseInt(x);
            return (result);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Id is not an Integer");
        }
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
    public void sendInvitation(@RequestBody  Object json)throws Exception{
        LinkedHashMap jsonbody=(LinkedHashMap)json;
        String inviterIdstring=jsonbody.get("inviterId").toString();
        String receiverIdstring=jsonbody.get("receiverId").toString();
        long inviterId = convert_to_long(inviterIdstring);
        long receiverId = convert_to_long(receiverIdstring);
        this.service.add_invitation(inviterId, receiverId);
    }

    @GetMapping("/invitation/{userId}")
    public LinkedHashSet<Long> seeInvitations(@PathVariable long userId){
        return(this.service.get_all_Invitations(userId));
    }

    @PutMapping("/invitation")
    public void senderResponse(@RequestBody Object json)throws Exception{
        LinkedHashMap jsonbody=(LinkedHashMap)json;
        String inviterIdstring=jsonbody.get("inviterId").toString();
        String receiverIdstring=jsonbody.get("inviteeId").toString();
        String answerstring=jsonbody.get("answer").toString();
        long inviterId=convert_to_long(inviterIdstring);
        long inviteeId=convert_to_long(inviterIdstring);
        boolean answer=convert_to_boolean(answerstring);
        this.service.invitationResponse(inviterId,inviteeId,answer);
    }

    @GetMapping("/game/{gameid}")
    public Game sendsgameback(@PathVariable Long gameid){
        return(this.gameservice.getGame(gameid));
    }

    @PutMapping("/game/{gameid}")
    public void updateGame(@PathVariable long gameId, @RequestBody Game game){
        this.gameservice.update(gameId,game);
    }

    @PostMapping("/quit/{gameid}")
    public void quitgame(@PathVariable long gameId, @RequestBody  Object json){
        LinkedHashMap jsonbody=(LinkedHashMap)json;
        String userIdstring=jsonbody.get("userId").toString();
        long userId=convert_to_long(userIdstring);
        this.gameservice.leaveGame(userId, gameId);
    }

    @PostMapping("/matchmaking")
    public void startmatchmaking(long userId){
        this.service.startmatchmaking(userId);
    }


    @PutMapping("/godcard/{gameId}")
    public void selectGodPower(@PathVariable long gameId, @RequestBody Object json){
        LinkedHashMap jsonbody=(LinkedHashMap)json;
        String userIdstring=jsonbody.get("userId").toString();
        String cardIdstring=jsonbody.get("cardId").toString();
        long userId=convert_to_long(userIdstring);
        int cardId=convert_to_int(cardIdstring);
        this.service.updateGodCard(gameId,userId,cardId);
     }

    @PostMapping("/movement/{gameId}")
    public Game moveWorker(@PathVariable long gameId, @RequestBody Object json){
        LinkedHashMap jsonbody=(LinkedHashMap)json;
        String userIdstring=jsonbody.get("userId").toString();
        String workerIdstring=jsonbody.get("workerId").toString();
        String posXstring=jsonbody.get("posX").toString();
        String posYstring=jsonbody.get("posY").toString();
        long userId=convert_to_long(userIdstring);
        int workerId=convert_to_int(workerIdstring);
        int posX=convert_to_int(posXstring);
        int posY=convert_to_int(posYstring);
        return(this.gameservice.move(userId,gameId,workerId,posX,posY));
    }

    @PostMapping("/building/{gameId}")
    public Game build(@PathVariable long gameId, @RequestBody Object json){
        LinkedHashMap jsonbody=(LinkedHashMap)json;
        String userIdstring=jsonbody.get("userId").toString();
        String workerIdstring=jsonbody.get("workerId").toString();
        String posXstring=jsonbody.get("posX").toString();
        String posYstring=jsonbody.get("posY").toString();
        long userId=convert_to_long(userIdstring);
        int workerId=convert_to_int(workerIdstring);
        int posX=convert_to_int(posXstring);
        int posY=convert_to_int(posYstring);
        return(this.gameservice.build(gameId,userId,workerId,posX,posY));
    }

    @PostMapping("/waive/{gameId")
    public void cancelGodPower(@PathVariable long gameId, @RequestBody Object json){
        LinkedHashMap jsonbody=(LinkedHashMap)json;
        String userIdstring=jsonbody.get("userId").toString();
        long userId=convert_to_long(userIdstring);
        this.gameservice.waiveGodCard(gameId,userId);
    }


    @GetMapping("/turn/{gameId}")
    public Game turn(@PathVariable long gameId){
        return(this.gameservice.changeturn(gameId));
    }


    //highlight?? zweimal move und build..
}