package com.mindmap.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.mindmap.model.Ownership;

public interface OwnershipRepository extends CrudRepository<Ownership, Integer> {
	public List<Ownership> findByOwner(Integer id);
}
