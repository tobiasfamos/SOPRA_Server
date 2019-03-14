package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public Iterable<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User createUser(User newUser) {
        newUser.setToken(UUID.randomUUID().toString());
        newUser.setStatus(UserStatus.ONLINE);
        newUser.setCreationDate(new Date().getTime());
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User getUserById(long userId){
        return userRepository.findById(userId);

    }

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public User getUserByName(String name){
        return userRepository.findByName(name);
    }

    public void updateUser(long id, User user) {
        /*
        *Updates the the Fields birthday, name and username of a User. It must be checked, that the username is not yet taken.
        * */
        User userToCopy = userRepository.findById(id);
        // Set the Name, Username and Birthday
        userToCopy.setBirthday(user.getBirthday());
        userToCopy.setName(user.getName());
        userToCopy.setUsername(user.getUsername());
    }

    public User getUserByToken(String token){
        return userRepository.findByToken(token);
    }
}
