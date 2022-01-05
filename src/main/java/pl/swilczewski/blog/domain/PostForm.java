package pl.swilczewski.blog.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import pl.swilczewski.blog.validators.NotEmptyList;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostForm {
    @NotEmptyList
    private List<String> authors;

    private List<String> selectedAuthors;

    private Integer id;

    @NotEmpty(message="Post cannot be empty")
    private String post_content;

    @NotEmpty(message="Tags cannot be empty")
    private String tags;

    private List<String> filenames;

    private MultipartFile[] attachments;
}
