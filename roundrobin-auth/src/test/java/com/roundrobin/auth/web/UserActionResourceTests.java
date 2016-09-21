package com.roundrobin.auth.web;

import static com.roundrobin.auth.error.AuthErrorCode.INVALID_SECRET;
import static com.roundrobin.auth.error.AuthErrorCode.INVALID_USERNAME;
import static com.roundrobin.auth.error.AuthErrorCode.INVALID_USER_ID;
import static com.roundrobin.auth.error.AuthErrorCode.USER_ALREADY_EXIST;
import static com.roundrobin.auth.error.AuthErrorCode.USER_ALREADY_VERIFIED;
import static com.roundrobin.error.ErrorCode.INVALID_FIELD;
import static com.roundrobin.error.ErrorCode.UNPARSABLE_INPUT;
import static com.roundrobin.error.ErrorType.INVALID_REQUEST_ERROR;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import com.roundrobin.api.Response;
import com.roundrobin.auth.api.UserActionTo;
import com.roundrobin.auth.api.UserTo;
import com.roundrobin.auth.domain.User;
import com.roundrobin.auth.domain.UserAction;
import com.roundrobin.auth.service.UserService;

/**
 * Created by rengasu on 5/12/16.
 */
public class UserActionResourceTests extends ResourceTests {
  // TODO Include all test cases
  // TODO Messages for all error code
  // TODO Handle all other api failures like 404/400
  // TODO Complete pom file
  // TODO Add indexes to columns

  @Autowired
  private UserService userService;

  @Test
  public void testCreate() {
    UserTo userTo = new UserTo();
    userTo.setUsername(Optional.of("testing" + System.currentTimeMillis() + "@testing.com"));
    userTo.setVendor(Optional.of(false));
    userTo.setPassword(Optional.of("testing"));
    Response<Boolean> created =
        helper.post(authUrl + "user-action/create-user", userTo, new ParameterizedTypeReference<Response<Boolean>>() {})
            .getBody();
    assertThat(created.getEntity(), notNullValue());
    assertThat(created.getEntity(), is(true));
  }

