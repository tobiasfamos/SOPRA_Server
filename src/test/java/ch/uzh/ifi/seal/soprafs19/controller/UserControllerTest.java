package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
@AutoConfigureMockMvc
public class UserControllerTest {

    private User testUser;
    private String testUserJson;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;


    @Before
    public void setUp(){
        testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setBirthday("testBirthday");
        testUser.setPassword("testPassword");

        testUserJson = "{\"name\":\"testName\",\"username\":\"testUsername\",\"password\":\"testPassword\",\"birthday\":\"testBirthday\" }";


    }

    // Testblock for access via Token
    @Test
    public void testGetUsersNotLoggedIn() throws Exception{
        // Test for Spring to return a 400 if the token is missing.
        this.mockMvc.perform(get("/users")).andExpect(status().is4xxClientError());
    }

    @Test
    public void testgetUsersLoggedIn() throws Exception{
        User newUser = userService.createUser(testUser);
        // Test with valid token
        this.mockMvc.perform(get("/users").header("token",newUser.getToken())).andExpect(status().isOk());

        // Test with invalid Token
        this.mockMvc.perform(get("/users").header("token","invalid-token")).andExpect(status().isForbidden());

        //Delete the created Users from the database
        userService.deleteUser(newUser);
    }



    // Testblock for creating a User
    @Test
    public void testCreateUser() throws Exception {
        // Test for creating one User
        this.mockMvc.perform(post("/users").header("Content-Type","application/json")
                .content(testUserJson))
                .andExpect(status().isCreated());

        // Test for error whe using a already used username
        this.mockMvc.perform(post("/users").header("Content-Type","application/json")
                .content(testUserJson))
                .andExpect(status().isConflict());
        // Delete the created user form database
        userService.deleteUser(userService.getUserByUsername("testUsername"));
    }

    // Tests for getting all Users
    @Test
    public void testGetAllUsers() throws Exception{
        // Test for the first user
        User newUser = userService.createUser(testUser);
        MvcResult  result = this.mockMvc.perform(get("/users").header("token",newUser.getToken()))
            .andExpect(status().isOk())
            .andReturn();

        validateUserInResponse(newUser, result);
        //Test for a second User
        User newUser2 = new User();
        newUser2.setName("newUser2");
        newUser2.setPassword("newUser2Password");
        newUser2.setBirthday("newUser2Birthday");
        newUser2.setUsername("newUser2Username");
        newUser2 = userService.createUser(newUser2);

        result = this.mockMvc.perform(get("/users").header("token",newUser2.getToken()))
                .andExpect(status().isOk())
                .andReturn();

        validateUserInResponse(newUser2, result);


        // Delete the created users from database
        userService.deleteUser(newUser);
        userService.deleteUser(newUser2);
    }

    //Test get sincle User
    @Test
    public void testSingleUser() throws Exception{
        // Test for forbidden acces without token
        this.mockMvc.perform(get("/user/1")).andExpect(status().is4xxClientError());

        // Test for invalid token
        User newUser = userService.createUser(testUser);
        this.mockMvc.perform(get(String.format("/user/%d",newUser.getId())).header("token","invalid-token"))
                .andExpect(status().isForbidden());

        // Test for invalid id
        this.mockMvc.perform(get(String.format("/user/%d",newUser.getId()+1)).header("token",newUser.getToken()))
                .andExpect(status().isNotFound());

        // Test for valid request
        MvcResult result = this.mockMvc.perform(get(String.format("/user/%d",newUser.getId())).header("token",newUser.getToken()))
                .andExpect(status().isAccepted()).andReturn();
        validateUserInResponse(newUser, result);

        userService.deleteUser(newUser);
    }

