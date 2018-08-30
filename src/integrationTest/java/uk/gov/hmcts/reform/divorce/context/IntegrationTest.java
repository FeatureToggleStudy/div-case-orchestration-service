package uk.gov.hmcts.reform.divorce.context;

import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.IdamUtils;

import java.util.Locale;
import java.util.UUID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {
    private static final String CASE_WORKER_USERNAME = "robreallywantsccdaccess@mailinator.com";
    private static final String CASE_WORKER_PASSWORD = "Passw0rd";

    @Value("${case.orchestration.service.base.uri}")
    protected String serverUrl;

    @Autowired
    private IdamUtils idamUtils;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    protected IntegrationTest() {
        System.setProperty("http.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "proxyout.reform.hmcts.net");
        System.setProperty("https.proxyPort", "8080");
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    protected synchronized UserDetails getUserDetails(boolean isCitizen) {
        return isCitizen ? createCitizen() : createCaseworker();
    }

    private UserDetails createCitizen() {
        final String username = "simulate-delivered" + UUID.randomUUID();
        final String password = UUID.randomUUID().toString().toUpperCase(Locale.ENGLISH);
        final String emailAddress = username + "@gmail.com";

        final RegisterUserRequest registerUserRequest =
                RegisterUserRequest.builder()
                        .email(emailAddress)
                        .forename(username)
                        .password(password)
                        .build();

        idamUtils.createUserInIdam(registerUserRequest);

        final String authToken = idamUtils.authenticateUser(emailAddress, password);

        final String userId = idamUtils.getUserId(authToken);

        return UserDetails.builder()
                .id(userId)
                .username(username)
                .emailAddress(emailAddress)
                .password(password)
                .authToken(authToken)
                .build();
    }

    private UserDetails createCaseworker() {
        idamUtils.createDivorceCaseworkerUserInIdam(CASE_WORKER_USERNAME, CASE_WORKER_PASSWORD);
        final String authToken = idamUtils.generateUserTokenWithNoRoles(CASE_WORKER_USERNAME, CASE_WORKER_PASSWORD);

        return UserDetails.builder()
                .username(CASE_WORKER_USERNAME)
                .emailAddress(CASE_WORKER_USERNAME)
                .password(CASE_WORKER_PASSWORD)
                .authToken(authToken)
                .id(idamUtils.getUserId(authToken))
                .build();
    }

    protected String getUserToken(boolean isCitizen) {
        return getUserDetails(isCitizen).getAuthToken();
    }
}
