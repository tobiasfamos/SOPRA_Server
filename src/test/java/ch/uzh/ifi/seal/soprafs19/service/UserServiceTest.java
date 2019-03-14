package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import org.apache.tomcat.jni.Local;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
public class UserServiceTest {

    private User testUser;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;



    @Autowired
    private UserService userService;

    private static Logger LOGGER = Logger.getLogger("Test Logger");


    @Before
    public void setUp(){
        testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setBirthday(LocalDate.ofYearDay(1111,11));
        testUser.setPassword("testPassword");
        testUser = userService.createUser(testUser);
        LOGGER.info(userRepository.findByName("testName").toString());
    }

    @After
    public void tearDown(){
        userRepository.deleteAll();
    }

    @Test()
    public void getUsers(){
        ArrayList<User> users = new ArrayList<User>();
        users.add(testUser);
        Assert.assertEquals("Userlist is wrong. ",users, userService.getUsers());
    }
    @Test
    public void createUser() {
        Assert.assertNull(userRepository.findByUsername("LocalTestUsername"));

        User localTestUser = new User();
        localTestUser.setName("LocaltestName");
        localTestUser.setUsername("LocalTestUsername");
        localTestUser.setBirthday(LocalDate.ofYearDay(1111,11));
        localTestUser.setPassword("LocaltestPassword");
        User createdUser = userService.createUser(localTestUser);

        Assert.assertNotNull("Token was not Set", createdUser.getToken());
        Assert.assertEquals("User Status is not online", createdUser.getStatus(),UserStatus.ONLINE);
        Assert.assertEquals("User can not be found with token", createdUser, userRepository.findByToken(createdUser.getToken()));
    }

    @Test
    public void getUserById() {
        User foundUser = userService.getUserById(testUser.getId());
        Assert.assertEquals("User not found by Id", testUser, foundUser);
    }

    @Test
    public void getUserByUsername() {
        User foundUser = userService.getUserByUsername(testUser.getUsername());
        Assert.assertEquals("User not found by username",  testUser, foundUser);
    }

    @Test
    public void getUserByName() {
        User foundUser = userService.getUserByName(testUser.getName());
        LOGGER.info(testUser.getName());
        Assert.assertEquals("User not Found by Name", testUser, foundUser);
    }

    @Test
    public void updateUser() {
        String newName = "NewName";
        LocalDate newBirthday = LocalDate.ofYearDay(1111,11);
        String newUsername = "New Username";
        testUser.setName(newName);
        testUser.setBirthday(newBirthday);
        testUser.setUsername(newUsername);
        userService.updateUser(testUser.getId(), testUser);

        User foundUser = userService.getUserById(testUser.getId());

        Assert.assertEquals("Name does not match", foundUser.getName(), newName);
        Assert.assertEquals("Birthday does not match", foundUser.getBirthday(), newBirthday);
        Assert.assertEquals("Username does not match",foundUser.getUsername(), newUsername);
    }
}
