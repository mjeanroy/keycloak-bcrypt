package com.github.leroyguillaume.keycloak.bcrypt;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.credential.PasswordCredentialModel;

/**
 * @author <a href="mailto:pro.guillaume.leroy@gmail.com">Guillaume Leroy</a>
 */
public class BCryptPasswordHashProvider implements PasswordHashProvider {
    private final int defaultIterations;
    private final String providerId;
    private final BCrypt.Hasher bCryptHasher;
    private final BCrypt.Verifyer bCryptVerifier;

    public BCryptPasswordHashProvider(final String providerId, final int defaultIterations) {
        this.providerId = providerId;
        this.defaultIterations = defaultIterations;
        this.bCryptHasher =  BCrypt.with(BCrypt.Version.VERSION_2Y, LongPasswordStrategies.none());
        this.bCryptVerifier = BCrypt.verifyer(BCrypt.Version.VERSION_2Y, LongPasswordStrategies.none());
    }

    @Override
    public boolean policyCheck(final PasswordPolicy policy, final PasswordCredentialModel credential) {
        final int policyHashIterations = policy.getHashIterations() == -1 ? defaultIterations : policy.getHashIterations();

        return credential.getPasswordCredentialData().getHashIterations() == policyHashIterations
                && providerId.equals(credential.getPasswordCredentialData().getAlgorithm());
    }

    @Override
    public PasswordCredentialModel encodedCredential(final String rawPassword, final int iterations) {
        final String encodedPassword = encode(rawPassword, iterations);

        // bcrypt salt is stored as part of the encoded password so no need to store salt separately
        return PasswordCredentialModel.createFromValues(providerId, new byte[0], iterations, encodedPassword);
    }

    @Override
    public String encode(final String rawPassword, final int iterations) {
        final int cost = iterations == -1 ? defaultIterations : iterations;
        return bCryptHasher.hashToString(cost, rawPassword.toCharArray());
    }

    @Override
    public void close() {

    }

    @Override
    public boolean verify(final String rawPassword, final PasswordCredentialModel credential) {
        final String hash = credential.getPasswordSecretData().getValue();
        final BCrypt.Result verifier = bCryptVerifier.verify(rawPassword.toCharArray(), hash.toCharArray());
        return verifier.verified;
    }
}
