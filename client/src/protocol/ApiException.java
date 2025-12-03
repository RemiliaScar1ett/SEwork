package protocol;

/**
 * 后端返回 success=false 时，抛出的统一异常。
 * UI 层可以根据 errorCode 决定提示内容和逻辑。
 */
public class ApiException extends RuntimeException {
    private final String errorCode;

    public ApiException(String errorCode,String message){
        super(message!=null?message:"");
        this.errorCode=errorCode!=null?errorCode:"";
    }

    public String getErrorCode(){
        return errorCode;
    }

    @Override
    public String toString(){
        return "ApiException{errorCode='"+errorCode+"', message='"+getMessage()+"'}";
    }
}
