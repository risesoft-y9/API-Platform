package net.risesoft.util;

public class BizException extends RuntimeException {
    public BizException() {
    }

    public BizException(String message) {
        super(message);
    }
}
