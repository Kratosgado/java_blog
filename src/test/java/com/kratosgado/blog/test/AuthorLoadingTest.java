package com.kratosgado.blog.test;

import com.kratosgado.blog.dao.PostDAO;
import com.kratosgado.blog.models.Post;
import java.util.List;
import java.util.Optional;

public class AuthorLoadingTest {
    public static void main(String[] args) {
        System.out.println("Testing automatic author loading...");
        
        PostDAO postDAO = new PostDAO();
        
        // Test getPostById with author loading
        Optional<Post> post = postDAO.getPostById(1);
        if (post.isPresent()) {
            Post p = post.get();
            System.out.println("Post: " + p.getTitle());
            System.out.println("Author: " + p.getAuthorName());
            System.out.println("Created: " + p.getCreatedAt());
        } else {
            System.out.println("No post found with ID 1");
        }
        
        // Test getAllPosts with author loading
        System.out.println("\nTesting getAllPosts with author loading...");
        List<Post> posts = postDAO.getAllPosts();
        System.out.println("Found " + posts.size() + " posts:");
        
        for (int i = 0; i < Math.min(3, posts.size()); i++) {
            Post p = posts.get(i);
            System.out.println("- " + p.getTitle() + " by " + p.getAuthorName());
        }
    }
}