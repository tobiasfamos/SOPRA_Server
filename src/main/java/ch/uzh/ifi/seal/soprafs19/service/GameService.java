package ch.uzh.ifi.seal.soprafs19.service;


import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GameService {

    private final ch.uzh.ifi.seal.soprafs19.repository.GameRepository GameRepository;

    private long convert_to_long(Long id){
        long i=id;
        return (i);
    }

    @Autowired
    public GameService(GameRepository GameRepository) { this.GameRepository = GameRepository;
    }

    public void createGame(){
        Game game= new Game();
        GameRepository.save(game);
    }

    public Game getGame(Long gameId){
        return(this.GameRepository.findById(convert_to_long(gameId)));
    }

    public void update(long gameId, Game newstategame){
        Game oldstateGame=this.GameRepository.findById(gameId);
        //änderungen vornehmen
    }

    public void leaveGame(long userId, long gameId){
        //kick the User out of the game
    }

    public Game move(long userId,long gameId,int workerId,int posX,int posY){
        Game oldstateGame=this.GameRepository.findById(gameId);

        //update position

        Game newstateGame=this.GameRepository.findById(gameId);
        return(newstateGame);
    }

    public Game build (long gameId, long userId, int workerId, int posX, int posY){
        Game currentGame=this.GameRepository.findById(gameId);
        //build
        Game newstateGame=this.GameRepository.findById(gameId);
        return(newstateGame);
    }


    public void waiveGodCard(long gameId, long userId){
        //waive God Cards
    }

    public Game changeturn(long gameId){
        Game oldstateGame=this.GameRepository.findById(gameId);
        //change the turn
        Game newstateGame=this.GameRepository.findById(gameId);
        return(newstateGame);
    }

}