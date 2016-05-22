package com.roundrobin.common;

public enum ErrorCode {
  INVALID_FIELD(1001), 
  INVALID_SKILL_DETAIL_ID(1002), 
  INVALID_SKILL_GROUP_ID(1003), 
  INVALID_SKILL_ID(1004), 
  INVALID_PROFILE_ID(1005),  
  INVALID_BANK_ACCOUNT_ID(1007), 
  INVALID_CREDIT_CARD_ID(1008), 
  INVALID_CREDENTIAL_ID(1009), 
  INVALID_USER_ACTION_ID(1010),
  INVALID_SECRET(1011),
  ACTION_EXPIRED(1012),
  SKILL_ALREADY_EXISTS(1006),
  SKILL_GROUP_ALREADY_EXISTS(1013),
  INVALID_URL(1014),
  UNPARSABLE_INPUT(1015),
  INTERNAL_ERROR(5000);

  private int code;

  private ErrorCode(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }


}
