package com.pointwest.pastebook.pastebook_backend.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "post")
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String content;

  @Column(name = "created_at")
  private String datetimeCreated;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @OneToOne(cascade = CascadeType.PERSIST)
  private Image image;

  @OneToMany(mappedBy = "post")
  private Set<Comment> comments;

  @OneToMany(mappedBy = "post")
  private List<LikedPost> likes = new ArrayList<>();

  public Post() {}

  public Post(String content) {
    this.content = content;
  }

  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public List<LikedPost> getLikes() {
    return likes;
  }

  public void setLikes(List<LikedPost> likes) {
    this.likes = likes;
  }

  public String getDatetimeCreated() {
    return datetimeCreated;
  }

  public void setDatetimeCreated(String datetimeCreated) {
    this.datetimeCreated = datetimeCreated;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Set<Comment> getComments() {
    return comments;
  }

  public void setComments(Set<Comment> comments) {
    this.comments = comments;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}