package com.mindmap.repository;

import org.springframework.data.repository.CrudRepository;

import com.mindmap.model.Document;

public interface DocumentRepository extends CrudRepository<Document, Integer>{
}
