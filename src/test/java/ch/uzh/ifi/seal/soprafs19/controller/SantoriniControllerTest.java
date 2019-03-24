package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.service.GameService;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
@AutoConfigureMockMvc
public class SantoriniControllerTest {

    private User testUser;
    private String testUserJson;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Qualifier("gameRepository")
    @Autowired
    private GameRepository gameRepository;


    @Before
    public void setUp(){
        testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setBirthday(LocalDate.ofYearDay(1111,11));
        testUser.setPassword("testPassword");

        testUserJson = "{\"name\":\"testName\",\"username\":\"testUsername\",\"password\":\"testPassword\",\"birthday\":\"1111-01-11\" }";


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

    @Test
    public void testCreateUserWithEmptyFields() throws Exception{
        String emptyPassword = "{\"name\":\"testName\",\"username\":\"testUsername\",\"password\":\"\",\"birthday\":\"1111-01-11\" }";
        String emptyUsername = "{\"name\":\"testName\",\"username\":\"\",\"password\":\"testPassword\",\"birthday\":\"1111-01-11\" }";

        MvcResult result = this.mockMvc.perform(post("/users").header("Content-Type","application/json")
                .content(emptyPassword))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        Assert.assertEquals(result.getResponse().getErrorMessage(), "The password can not be empty");

        //Test for empty Username
         result = this.mockMvc.perform(post("/users").header("Content-Type","application/json")
                .content(emptyUsername))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        Assert.assertEquals(result.getResponse().getErrorMessage(), "The username can not be empty");
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
        newUser2.setBirthday(LocalDate.ofYearDay(1111,11));
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
        String altertUser = "{\"name\":\"testName\",\"username\":\"testUsername\",\"birthday\":\"1111-01-11\" }";
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
        newUser2.setBirthday(LocalDate.ofYearDay(1111,11));
        newUser2.setPassword("newPassword");
        newUser2.setName("newName");
        newUser2 = userService.createUser(newUser2);

        String newAlterString = String.format("{\"username\": \"%s\",\"name\":\"newName\",\"birthday\":\"1111-01-11\"} ",newUser.getUsername());

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
        Assert.assertTrue("Result does not contain Birthday",content.contains(user.getBirthday().toString()));
        Assert.assertTrue("Result does not contain Id",content.contains(user.getId().toString()));
    }


    //SANTORINI GAME TESTS


    @Test
    public void testInvitation()throws Exception{
        User testUser3 = new User();
        testUser3.setName("testName");
        testUser3.setUsername("testUsername3");
        testUser3.setBirthday(LocalDate.ofYearDay(1111,11));
        testUser3.setPassword("testPassword");

        User newUser3 = userService.createUser(testUser3);

        //getting the id
        long id=newUser3.getId();

        //write the jsonfile
        String jsonfile = "{\"inviterId\" : \"3\", \"receiverId\" : \""+id+"\"}";

        this.mockMvc.perform(post("/invitation")
                .content(jsonfile)
                .contentType(APPLICATION_JSON_UTF8)
                .header("token",newUser3.getToken())
                .header("sending an invitation","invitation ID's"))
                .andExpect(status().isOk());
    }


    @Test
    public void testGetInvitation()throws Exception{
        User testUser4 = new User();
        testUser4.setName("testName");
        testUser4.setUsername("testUsername4");
        testUser4.setBirthday(LocalDate.ofYearDay(1111,11));
        testUser4.setPassword("testPassword");

        User newUser4 = userService.createUser(testUser4);

        //getting the id
        long id=newUser4.getId();

        this.mockMvc.perform(get("/invitation/"+id)
                .header("token",newUser4.getToken())
                .header("test getting all invitations","invitations"))
                .andExpect(status().isOk());
    }

    @Test
    public void testAnswerInvitation()throws Exception{
        //write the jsonfile
        String jsonfile = "{\"inviterId\" : \"5\", \"inviteeId\" : \"7\", \"answer\" : \"true\"}";

        //create a new User
        User testUser7 = new User();
        testUser7.setName("testName");
        testUser7.setUsername("testUsername7");
        testUser7.setBirthday(LocalDate.ofYearDay(1111,11));
        testUser7.setPassword("testPassword");
        User newUser7 = userService.createUser(testUser7);

        this.mockMvc.perform(put("/invitation")
                .content(jsonfile)
                .contentType(APPLICATION_JSON_UTF8)
                .header("token",newUser7.getToken())
                .header("writing an answer, update invitations","invitations ID and answer"))
                .andExpect(status().isOk());
    }


    @Test
    public void testGetGame()throws Exception{

        this.mockMvc.perform(get("/game/1")
                .header("writing an answer, update invitations","invitations ID and answer"))
                .andExpect(status().isOk());
    }

    /*@Test
    public void testUpdateGame()throws Exception{

        //create game and save it into the database
        Game game= new Game();
        game.setGameId((long)1);
        gameRepository.save(game);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson=ow.writeValueAsString(game);

        this.mockMvc.perform(put("/game/1")
                .content(requestJson)
                .header("update game","gameupdate"))
                .andExpect(status().isOk());
    }*/

    @Test
    public void testMovement()throws Exception{
        //create game and save it into the database
        Game game= new Game();
        game.setGameId((long)1);
        gameRepository.save(game);

        //create a new User
        User testUser6 = new User();
        testUser6.setName("testName");
        testUser6.setUsername("testUsername6");
        testUser6.setBirthday(LocalDate.ofYearDay(1111,11));
        testUser6.setPassword("testPassword");
        User newUser6 = userService.createUser(testUser6);

        //write the jsonfile
        String jsonfile = "{\"userId\" : \"5\", \"workerId\" : \"7\", \"posX\" : \"1\",\"posY\" : \"1\"}";

        this.mockMvc.perform(post("/movement/1")
                .content(jsonfile)
                .contentType(APPLICATION_JSON_UTF8)
                .header("token",newUser6.getToken())
                .header("movement","movement"))
                .andExpect(status().isOk());
    }

    @Test
    public void testBuilding()throws Exception{
        //create game and save it into the database
        Game game= new Game();
        game.setGameId((long)1);
        gameRepository.save(game);

        //create a new User
        User testUser5 = new User();
        testUser5.setName("testName");
        testUser5.setUsername("testUsername5");
        testUser5.setBirthday(LocalDate.ofYearDay(1111,11));
        testUser5.setPassword("testPassword");
        User newUser5 = userService.createUser(testUser5);

        //write the jsonfile
        String jsonfile = "{\"userId\" : \"5\", \"workerId\" : \"7\", \"posX\" : \"1\",\"posY\" : \"1\"}";

        this.mockMvc.perform(post("/building/1")
                .content(jsonfile)
                .contentType(APPLICATION_JSON_UTF8)
                .header("token",newUser5.getToken())
                .header("building","building"))
                .andExpect(status().isOk());
    }
















}