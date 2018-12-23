package com.mindmap.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mindmap.model.Document;
import com.mindmap.model.Ownership;
import com.mindmap.model.Participation;
import com.mindmap.model.Response;
import com.mindmap.model.Version;
import com.mindmap.repository.DocumentRepository;
import com.mindmap.repository.OwnershipRepository;
import com.mindmap.repository.ParticipationRepository;
import com.mindmap.repository.VersionRepository;
import com.mindmap.token.TokenUtil;

@RestController
@RequestMapping("/document")
public class DocumentController {
	private final static Logger logger = LoggerFactory.getLogger(DocumentController.class);

	@Autowired
	DocumentRepository documentRepository;
	@Autowired
	ParticipationRepository participationRepository;
	@Autowired
	OwnershipRepository ownershipRepository;
	@Autowired
	VersionRepository versionRepository;

	// 实际得到的是最新的版本
	@RequestMapping(method = RequestMethod.GET)
	public Response getDocumet(@RequestParam("token") String token, @RequestParam("id") Integer id) {
		Response res;
		try {
			Integer uid = TokenUtil.validateToken(token, "token");
			if (null != participationRepository.findByParticipatorAndDocument(uid, id)) {
				Optional<Document> doc = documentRepository.findById(id);
				try {
					Optional<Version> version = versionRepository.findById(doc.get().getLatestVersion());
					res = new Response("success", version.get(), "");
				} catch (Exception e) {
					logger.error(e.getMessage());
					res = new Response("eroor", null, "没有此文档");
				}
			}
			res = new Response("eroor", null, "没有权限查看此文档");
		} catch (Exception e) {
			logger.error(e.getMessage());
			res = new Response("eroor", null, "没有权限查看此文档");
		}
		return res;
	}

	@RequestMapping(method = RequestMethod.POST)
	@Transactional
	public Response createDocument(@RequestParam("token") String token, @RequestBody Document doc) {
		Response res;

		try {
			Integer uid = TokenUtil.validateToken(token, "token");
			doc = documentRepository.save(doc);
			ownershipRepository.save(new Ownership(uid, doc.getId()));
			res = new Response("success", doc, "");
		} catch (Exception e) {
			logger.error(e.getMessage());
			res = new Response("error", null, "创建文档失败");
		}
		return res;
	}

	@ResponseBody
	@RequestMapping(value = "/doc", method = RequestMethod.GET)
	public Response getDocuments(@RequestParam("token") String token) {
		Response res = null;
		List<Document> ownership = new ArrayList<>();
		List<Document> participation = new ArrayList<>();
		Map<String, List<Document>> docs = new HashMap<>();
		try {
			int uid = TokenUtil.validateToken(token, "token");
			
			for(Ownership os: ownershipRepository.findByOwner(uid)) {
				ownership.add(documentRepository.findById(os.getDocument()).get());
			}
			docs.put("ownership", ownership);
			for(Participation pp: participationRepository.findByParticipator(uid)) {
				participation.add(documentRepository.findById(pp.getDocument()).get());
			}
			docs.put("participation", participation);
			
			res = new Response("success", docs, "");
		} catch (Exception e) {
			logger.error(e.getMessage());
			res = new Response("error", null, e.getMessage());
		}
		return res;
	}

	// 添加一名参与者
		@ResponseBody
		@RequestMapping(value = "/addpart", method = RequestMethod.GET)
		public Response addPaticipator(@RequestParam("utoken")String utoken,@RequestParam("dtoken") String dtoken) {
			Response res = null;
			try{
				int uid = TokenUtil.validateToken(utoken, "token");//
				int docid = TokenUtil.validateToken(dtoken, "invitation");
				Participation part =new Participation(uid,docid); 
				part = participationRepository.save(part);
				Document doc = documentRepository.findById(part.getDocument()).get();
				res=new Response("success", doc,"");
			}catch (Exception e) {
					logger.error(e.getMessage());
					res = new Response("error",null, "添加参与者失败"+e.getMessage());
				}
			return res;
		}
		// 得到文档的所有版本
		@ResponseBody
		@RequestMapping(value = "/getvers", method = RequestMethod.GET)
		public Response getVersions(@RequestParam("token")String token, @RequestParam("did") Integer did) {
			Response res = null;
			List<Version> version = new ArrayList<>();
			try {
				TokenUtil.validateToken(token, "token");//
				version = versionRepository.findIdByDocument(did);
				res = new Response("success", version, "");
			} catch (Exception e) {
				logger.error(e.getMessage());
				res = new Response("error", null, "获取所有版本失败");
			}
			return res;
		}
		// 得到文档的指定版本
		@ResponseBody
		@RequestMapping(value = "/getver", method = RequestMethod.GET)
		public Response getVersion(@RequestParam("token")String token, @RequestParam("vid") Integer vid) {
			Response res = null;
			try {
				TokenUtil.validateToken(token, "token");//
				Optional<Version> version = versionRepository.findById(vid);
				res = new Response("success", version, "");
			} catch (Exception e) {
				logger.error(e.getMessage());
				res = new Response("error", null, "获取指定版本失败");
			}
			return res;
		}
		// 生成invitation token
		@ResponseBody
		@RequestMapping(value = "/invitation", method = RequestMethod.GET)
		public Response getInvitationToken(@RequestParam("token") String token, 
				@RequestParam("did")Integer did) {
			Response res = null;
			try {
				TokenUtil.validateToken(token, "token");
				String ntoken =TokenUtil.createInvitation(did);
				res = new Response("success", ntoken, "");
			} catch (Exception e) {
				logger.error(e.getMessage());
				res = new Response("error", null, "生成token失败");
			}
			return res;
		}

		// 提交一个版本
		@ResponseBody
		@RequestMapping(value = "/mitver", method = RequestMethod.POST)
		@Transactional
		public Response commitVersion(@RequestParam("token")String token, @RequestBody Version version) {
			Response res = null;
			try {
				TokenUtil.validateToken(token, "token");
				version.setName(UUID.randomUUID().toString());
				version = versionRepository.save(version);
				logger.debug("version saved");
				try {
					Document doc = documentRepository.findById(version.getDocument()).get();
					doc.setLatestVersion(version.getId());
					documentRepository.save(doc);
					logger.debug("version saved");
				}catch (Exception e) {
					logger.error(e.getMessage());
					res = new Response("error", null, "提交版本失败");
				}
				
				res = new Response("success", version, "");
			} catch (Exception e) {
				logger.error(e.getMessage());
				res = new Response("error", null, "提交版本失败");
			}
			return res;
		}
}
