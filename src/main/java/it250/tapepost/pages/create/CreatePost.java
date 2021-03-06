/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it250.tapepost.pages.create;

import it250.tapepost.data.MemberDAO;
import it250.tapepost.data.PostDAO;
import it250.tapepost.entities.Comment;
import it250.tapepost.entities.Member;
import it250.tapepost.entities.Post;
import it250.tapepost.pages.Dashboard;
import it250.tapepost.pages.Posts;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

/**
 *
 * @author Dejan Ivanovic divanovic3d@gmail.com
 */
public class CreatePost {

    @Property
    private Comment comment;

    @InjectPage
    private Dashboard dashboardPage;

    @Property
    @SessionState
    private Member loggedInMember;
    @Property
    @Persist
    private Member member;

    @Inject
    private MemberDAO memberDao;

    @Property
    private Post memberPost;

    @Property
    private List<Post> memberPosts;

    @Property
    @Validate("required")
    private String postTitle;
    @Property
    @Validate("required")
    private String postContent;
    @Property
    private Post post;

    @InjectPage
    private Posts postsPage;

    @Inject
    private PostDAO postDao;

    @Property
    private boolean postSelected = false;
    @Inject
    private Request request;

    @Inject
    private AjaxResponseRenderer response;

    @InjectComponent
    private Zone commentZone;

    @CommitAfter
    Object onSuccessFromAddPostForm() {
        Post newPost = new Post(postTitle, postContent, loggedInMember);
        postDao.savePost(newPost);
        return postsPage;
    }

    /**
     * Find a Member instance that created a specified comment
     *
     * @param id
     * @return String of member's username
     */
    public String getCommentingMember(Integer id) {
        return memberDao.findMemberById(id).getMemberUsername();
    }

    /**
     * Return specified date in a readable format
     *
     * @param date
     * @return Date as a string, e.g. Jan 09
     */
    public String getFormattedDate(Date date) {
        return dashboardPage.getFormattedDate(date);
    }

    /**
     *
     * Return specified time in a readable format
     *
     * @param date
     * @return Date as a time string, e.g. 12:15
     */
    public String getFormattedTime(Date date) {
        return dashboardPage.getFormattedTime(date);
    }

    /**
     * Check if member has posts and display corresponding html content
     *
     * @return True if posts exist
     */
    public boolean getCheckPosts() {
        return member.getPosts().size() == 0 ? true : false;
    }

    /**
     * Find top 3 latest comments
     *
     * @return List of comments that belong to selected post
     */
    public List<Comment> getLatestComments() {
        List<Comment> latestComments = post.getComments();
        if (latestComments == null) {
            return new ArrayList<>();
        }
        Collections.sort(latestComments, new Comparator<Comment>() {
            public int compare(Comment o1, Comment o2) {
                return o1.getCommentTime().after(o2.getCommentTime()) ? -1 : 1;
            }
        });
        if (latestComments.size() < 3) {
            return latestComments.subList(0, latestComments.size());
        }
        return latestComments.subList(0, 3);
    }

    /**
     * Event triggered when post gets selected, creates ajax response
     *
     * @param id
     */
    public void onSelectPost(Integer id) {
        postSelected = true;
        post = postDao.findPostById(id);
        response.addRender(commentZone);
    }

    /**
     * Page activation context method to display a member selected from a
     * different page
     *
     * @param member
     */
    public void set(Member member) {
        this.member = member;
    }

    void onActivate(Member member) {
        this.member = member;
        memberPosts = member.getPosts();
        if (memberPosts == null) {
            memberPosts = new ArrayList<>();
        }
    }

    Member onPassivate() {
        return member;
    }
}
