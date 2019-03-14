package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.validation.constraints.Null;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
public class UserControllerTest {

    private User testUser;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    private UserController userController;

    @Autowired
    private UserService userService;


    @Before
    public void setUp(){
        userController = new UserController(userService);
        testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setBirthday("testBirthday");
        testUser.setPassword("testPassword");
    }

    @Test
    public void all() {
        //Test for testusers in list after added.
        User newTestUser = (User) userController.createUser(testUser).getBody();
        ArrayList<User> returnedUsers = (ArrayList<User>) userController.all(newTestUser.getToken());
        Assert.assertEquals("Testuser not in array", newTestUser, returnedUsers.get(0));
    }

    @Test
    public void createUser() throws Exception {
        ResponseEntity<?> response = userController.createUser(testUser);
        Assert.assertEquals("Wrong Statuscode", HttpStatus.CREATED, response.getStatusCode());
        long id = 1;
        testUser.setId(id);
        Assert.assertEquals("Not same User", testUser, response.getBody());

        // Test for Fail due to already taken Username.
        response = userController.createUser(testUser);
        Assert.assertEquals("Wrong Status Code at fail to create login",HttpStatus.CONFLICT, response.getStatusCode());

    }
    @Test
    public void single() {
        ResponseEntity<?> response = userController.single(1);
        Assert.assertEquals("Wrong Status Code at sending wrong ID",HttpStatus.NOT_FOUND, response.getStatusCode());

        User newUser = (User) userController.createUser(testUser).getBody();
        response = userController.single(newUser.getId());
        Assert.assertEquals("Wrong or no user returned", newUser, (User) response.getBody());

    }

    @Test
    public void authentication() throws NullPointerException {
        // Test access denied
        ResponseEntity<?> response = userController.authentication(testUser.getUsername(), testUser.getPassword());
        Assert.assertEquals("Wrong Status code", HttpStatus.FORBIDDEN, response.getStatusCode());

        // Test access granted
        User newUser = (User) userController.createUser(testUser).getBody();
        response = userController.authentication(newUser.getUsername(), newUser.getPassword());
        Assert.assertEquals("Wrong Status Code", HttpStatus.ACCEPTED, response.getStatusCode());
        Assert.assertEquals("Wrong User was returned", newUser, (User) response.getBody());
    }

    @Test
    public void changeUser() {
    }
}