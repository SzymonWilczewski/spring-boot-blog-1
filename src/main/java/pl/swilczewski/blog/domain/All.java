package pl.swilczewski.blog.domain;

import lombok.Data;

import java.util.List;

@Data
public class All {
    private List<Author> authors;
    private List<Post> posts;
    private List<PostAuthor> postsAuthors;
    private List<Comment> comments;
    private List<Attachment> attachments;
}
