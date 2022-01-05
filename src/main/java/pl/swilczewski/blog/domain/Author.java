package pl.swilczewski.blog.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    private Integer id;
    private String first_name;
    private String last_name;
    private String username;
}
