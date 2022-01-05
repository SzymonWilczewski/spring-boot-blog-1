package pl.swilczewski.blog.controllers.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.swilczewski.blog.BlogApplication;
import pl.swilczewski.blog.domain.*;
import pl.swilczewski.blog.storage.StorageService;

import javax.validation.Valid;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {
    private List<Author> authors = BlogApplication.authors;
    private List<Post> posts = BlogApplication.posts;
    private List<PostAuthor> postsAuthors = BlogApplication.postsAuthors;
    private List<Comment> comments = BlogApplication.comments;
    private List<Attachment> attachments = BlogApplication.attachments;
    private final StorageService storageService = BlogApplication.storageService;

    // Posts

    @GetMapping("/")
    public String getPosts(Model model, @RequestParam(required=false) String c, @RequestParam(required=false) String f) {
        if (c != null && f != null) {
            switch (c) {
                case "p":
                    if (f.matches("-?\\d+")) {
                        return "redirect:/post/" + f;
                    } else {
                        return "error";
                    }
                case "pc":
                    List<Post> postsContent = posts.stream()
                            .filter(p -> p.getPost_content().matches("(?i).*" + f + ".*"))
                            .sorted(Comparator.comparing(Post::getId).reversed())
                            .collect(Collectors.toList());
                    model.addAttribute("posts", postsContent);
                    return "posts";
                case "t":
                    List<Post> postsTags = posts.stream()
                            .filter(p -> p.getTags().matches("(?i).*\\b" + f + "\\b.*"))
                            .sorted(Comparator.comparing(Post::getId).reversed())
                            .collect(Collectors.toList());
                    model.addAttribute("posts", postsTags);
                    return "posts";
                case "ap":
                    if (f.matches("-?\\d+")) {
                        return "redirect:/author/" + f + "/posts";
                    } else {
                        return "error";
                    }
                case "uc":
                    return "redirect:/user/" + f + "/comments";
                default:
                    model.addAttribute("posts", posts.stream()
                            .sorted(Comparator.comparing(Post::getId).reversed())
                            .collect(Collectors.toList()));
                    return "posts";
            }
        } else {
            model.addAttribute("posts", posts.stream()
                    .sorted(Comparator.comparing(Post::getId).reversed())
                    .collect(Collectors.toList()));
            return "posts";
        }
    }

    // Post

    @GetMapping("/post/{id}")
    public String getPostId(Model model, @PathVariable int id) {
        Post post = posts.stream()
                .filter(p -> p.getId() == id)
                .findAny()
                .orElse(null);

        if (post != null) {
            List<Integer> postsAuthorsIds_ = postsAuthors.stream()
                    .filter(pa -> pa.getId_post() == id)
                    .map(PostAuthor::getId_author)
                    .collect(Collectors.toList());
            List<Author> authors_ = authors.stream()
                    .filter(a -> postsAuthorsIds_.contains(a.getId()))
                    .sorted(Comparator.comparing(Author::getUsername))
                    .collect(Collectors.toList());

            if (!ObjectUtils.isEmpty(authors_)) {
                model.addAttribute("authors", authors_);
            }
            model.addAttribute("post", post);
            model.addAttribute("comment", new Comment());
            model.addAttribute("comments", comments.stream()
                    .filter(c -> c.getId_post() == id)
                    .sorted(Comparator.comparing(Comment::getId).reversed())
                    .collect(Collectors.toList()));
            return "postId";
        } else {
            return "error";
        }
    }

    @GetMapping("/new/post")
    public String newPost(Model model) {
        PostForm postForm = new PostForm();
        postForm.setAuthors(authors.stream()
                .map(Author::getUsername)
                .collect(Collectors.toList()));
        postForm.setSelectedAuthors(new ArrayList<>());
        model.addAttribute("postForm", postForm);
        return "newPost";
    }

    @PostMapping("/new/post")
    public String postNewPost(Model model, @Valid PostForm postForm, Errors errors) {
        if (errors.hasErrors()) {
            postForm.setSelectedAuthors(postForm.getAuthors());
            postForm.setAuthors(authors.stream()
                    .map(Author::getUsername)
                    .collect(Collectors.toList()));
            model.addAttribute("postForm", postForm);
            return "newPost";
        }

        try {
            int postId = posts.stream().mapToInt(Post::getId).max().orElse(0) + 1;
            List<Author> authors_ = authors.stream()
                    .filter(a -> postForm.getAuthors().contains(a.getUsername()))
                    .collect(Collectors.toList());

            authors_.forEach(a -> postsAuthors.add(new PostAuthor(postId, a.getId())));
            posts.add(new Post(postId, postForm.getPost_content(), postForm.getTags()));
            Arrays.asList(postForm.getAttachments()).forEach(f -> {
                if (!f.isEmpty()) {
                    String filename = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS_").format(new Date(System.currentTimeMillis())) + f.getOriginalFilename();
                    attachments.add(new Attachment(postId, filename));
                    storageService.store(f, filename);
                }
            });

            return "redirect:/";
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/edit/post/{id}")
    public String editPostId(Model model, @PathVariable int id) {
        Post post = posts.stream()
                .filter(p -> p.getId() == id)
                .findAny()
                .orElse(null);

        if (post != null) {
            List<Integer> postsAuthorsIds_ = postsAuthors.stream()
                    .filter(pa -> pa.getId_post() == id)
                    .map(PostAuthor::getId_author)
                    .collect(Collectors.toList());
            PostForm postForm = new PostForm();
            postForm.setAuthors(authors.stream()
                    .map(Author::getUsername)
                    .collect(Collectors.toList()));
            postForm.setSelectedAuthors(authors.stream()
                    .filter(a -> postsAuthorsIds_.contains(a.getId()))
                    .map(Author::getUsername)
                    .collect(Collectors.toList()));
            postForm.setId(post.getId());
            postForm.setPost_content(post.getPost_content());
            postForm.setTags(post.getTags());
            postForm.setFilenames(attachments.stream()
                    .filter(a -> a.getId_post() == id)
                    .map(Attachment::getFilename)
                    .collect(Collectors.toList()));
            model.addAttribute("postForm", postForm);
            return "editPost";
        } else {
            return "error";
        }
    }

    @PostMapping("/edit/post/{id}")
    public String postEditPostId(Model model, @PathVariable int id, @Valid PostForm postForm, Errors errors) {
        if (errors.hasErrors()) {
            postForm.setSelectedAuthors(postForm.getAuthors());
            postForm.setAuthors(authors.stream()
                    .map(Author::getUsername)
                    .collect(Collectors.toList()));
            model.addAttribute("postForm", postForm);
            return "editPost";
        }

        try {
            List<Integer> authors_ = authors.stream()
                    .filter(a -> postForm.getAuthors().contains(a.getUsername()))
                    .map(Author::getId)
                    .collect(Collectors.toList());

            // Delete old authors
            postsAuthors = postsAuthors.stream()
                    .filter(pa -> authors_.contains(pa.getId_author()) || pa.getId_post() != id)
                    .collect(Collectors.toList());
            // Add new authors
            authors_.removeAll(postsAuthors.stream().filter(pa -> pa.getId_post() == id).map(PostAuthor::getId_author).collect(Collectors.toList()));
            authors_.forEach(a -> postsAuthors.add(new PostAuthor(id, a)));
            posts = posts.stream()
                    .map(p -> p.getId() == id ? new Post(id, postForm.getPost_content(), postForm.getTags()) : p)
                    .collect(Collectors.toList());
            Arrays.asList(postForm.getAttachments()).forEach(f -> {
                if (!f.isEmpty()) {
                    String filename = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS_").format(new Date(System.currentTimeMillis())) + f.getOriginalFilename();
                    attachments.add(new Attachment(id, filename));
                    storageService.store(f, filename);
                }
            });

            return "redirect:/post/" + id;
        } catch (Exception e) {
            System.out.println("Exception");
            e.printStackTrace();
            return "error";
        }
    }

    @GetMapping("/delete/post/{id}")
    public String deletePostId(@PathVariable int id) {
        Post post = posts.stream()
                .filter(p -> p.getId() == id)
                .findAny()
                .orElse(null);

        if (post != null) {
            posts.remove(post);
            return "redirect:/";
        } else {
            return "error";
        }
    }

    // Comment

    @PostMapping("/post/{id_post}")
    public String postComment(Model model, @PathVariable int id_post, @Valid Comment comment, Errors errors) {
        if (errors.hasErrors()) {
            model.addAttribute("post", posts.stream()
                    .filter(p -> p.getId() == id_post)
                    .findAny()
                    .orElse(null));
            model.addAttribute("comment", comment);
            model.addAttribute("comments", comments.stream()
                    .filter(c -> c.getId_post() == id_post)
                    .sorted(Comparator.comparing(Comment::getId).reversed())
                    .collect(Collectors.toList()));
            return "postId";
        }

        comment.setId(comments.stream().mapToInt(Comment::getId).max().orElse(0) + 1);
        comments.add(comment);
        return "redirect:/post/" + id_post;
    }

    @GetMapping("/post/{id_post}/comment/{id}")
    public String editComment(Model model, @PathVariable int id_post, @PathVariable int id) {
        Post post = posts.stream()
                .filter(p -> p.getId() == id_post)
                .findAny()
                .orElse(null);

        if (post != null) {
            Comment comment = comments.stream()
                    .filter(c -> c.getId() == id)
                    .findAny()
                    .orElse(null);
            if (comment != null) {
                model.addAttribute("comment", comment);
                return "editComment";
            } else {
                model.addAttribute("message", "Comment not found");
                return "error";
            }
        } else {
            model.addAttribute("message", "Post not found");
            return "error";
        }
    }

    @PostMapping("/post/{id_post}/comment/{id}")
    public String editComment(Model model, @PathVariable int id_post, @PathVariable int id, @Valid Comment comment, Errors errors) {
        if (errors.hasErrors()) {
            model.addAttribute("comment", comment);
            return "editComment";
        }

        comments = comments.stream()
                .map(c -> c.getId() == id ? comment : c)
                .collect(Collectors.toList());
        return "redirect:/post/" + id_post;
    }

    @GetMapping("/delete/comment/{id}")
    public String deleteCommentId(@PathVariable int id) {
        Comment comment = comments.stream()
                .filter(c -> c.getId() == id)
                .findAny()
                .orElse(null);

        if (comment != null) {
            comments.remove(comment);
            return "redirect:/post/" + comment.getId_post();
        } else {
            return "error";
        }
    }

    @GetMapping("/user/{username}/comments")
    public String getUserUsernameComments(Model model, @PathVariable String username) {
        List<Comment> comments_ = comments.stream()
                .filter(c -> c.getUsername().matches("(?i)\\b" + username + "\\b"))
                .sorted(Comparator.comparing(Comment::getId).reversed())
                .collect(Collectors.toList());

        if (!ObjectUtils.isEmpty(comments_)) {
            model.addAttribute("comments", comments_);
            return "userUsernameComments";
        } else {
            model.addAttribute("message", "No comments found");
            return "error";
        }
    }

    // Authors

    @GetMapping("/authors")
    public String getAuthors(Model model) {
        model.addAttribute("authors", authors);
        return "authors";
    }

    @GetMapping("/author/{id}/posts")
    public String getAuthorIdPosts(Model model, @PathVariable int id) {
        Author author = authors.stream()
                .filter(a -> a.getId() == id)
                .findAny()
                .orElse(null);

        if (author != null) {
            List<Integer> postsIds = postsAuthors.stream()
                    .filter(pa -> pa.getId_author() == id)
                    .mapToInt(PostAuthor::getId_post)
                    .boxed()
                    .collect(Collectors.toList());
            List<Post> posts_ = posts.stream()
                    .filter(p -> postsIds.contains(p.getId()))
                    .sorted(Comparator.comparing(Post::getId).reversed())
                    .collect(Collectors.toList());

            if (!ObjectUtils.isEmpty(posts_)) {
                model.addAttribute("posts", posts_);
                return "authorIdPosts";
            } else {
                model.addAttribute("message", "No posts found");
                return "error";
            }
        } else {
            return "error";
        }
    }

    // Stats

    @GetMapping("/stats")
    public String getStats(Model model) {
        String[][] stats = {
                {"Number of authors", String.valueOf(authors.size())},
                {"Number of posts", String.valueOf(posts.size())},
                {"Number of comments", String.valueOf(comments.size())},
                {"Number of attachments", String.valueOf(attachments.size())},
                {"Average post length", String.valueOf(posts.stream().mapToInt(p -> p.getPost_content().length()).sum() / posts.size())},
                {"Average comment length", String.valueOf(comments.stream().mapToInt(p -> p.getComment_content().length()).sum() / comments.size())},
        };
        model.addAttribute("stats", stats);
        return "stats";
    }

    // Files

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + Objects.requireNonNull(file.getFilename()).substring(24) + "\"").body(file);
    }

    @GetMapping("/delete/file/{filename:.+}")
    public String deleteFile(@PathVariable String filename) throws IOException {
        Files.delete(Paths.get("upload/" + filename));
        Attachment attachment = attachments.stream().filter(a -> a.getFilename().equals(filename)).findAny().orElse(null);

        if (attachment != null) {
            attachments.remove(attachment);
            return "redirect:/edit/post/" + attachment.getId_post();
        } else {
            return "error";
        }
    }

    @PostMapping("/import")
    public String csvImport(Model model, @RequestParam("file") MultipartFile file) {
        try {
            String str = new String(file.getBytes());
            ObjectMapper objectMapper = new ObjectMapper();
            All all = objectMapper.readValue(str, All.class);
            authors = all.getAuthors();
            posts = all.getPosts();
            postsAuthors = all.getPostsAuthors();
            comments = all.getComments();
            attachments = all.getAttachments();
            return "redirect:/stats";
        } catch (Exception e) {
            model.addAttribute("message", "Cannot open file!");
            return "error";
        }
    }


    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<InputStreamResource> export() throws IOException {
        String filename = "export-" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date(System.currentTimeMillis())) + ".csv";
        Path path = Paths.get("upload/" + filename);
        All all = new All();
        all.setAuthors(authors);
        all.setPosts(posts);
        all.setPostsAuthors(postsAuthors);
        all.setComments(comments);
        all.setAttachments(attachments);
        ObjectMapper objectMapper = new ObjectMapper();
        Writer writer = new FileWriter(path.toString());
        writer.write(objectMapper.writeValueAsString(all));
        writer.close();

        InputStreamResource isr = new InputStreamResource(new FileInputStream(path.toString()) {
            @Override
            public void close() throws IOException {
                super.close();
                Files.delete(path);
            }
        });
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename + "\"").body(isr);
    }

}
