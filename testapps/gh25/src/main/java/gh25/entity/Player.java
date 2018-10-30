package gh25.entity;

import act.controller.annotation.UrlContext;
import act.db.DbBind;
import act.db.jpa.JPADao;
import act.util.PropertySpec;
import com.alibaba.fastjson.annotation.JSONField;
import org.osgl.mvc.annotation.*;

import java.util.List;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@NamedQueries({
        @NamedQuery(name = "Player.findByName", query = "SELECT p FROM Player p WHERE p.name = :name ORDER BY p.id")
})
@Table(name = "PLAYER")
public class Player extends AbstractEntity {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "player", cascade = CascadeType.ALL)
    @JSONField(serialize = false, deserialize = false)
    private List<Score> scoreList;

    @NotNull
    @Size(min = 3, max = 45)
    @Column(name = "NAME", length = 45)
    public String name;

    @NotNull
    @Size(min = 3, max = 45)
    @Column(name = "LOGIN", length = 45)
    public String login;

    @UrlContext("players")
    public static class Dao extends JPADao<Long, Player> {

        @PostAction
        @PropertySpec("id")
        public Player create(@Valid Player player) {
            return save(player);
        }

        @GetAction("{player}")
        public Player getOne(@DbBind Player player) {
            return player;
        }

        @GetAction("first")
        public Player firstByName(String name) {
            return findOneBy("name", name);
        }

        @DeleteAction("{player}")
        public void deleteOne(@DbBind Player player) {
            delete(player);
        }

        @PostAction("{player}/scores")
        @PropertySpec("id")
        public Score addScore(@DbBind Player player, Score score, Score.Dao scoreDao) {
            score.player = player;
            return scoreDao.save(score);
        }

        @GetAction("{player}/scores")
        public List<Score> getScores(@DbBind Player player) {
            return player.scoreList;
        }

        @GetAction
        public Iterable<Player> list(String name) {
            if (null == name) {
                return findAll();
            } else {
                return findBy("name", name);
            }
        }
    }

}
