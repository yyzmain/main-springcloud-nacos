package com.main.exception;

import com.main.result.MainResult;
import com.main.result.MainResultGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 全局统一异常处理,对特定的几个异常进行捕获及处理，这里不处理的，会转到ErrorController进行处理
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String DEFAULT_ALERT = "服务器内部错误,请联系管理员.";

    /**
     * 内部代码抛出错误统一捕获处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public MainResult<String> handleServerException(Exception ex) {
        log.error(DEFAULT_ALERT, ex);
        return MainResultGenerator.createFailResult(DEFAULT_ALERT);
    }

    /**
     * 当使用了 @Validated + @RequestBody 注解但是没有在绑定的数据对象后面跟上 Errors 类型的参数声明的话，
     * Spring MVC 框架会抛出 MethodArgumentNotValidException 异常。
     * 当抛出 MethodArgumentNotValidException 异常,就会被相应的异常处理捕捉到理(有点类似于aop的意思)
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) //指定异常类型
    @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public MainResult<String> exception(MethodArgumentNotValidException e) {
        log.error("异常，{}", e.getMessage());
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();

        String message = allErrors.stream().map(s -> s.getDefaultMessage()).collect(Collectors.joining(";"));


        return MainResultGenerator.createFailResult(message);

    }

    /**
     * 处理服务地址正确，但是http请求方法错误的情况
     **/
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public MainResult<String> handleNotFoundException(Exception ex) {
        log.error(DEFAULT_ALERT, ex);
        return MainResultGenerator.createFailResult(ex.getMessage());
    }

    /**
     * 请求参数错误
     **/
    @ExceptionHandler(IllegalArgumentException.class)
    public MainResult<String> illegalArgumentException(Exception ex) {
        log.error("请求参数错误,", ex);
        return MainResultGenerator.createFailResult("请求参数错误");
    }


    /**
     * 1.HttpMessageNotReadableException：处理post 请求时参数类型错误
     * 2.MethodArgumentTypeMismatchException：处理get 请求时参数类型错误
     **/
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public MainResult<String> parameterTypeErrorException(Exception e) {
        log.error("参数类型错误,", e);
        return MainResultGenerator.createFailResult("参数类型错误");
    }

    /**
     * GET请求时，缺少必须参数（参数形式为？号后面的一串，缺少路径上的参数时，匹配到404）
     **/
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public MainResult<String> missingParameterException(MissingServletRequestParameterException e) {
        log.error(DEFAULT_ALERT, e);
        return MainResultGenerator.createFailResult(e.getMessage());
    }

    @ExceptionHandler(CustomizeRuntimeException.class)
    public MainResult<String> handleFileOperationsException(CustomizeRuntimeException e) {
        return MainResultGenerator.createFailResult(e.getMessage());
    }
}