package com.mindmap.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.mindmap.model.Participation;

public interface ParticipationRepository extends CrudRepository<Participation, Integer>{
	public List<Participation> findByParticipator(Integer id);
	public Participation findByParticipatorAndDocument(Integer p, Integer d);
}
