package com.mindmap.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Version {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    Integer id;
    Integer document;
    String name;
    @Lob
    String content;
    String comment;
    Integer commiter;
    @CreatedDate
    Date commitAt;
    
    public Version() {}
    
	public Version(Integer document, String content, String comment, Integer commiter) {
		this.document = document;
		this.content = content;
		this.comment = comment;
		this.commiter = commiter;
		this.name = UUID.randomUUID().toString();
	}

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getDocument() {
		return document;
	}
	public void setDocument(Integer document) {
		this.document = document;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Integer getCommiter() {
		return commiter;
	}
	public void setCommiter(Integer commiter) {
		this.commiter = commiter;
	}
	public Date getCommitAt() {
		return commitAt;
	}
	public void setCommitAt(Date commitAt) {
		this.commitAt = commitAt;
	}
}
