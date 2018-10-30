package gh25.entity;

import act.util.SimpleBean;
import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.*;

@MappedSuperclass
public abstract class AbstractEntity implements SimpleBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JSONField(deserialize = false)
    @Column(name = "ID")
    public Long id;

    @Version
    @Column(name = "VERSION")
    public Long version;

}
