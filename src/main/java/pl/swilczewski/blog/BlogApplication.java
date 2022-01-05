package pl.swilczewski.blog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import pl.swilczewski.blog.domain.*;
import pl.swilczewski.blog.storage.StorageProperties;
import pl.swilczewski.blog.storage.StorageService;

import java.util.List;

@SpringBootApplication
@ImportResource("classpath:beans.xml")
@EnableConfigurationProperties(StorageProperties.class)
public class BlogApplication {
	public static List<Author> authors;
	public static List<Post> posts;
	public static List<PostAuthor> postsAuthors;
	public static List<Comment> comments;
	public static List<Attachment> attachments;
	public static StorageService storageService;

	@Autowired
	public BlogApplication(List<Author> authors, List<Post> posts, List<PostAuthor> postsAuthors, List<Comment> comments, List<Attachment> attachments, StorageService storageService) {
		BlogApplication.authors = authors;
		BlogApplication.posts = posts;
		BlogApplication.postsAuthors = postsAuthors;
		BlogApplication.comments = comments;
		BlogApplication.attachments = attachments;
		BlogApplication.storageService = storageService;
	}

	public static void main(String[] args) {
		SpringApplication.run(BlogApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			storageService.deleteAll();
			storageService.init();
		};
	}

}
