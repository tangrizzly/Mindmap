package com.mindmap.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.mindmap.model.User;

public interface UserRepository extends CrudRepository<User, Integer>{
	public User findByName(String name);
	public Optional<User> findById(Integer id);
}
