package com.hgstudy.post.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Post {
    private Long id;
    private String title;
    private String content;
    private Long hit;
    private Long version;

    public Post(String title, String content, Long hit) {
        this.title = title;
        this.content = content;
        this.hit = hit;
    }
}
