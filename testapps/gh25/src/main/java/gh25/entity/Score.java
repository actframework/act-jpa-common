package gh25.entity;

import act.controller.annotation.UrlContext;
import act.db.jpa.JPADao;

import javax.persistence.*;

@Entity
@Table(name = "SCORE")
public class Score extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    public Player player;

    @Column(name = "STAGE")
    public Long stage;

    @Column(name = "POINTS")
    public Long points;

    @UrlContext("scores")
    public static class Dao extends JPADao<Long, Score> {

    }

}
