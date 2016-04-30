package com.roundrobin.exception;

import com.roundrobin.common.ErrorCode;

public class ClientException extends GeneralException {

  private static final long serialVersionUID = 4665827751058454341L;
  private ErrorCode code;

  public ClientException(ErrorCode code) {
    this.code = code;
  }

  public ErrorCode getCode() {
    return code;
  }

  public void setCode(ErrorCode code) {
    this.code = code;
  }


}
