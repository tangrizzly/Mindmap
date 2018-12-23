package com.mindmap.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

import net.minidev.json.JSONObject;

/**
 * @author Tangrizzly
 * @email  tangyyuanr@gmail.com
 * @create 2018/6/5
 */

public class TokenUtil {

    private final static Logger logger = LoggerFactory.getLogger(TokenUtil.class);
    private static String secret = "doyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecretdoyouknowmysecret";
    private static String invitation = "invitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNoteinvitationNote666";
    private static long threshold = 1000 * 60 * 60 * 4;    // four hours later the token will be expired
    private static long invitationThreshold = 1000 * 60 *60 * 24 * 7;   // one week later the invitation will be expired

    public static String createToken(Integer userId) throws KeyLengthException {

        JSONObject userInfo = new JSONObject();
        userInfo.put("uid", userId.toString());
        userInfo.put("exp", System.currentTimeMillis() + threshold);

        Payload payload = new Payload(userInfo);

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256, JOSEObjectType.JWT, null, null, null,
                null, null, null, null, null, null, null, null);

        // Create JWS object
        JWSObject jwsObject = new JWSObject(header, payload);

        // Create HMAC signer
        JWSSigner signer = new MACSigner(secret);
        try
        {
            jwsObject.sign(signer);

        } catch (JOSEException e)
        {
            logger.error("Couldn't sign JWS object: " + e.getMessage());
            return null;
        }
        // Serialise JWS object to compact format
        String token = jwsObject.serialize();

        return token;
    }

    public static String createInvitation(Integer documentId) throws KeyLengthException {

        JSONObject userInfo = new JSONObject();
        userInfo.put("did", documentId.toString());
        userInfo.put("exp", System.currentTimeMillis() + invitationThreshold);

        Payload payload = new Payload(userInfo);

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256, JOSEObjectType.JWT, null, null, null,
                null, null, null, null, null, null, null, null);

        // Create JWS object
        JWSObject jwsObject = new JWSObject(header, payload);

        // Create HMAC signer
        JWSSigner signer = new MACSigner(invitation);
        try
        {
            jwsObject.sign(signer);

        } catch (JOSEException e)
        {
            logger.error("Couldn't sign JWS object: " + e.getMessage());
            return null;
        }
        return jwsObject.serialize();
    }

    public static Integer validateToken(String token, String type) throws Exception {
        String varifier = null;
        if (type.equals("invitation")) {
            varifier = invitation;
        } else if (type.equals("token")) {
            varifier = secret;
        } else {
            throw new Exception("Wrong token type.");
        }
        JWSObject jwsObject = JWSObject.parse(token);
        Payload payload = jwsObject.getPayload();
        JWSVerifier verifier = new MACVerifier(varifier);
        if (!jwsObject.verify(verifier)) {
            throw new Exception("Token failed verified: " + token);
        }
        JSONObject jsonOBj = payload.toJSONObject();
        long extTime = Long.valueOf(jsonOBj.get("exp").toString());
        long curTime = System.currentTimeMillis();
        if (curTime > extTime) {
            throw new Exception("Token expired: " + token);
        }
        if (type.equals("invitation")) {
            return Integer.valueOf(jsonOBj.get("did").toString());
        } else {
            return Integer.valueOf(jsonOBj.get("uid").toString());
        }

    }

}