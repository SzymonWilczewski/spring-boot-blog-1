package pl.swilczewski.blog.controllers.api;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.swilczewski.blog.domain.Author;
import pl.swilczewski.blog.domain.Post;
import pl.swilczewski.blog.domain.PostAuthor;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ApiController {
    private final List<Author> authors;
    private final List<Post> posts;
    private final List<PostAuthor> postsAuthors;

    @Autowired
    public ApiController(List<Author> authors, List<Post> posts, List<PostAuthor> postsAuthors) {
        this.authors = authors;
        this.posts = posts;
        this.postsAuthors = postsAuthors;
    }

    @JsonSerialize
    public static class EmptyJson {}

    private static final File[] EmptyArray = {};

    @GetMapping("/api/post/{id}")
    public ResponseEntity<?> getPost(@PathVariable int id) {
        Post post = posts.stream()
                .filter(p -> p.getId() == id)
                .findAny()
                .orElse(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        if (post != null) {
            return new ResponseEntity<>(post, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new EmptyJson(), headers, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/api/author/{id}/posts")
    public ResponseEntity<?> getAuthorIdPosts(@PathVariable int id) {
        Author author = authors.stream()
                .filter(a -> a.getId() == id)
                .findAny()
                .orElse(null);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        if (author != null) {
            List<Integer> postsIds = postsAuthors.stream()
                    .filter(pa -> pa.getId_author() == id)
                    .mapToInt(PostAuthor::getId_post)
                    .boxed()
                    .collect(Collectors.toList());
            List<Post> posts_ = posts.stream()
                    .filter(p -> postsIds.contains(p.getId()))
                    .collect(Collectors.toList());

            if (!ObjectUtils.isEmpty(posts_)) {
                return new ResponseEntity<>(posts_, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(EmptyArray, headers, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(EmptyArray, headers, HttpStatus.NOT_FOUND);
        }
    }
}
