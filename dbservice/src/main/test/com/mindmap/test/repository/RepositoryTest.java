package com.mindmap.test.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.mindmap.Application;
import com.mindmap.model.Document;
import com.mindmap.model.Ownership;
import com.mindmap.model.Participation;
import com.mindmap.model.User;
import com.mindmap.model.Version;
import com.mindmap.repository.DocumentRepository;
import com.mindmap.repository.OwnershipRepository;
import com.mindmap.repository.ParticipationRepository;
import com.mindmap.repository.UserRepository;
import com.mindmap.repository.VersionRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class RepositoryTest {

	@Autowired
	UserRepository userRepository;
	@Autowired
	DocumentRepository documentRepository;
	@Autowired
	VersionRepository versionRepository;
	@Autowired
	ParticipationRepository participationRepository;
	@Autowired
	OwnershipRepository ownershipRepository;
	@Test
	public void test() {
		System.out.println(userRepository.findAll());
	}
	
	@Test
	public void createTestSet() {
		// create user
		for(int i = 0; i < 3; i++) {
			userRepository.save(new User("user"+i,"password"));			
		}
		// create version
		for(int i = 1; i < 5; i++) {
			versionRepository.save(new Version(i, "", "", 1));
		}
		for(int i = 5; i < 9; i++) {
			versionRepository.save(new Version(i, "", "", 2));
		}
		// create directory structure
		documentRepository.save(new Document("目录1", true));
		documentRepository.save(new Document("目录2", true));
		documentRepository.save(new Document("目录3", 1, true));
		documentRepository.save(new Document("目录4", true));
		// create document
		for(int i = 1; i < 4; i++) {
			documentRepository.save(new Document("文档"+i, i, 1));
		}
		for(int i = 4; i < 6; i++) {
			documentRepository.save(new Document("文档"+i, i, 2));
		}
		for(int i = 6; i < 7; i++) {
			documentRepository.save(new Document("文档"+i, i, 3));
		}
		for(int i = 7; i < 9; i++) {
			documentRepository.save(new Document("文档"+i, i, 4));
		}
		// create participation
		for(int i = 5; i < 8; i++) {
			participationRepository.save(new Participation(1, i));
		}
		participationRepository.save(new Participation(1, 1));
		
		// create ownership
		
		ownershipRepository.save(new Ownership(1, 2));
		for(int i = 8; i < 10; i++) {
			ownershipRepository.save(new Ownership(1, i));
		}
	}

}
