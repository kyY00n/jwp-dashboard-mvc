package webmvc.org.springframework.web.servlet.mvc.tobe;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import webmvc.org.springframework.web.servlet.ModelAndView;

public class HandlerExecution {

    private Object instance;
    private Method handler;

    public HandlerExecution(final Object instance, final Method handler) {
        this.instance = instance;
        this.handler = handler;
    }

    public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        return (ModelAndView) handler.invoke(instance, request, response);
    }
}