  @Test
  public void testCreateWithEmptyValue() {
    UserTo userProfileTo = new UserTo();
    Response<String> created = helper
        .post(authUrl + "user-action/create-user", userProfileTo, new ParameterizedTypeReference<Response<String>>() {})
        .getBody();
    assertThat(created.getEntity(), nullValue());
    assertThat(created.getError(), notNullValue());
    assertThat(created.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(created.getError().getCode(), is(INVALID_FIELD));
    assertThat(created.getError().getMessage(), is("Invalid values specified for fields"));
    assertThat(created.getError().getFieldErrors(),
        hasItems("username: may not be empty", "password: may not be empty", "vendor: may not be null"));
  }

  @Test
  public void testCreateWithInvalidValue() {
    Map<String, String> userTo = new HashMap<>();
    userTo.put("username", "testing");
    userTo.put("password", "testingdaskdjaskjdkasjdkjaskdjkasjdkasjdkasjdkasjdkajsdkjaksdjkasdjkajs");
    Response<String> created =
        helper.post(authUrl + "user-action/create-user", userTo, new ParameterizedTypeReference<Response<String>>() {})
            .getBody();
    assertThat(created.getEntity(), nullValue());
    assertThat(created.getError(), notNullValue());
    assertThat(created.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(created.getError().getCode(), is(INVALID_FIELD));
    assertThat(created.getError().getMessage(), is("Invalid values specified for fields"));
    assertThat(created.getError().getFieldErrors(), hasItems("username: not a well-formed email address",
        "vendor: may not be null", "password: length must be between 0 and 35"));
  }

  @Test
  public void testCreateWithInvalidVendor() {
    Map<String, String> userProfileTo = new HashMap<>();
    userProfileTo.put("vendor", "testing");
    Response<String> created = helper
        .post(authUrl + "user-action/create-user", userProfileTo, new ParameterizedTypeReference<Response<String>>() {})
        .getBody();
    assertThat(created.getError(), notNullValue());
    assertThat(created.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(created.getError().getCode(), is(UNPARSABLE_INPUT));
    assertThat(created.getError().getMessage(), is("Unable to parse input"));
  }

  @Test
  public void testCreateWithExistingName() {
    String username = createUser();
    UserTo userTo = new UserTo();
    userTo.setUsername(Optional.of(username));
    userTo.setVendor(Optional.of(false));
    userTo.setPassword(Optional.of("testing"));
    Response<String> created =
        helper.post(authUrl + "user-action/create-user", userTo, new ParameterizedTypeReference<Response<String>>() {})
            .getBody();
    assertThat(created.getError(), notNullValue());
    assertThat(created.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(created.getError().getCode(), is(USER_ALREADY_EXIST));
    assertThat(created.getError().getMessage(), is("User already present"));
  }

  @Test
  public void testCreateWithEmptyJson() {
    Response<String> created = helper
        .post(authUrl + "user-action/create-user", "", new ParameterizedTypeReference<Response<String>>() {}).getBody();
    assertThat(created.getError(), notNullValue());
    assertThat(created.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(created.getError().getCode(), is(UNPARSABLE_INPUT));
    assertThat(created.getError().getMessage(), is("Unable to parse input"));
  }

  @Test
  public void testVerify() {
    String username = createUser();
    User user = userService.getByUsername(username);
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setUserId(user.getId());
    userActionTo.setSecret(user.getActions().get(0).getSecret());
    Response<Boolean> verify = helper
        .post(authUrl + "user-action/verify", userActionTo, new ParameterizedTypeReference<Response<Boolean>>() {})
        .getBody();
    assertThat(verify.getEntity(), notNullValue());
    assertThat(verify.getEntity(), is(true));
  }

  @Test
  public void testDuplicateVerify() {
    String username = createUser();
    User user = userService.getByUsername(username);
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setUserId(user.getId());
    userActionTo.setSecret(user.getActions().get(0).getSecret());
    Response<Boolean> verify = helper
        .post(authUrl + "user-action/verify", userActionTo, new ParameterizedTypeReference<Response<Boolean>>() {})
        .getBody();
    assertThat(verify.getEntity(), notNullValue());
    assertThat(verify.getEntity(), is(true));
    verify = helper
        .post(authUrl + "user-action/verify", userActionTo, new ParameterizedTypeReference<Response<Boolean>>() {})
        .getBody();
    assertThat(verify.getEntity(), nullValue());
    assertThat(verify.getError(), notNullValue());
    assertThat(verify.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(verify.getError().getCode(), is(INVALID_SECRET));
    assertThat(verify.getError().getMessage(), is("Invalid secret"));
  }

  @Test
  public void testVerifyWithEmptyValues() {
    Response<String> verify = helper
        .post(authUrl + "user-action/verify", new UserActionTo(), new ParameterizedTypeReference<Response<String>>() {})
        .getBody();
    assertThat(verify.getEntity(), nullValue());
    assertThat(verify.getError(), notNullValue());
    assertThat(verify.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(verify.getError().getCode(), is(INVALID_FIELD));
    assertThat(verify.getError().getMessage(), is("Invalid values specified for fields"));
    assertThat(verify.getError().getFieldErrors(), hasItems("secret: may not be empty", "userId: may not be empty"));
  }

  @Test
  public void testVerifyWithInvalidProfileId() {
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setUserId("testing");
    userActionTo.setSecret("testing");
    Response<String> verify =
        helper.post(authUrl + "user-action/verify", userActionTo, new ParameterizedTypeReference<Response<String>>() {})
            .getBody();
    assertThat(verify.getEntity(), nullValue());
    assertThat(verify.getError(), notNullValue());
    assertThat(verify.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(verify.getError().getCode(), is(INVALID_USER_ID));
    assertThat(verify.getError().getMessage(), is("Invalid user id"));
  }

  @Test
  public void testVerifyWithInvalidSecretId() {
    String username = createUser();
    User user = userService.getByUsername(username);
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setUserId(user.getId());
    userActionTo.setSecret("testing");
    Response<Boolean> verify = helper
        .post(authUrl + "user-action/verify", userActionTo, new ParameterizedTypeReference<Response<Boolean>>() {})
        .getBody();
    assertThat(verify.getEntity(), nullValue());
    assertThat(verify.getError(), notNullValue());
    assertThat(verify.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(verify.getError().getCode(), is(INVALID_SECRET));
    assertThat(verify.getError().getMessage(), is("Invalid secret"));
  }

  @Test
  public void testVerifyWithWrongProfileId() {
    String username = createUser();
    User user = userService.getByUsername(username);
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setUserId(userService.getByUsername(createUser()).getId());
    userActionTo.setSecret(user.getActions().get(0).getSecret());
    Response<Boolean> verify = helper
        .post(authUrl + "user-action/verify", userActionTo, new ParameterizedTypeReference<Response<Boolean>>() {})
        .getBody();
    assertThat(verify.getEntity(), nullValue());
    assertThat(verify.getError(), notNullValue());
    assertThat(verify.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(verify.getError().getCode(), is(INVALID_SECRET));
    assertThat(verify.getError().getMessage(), is("Invalid secret"));
  }

  @Test
  public void testRequestVerify() {
    String username = createUser();
    User user = userService.getByUsername(username);
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail(user.getUsername());
    Response<Boolean> requestVerify = helper.post(authUrl + "user-action/request-verify", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(requestVerify.getEntity(), notNullValue());
    assertThat(requestVerify.getEntity(), is(true));
  }

  @Test
  public void testRequestVerifyWithEmptyValues() {
    Response<Boolean> verify = helper.post(authUrl + "user-action/request-verify", new UserActionTo(),
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(verify.getEntity(), nullValue());
    assertThat(verify.getError(), notNullValue());
    assertThat(verify.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(verify.getError().getCode(), is(INVALID_FIELD));
    assertThat(verify.getError().getMessage(), is("Invalid values specified for fields"));
    assertThat(verify.getError().getFieldErrors(), contains("email: may not be empty"));
  }

  @Test
  public void testRequestVerifyWithInvalidValues() {
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail("testing");
    Response<Boolean> verify = helper.post(authUrl + "user-action/request-verify", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(verify.getEntity(), nullValue());
    assertThat(verify.getError(), notNullValue());
    assertThat(verify.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(verify.getError().getCode(), is(INVALID_FIELD));
    assertThat(verify.getError().getMessage(), is("Invalid values specified for fields"));
    assertThat(verify.getError().getFieldErrors(), contains("email: not a well-formed email address"));
  }

  @Test
  public void testRequestVerifyWithUnknownEmail() {
    String email = System.currentTimeMillis() + "@testing.com";
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail(email);
    Response<Boolean> verify = helper.post(authUrl + "user-action/request-verify", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(verify.getEntity(), nullValue());
    assertThat(verify.getError(), notNullValue());
    assertThat(verify.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(verify.getError().getCode(), is(INVALID_USERNAME));
    assertThat(verify.getError().getMessage(), is("Invalid username"));
  }

  @Test
  public void testRequestVerifyForVerifiedProfile() {
    String username = createUser();
    User user = userService.getByUsername(username);
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setUserId(user.getId());
    userActionTo.setSecret(user.getActions().get(0).getSecret());
    Response<Boolean> verify = helper
        .post(authUrl + "user-action/verify", userActionTo, new ParameterizedTypeReference<Response<Boolean>>() {})
        .getBody();
    assertThat(verify.getEntity(), notNullValue());
    assertThat(verify.getEntity(), is(true));
    userActionTo = new UserActionTo();
    userActionTo.setEmail(user.getUsername());
    Response<Boolean> requestVerify = helper.post(authUrl + "user-action/request-verify", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(requestVerify.getEntity(), nullValue());
    assertThat(requestVerify.getError(), notNullValue());
    assertThat(requestVerify.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(requestVerify.getError().getCode(), is(USER_ALREADY_VERIFIED));
    assertThat(requestVerify.getError().getMessage(), is("User already verified"));
  }

  @Test
  public void testRequestResetPassword() {
    String username = createUser();
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail(username);
    Response<Boolean> reset = helper.post(authUrl + "user-action/request-reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), notNullValue());
    assertThat(reset.getEntity(), is(true));
  }

  @Test
  public void testRequestResetPasswordWithEmptyValues() {
    UserActionTo userActionTo = new UserActionTo();
    Response<Boolean> reset = helper.post(authUrl + "user-action/request-reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), nullValue());
    assertThat(reset.getError(), notNullValue());
    assertThat(reset.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(reset.getError().getCode(), is(INVALID_FIELD));
    assertThat(reset.getError().getMessage(), is("Invalid values specified for fields"));
    assertThat(reset.getError().getFieldErrors(), contains("email: may not be empty"));
  }

  @Test
  public void testRequestResetPasswordWithInvalidValues() {
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail("testing");
    Response<Boolean> reset = helper.post(authUrl + "user-action/request-reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), nullValue());
    assertThat(reset.getError(), notNullValue());
    assertThat(reset.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(reset.getError().getCode(), is(INVALID_FIELD));
    assertThat(reset.getError().getMessage(), is("Invalid values specified for fields"));
    assertThat(reset.getError().getFieldErrors(), contains("email: not a well-formed email address"));
  }

  @Test
  public void testRequestResetPasswordWithUnknownEmail() {
    String email = System.currentTimeMillis() + "@testing.com";
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail(email);
    Response<Boolean> reset = helper.post(authUrl + "user-action/request-reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), nullValue());
    assertThat(reset.getError(), notNullValue());
    assertThat(reset.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(reset.getError().getCode(), is(INVALID_USERNAME));
    assertThat(reset.getError().getMessage(), is("Invalid username"));
  }

  @Test
  public void testResetPassword() {
    String username = createUser();
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail(username);
    Response<Boolean> reset = helper.post(authUrl + "user-action/request-reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), notNullValue());
    assertThat(reset.getEntity(), is(true));
    User user = userService.getByUsername(username);
    UserAction userAction = null;
    for (UserAction action : user.getActions()) {
      if (action.getAction() == UserAction.UserActionType.RESET_PASSWORD) {
        userAction = action;
        break;
      }
    }
    userActionTo = new UserActionTo();
    userActionTo.setUserId(user.getId());
    userActionTo.setSecret(userAction.getSecret());
    userActionTo.setPassword("newpassword");
    reset = helper.post(authUrl + "user-action/reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), notNullValue());
    assertThat(reset.getEntity(), is(true));
  }

  @Test
  public void testResetPasswordWithEmptyValues() {
    UserActionTo userActionTo = new UserActionTo();
    Response<Error> reset = helper.post(authUrl + "user-action/reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Error>>() {}).getBody();
    assertThat(reset.getEntity(), nullValue());
    assertThat(reset.getError(), notNullValue());
    assertThat(reset.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(reset.getError().getCode(), is(INVALID_FIELD));
    assertThat(reset.getError().getMessage(), is("Invalid values specified for fields"));
    assertThat(reset.getError().getFieldErrors(),
        hasItems("userId: may not be empty", "secret: may not be empty", "password: may not be empty"));
  }

  @Test
  public void testResetPasswordWithInvalidSecret() {
    String username = createUser();
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail(username);
    Response<Boolean> reset = helper.post(authUrl + "user-action/request-reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), notNullValue());
    assertThat(reset.getEntity(), is(true));
    User user = userService.getByUsername(username);
    userActionTo = new UserActionTo();
    userActionTo.setUserId(user.getId());
    userActionTo.setSecret("testing");
    userActionTo.setPassword("newpassword");
    reset = helper.post(authUrl + "user-action/reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), nullValue());
    assertThat(reset.getError(), notNullValue());
    assertThat(reset.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(reset.getError().getCode(), is(INVALID_SECRET));
    assertThat(reset.getError().getMessage(), is("Invalid secret"));
  }

  @Test
  public void testResetPasswordWithInvalidUserId() {
    String username = createUser();
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail(username);
    Response<Boolean> reset = helper.post(authUrl + "user-action/request-reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), notNullValue());
    assertThat(reset.getEntity(), is(true));
    userActionTo = new UserActionTo();
    userActionTo.setUserId("testing");
    userActionTo.setSecret("testing");
    userActionTo.setPassword("newpassword");
    reset = helper.post(authUrl + "user-action/reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), nullValue());
    assertThat(reset.getError(), notNullValue());
    assertThat(reset.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(reset.getError().getCode(), is(INVALID_USER_ID));
    assertThat(reset.getError().getMessage(), is("Invalid user id"));
  }

  @Test
  public void testResetPasswordWithWrongUserId() {
    String username = createUser();
    UserActionTo userActionTo = new UserActionTo();
    userActionTo.setEmail(username);
    Response<Boolean> reset = helper.post(authUrl + "user-action/request-reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), notNullValue());
    assertThat(reset.getEntity(), is(true));
    userActionTo = new UserActionTo();
    User user = userService.getByUsername(username);
    UserAction userAction = null;
    for (UserAction action : user.getActions()) {
      if (action.getAction() == UserAction.UserActionType.RESET_PASSWORD) {
        userAction = action;
        break;
      }
    }
    userActionTo.setUserId(userService.getByUsername(createUser()).getId());
    userActionTo.setSecret(userAction.getSecret());
    userActionTo.setPassword("newpassword");
    reset = helper.post(authUrl + "user-action/reset-password", userActionTo,
        new ParameterizedTypeReference<Response<Boolean>>() {}).getBody();
    assertThat(reset.getEntity(), nullValue());
    assertThat(reset.getError(), notNullValue());
    assertThat(reset.getError().getType(), is(INVALID_REQUEST_ERROR));
    assertThat(reset.getError().getCode(), is(INVALID_SECRET));
    assertThat(reset.getError().getMessage(), is("Invalid secret"));
  }
}
