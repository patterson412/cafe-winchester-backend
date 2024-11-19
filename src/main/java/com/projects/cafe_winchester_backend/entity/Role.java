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
    private Member member;

    public Role() {
    }

    public Role(RoleId id, Member member) {
        this.id = id;
        this.member = member;
    }

    public RoleId getId() {
        return id;
    }

    public void setId(RoleId id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", memberId=" + (member != null ? member.getUserId() : null) +
                '}';
    }
}
