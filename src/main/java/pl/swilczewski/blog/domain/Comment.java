package pl.swilczewski.blog.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Integer id;

    @NotNull
    @Size(min=2, message="Username is too short")
    private String username;

    @NotNull
    private Integer id_post;

    @NotEmpty(message="Comment cannot be empty")
    private String comment_content;
}
