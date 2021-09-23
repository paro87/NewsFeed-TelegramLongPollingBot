package com.paro.newsfeed.model;

import java.util.ArrayList;
import java.util.List;

public class CommentNode<Comment> {
    private List<CommentNode<Comment>> children = new ArrayList<>();
    private CommentNode<Comment> parent = null;

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    private Comment comment;
    boolean root=false;

    public CommentNode() {
    }

    public List<CommentNode<Comment>> getChildren() {
        return children;
    }

    public void setChildren(List<CommentNode<Comment>> children) {
        this.children = children;
    }

    public void setParent(CommentNode<Comment> parent) {
        parent.addChild(this);
        this.parent = parent;
        this.root=false;
    }

    public void addChild(CommentNode<Comment> child) {
        this.children.add(child);
        this.root=true;
    }




}
