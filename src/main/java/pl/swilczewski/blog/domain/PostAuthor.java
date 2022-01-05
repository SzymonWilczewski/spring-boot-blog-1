package pl.swilczewski.blog.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostAuthor {
    private Integer id_post;
    private Integer id_author;
}
