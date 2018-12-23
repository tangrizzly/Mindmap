package com.mindmap.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.mindmap.model.Version;

public interface VersionRepository extends CrudRepository<Version, Integer>{
	public List<Version> findIdByDocument(Integer did);
}
