package ch.uzh.ifi.seal.soprafs19.repository;


import ch.uzh.ifi.seal.soprafs19.entity.Player;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@Component
public interface PlayerRepository extends CrudRepository<Player, Long> {
    Boolean existsById(long id);
    Player findById(long id);
}