    // Test authenticate
    @Test
    public void testAuthentaction() throws Exception{
        String authenticationBody = "{\"username\":\"testUsername\",\"password\":\"testPassword\"}";
        // Test for forbidden
        MvcResult result = this.mockMvc.perform(post("/authentication").header("Content-Type","application/json")
                .content(authenticationBody))
                .andExpect(status().isForbidden())
                .andReturn();
        Assert.assertEquals(result.getResponse().getErrorMessage(), "Invalid Username or Password");

        // Test Accepted
        User newUser = userService.createUser(testUser);
        result = this.mockMvc.perform(post("/authentication").header("Content-Type","application/json")
                .content(authenticationBody))
                .andExpect(status().isAccepted()).andReturn();
        validateUserInResponse(newUser, result);

        userService.deleteUser(newUser);
    }

    // Tests to change a User
    @Test
    public void testChangeUser() throws Exception{
        // Test for missing token
        String altertUser = "{\"name\":\"testName\",\"username\":\"testUsername\",\"birthday\":\"testBirthday\" }";
        this.mockMvc.perform(put("/users/1").content(altertUser)).andExpect(status().is4xxClientError());

        // test for logged in and invalid token
        User newUser = userService.createUser(testUser);
        MvcResult result = this.mockMvc.perform(put(String.format("/users/%d",newUser.getId()))
                .header("token","invalid-token")
                .header("Content-Type","application/json")
                .content(altertUser))
                .andExpect(status().isForbidden())
                .andReturn();
        Assert.assertEquals("You are not logged in",result.getResponse().getErrorMessage());

        // test for logged in and valid token
        this.mockMvc.perform(put(String.format("/users/%d",newUser.getId()))
                .header("token",newUser.getToken())
                .header("Content-Type","application/json")
                .content(altertUser))
                .andExpect(status().isNoContent());

        // test for altering a nonexisting user
        result = this.mockMvc.perform(put(String.format("/users/%d",newUser.getId()+1))
                .header("token",newUser.getToken())
                .header("Content-Type","application/json")
                .content(altertUser))
                .andExpect(status().isNotFound())
                .andReturn();

        Assert.assertEquals( "User not Found",result.getResponse().getErrorMessage());

        // test for Username already taken
        User newUser2 = new User();
        newUser2.setUsername("newUsername");
        newUser2.setBirthday("newBirthday");
        newUser2.setPassword("newPassword");
        newUser2.setName("newName");
        newUser2 = userService.createUser(newUser2);

        String newAlterString = String.format("{\"username\": \"%s\",\"name\":\"newName\",\"birthday\":\"newBirthday\"} ",newUser.getUsername());

        result = this.mockMvc.perform(put(String.format("/users/%d",newUser2.getId()))
                .header("token",newUser2.getToken())
                .header("Content-Type","application/json")
                .content(newAlterString))
                .andExpect(status().isConflict())
                .andReturn();

        Assert.assertEquals("Username already taken",result.getResponse().getErrorMessage());

        // Test for trying to change an other user
        result = this.mockMvc.perform(put(String.format("/users/%d",newUser.getId()))
                .header("token",newUser2.getToken())
                .header("Content-Type","application/json")
                .content(altertUser))
                .andExpect(status().isForbidden())
                .andReturn();

        Assert.assertEquals("You are not allowed to alter this user",result.getResponse().getErrorMessage());

        userService.deleteUser(userService.getUserByUsername("newUsername"));
        userService.deleteUser(userService.getUserByUsername("testUsername"));

    }

    private void validateUserInResponse(User user, MvcResult result) throws Exception{
        String content = result.getResponse().getContentAsString();
        Assert.assertTrue("Result does not containt Username",content.contains(user.getUsername()));
        Assert.assertTrue("Result does not contain Name", content.contains(user.getName()));
        Assert.assertTrue("Result does not contain token",content.contains(user.getToken()));
        Assert.assertTrue("Result does not contain Birthday",content.contains(user.getBirthday()));
        Assert.assertTrue("Result does not contain Id",content.contains(user.getId().toString()));
    }
 }