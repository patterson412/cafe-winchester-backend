package com.projects.cafe_winchester_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "favourites", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "item_id"}) // Prevents user from favouriting the same item twice
})
public class Favourites {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private MenuItem menuItem;

    public Favourites() {
    }

    public Favourites(Long id, User user, MenuItem menuItem) {
        this.id = id;
        this.user = user;
        this.menuItem = menuItem;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    @Override
    public String toString() {
        return "Favourites{" +
                "id=" + id +
                ", user=" + user +
                ", menuItem=" + menuItem +
                '}';
    }
}
