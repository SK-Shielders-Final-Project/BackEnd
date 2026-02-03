package org.rookies.zdme.service;

import org.rookies.zdme.dto.app.AppIntegrityRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AppIntegrityService {

    @Value("${app.integrity.signature-hash}")
    private String expectedSignatureHash;

    @Value("${app.integrity.binary-hash}")
    private String expectedBinaryHash;

    public boolean verify(AppIntegrityRequest request) {
        if (request == null || request.getSignature_hash() == null || request.getBinary_hash() == null) {
            return false;
        }

        boolean signatureMatches = Objects.equals(expectedSignatureHash, request.getSignature_hash());
        boolean binaryMatches = Objects.equals(expectedBinaryHash, request.getBinary_hash());

        return signatureMatches && binaryMatches;
    }
}
