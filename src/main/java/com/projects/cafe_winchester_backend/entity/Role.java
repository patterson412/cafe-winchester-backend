package com.projects.cafe_winchester_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @EmbeddedId
    private RoleId id; // Has userId and role. Acts as the Composite key

    @ManyToOne
    @MapsId("userId") // Reuses the userId from the composite key through the RoleId class.  ← Maps to RoleId.userId field. Avoids duplicating user_id column
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    public Role() {
    }

    public Role(RoleId id, User user) {
        this.id = id;
        this.user = user;
    }

    public RoleId getId() {
        return id;
    }

    public void setId(RoleId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
