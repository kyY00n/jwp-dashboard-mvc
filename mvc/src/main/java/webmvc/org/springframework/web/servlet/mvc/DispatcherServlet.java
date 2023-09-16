package webmvc.org.springframework.web.servlet.mvc;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webmvc.org.springframework.web.servlet.ModelAndView;
import webmvc.org.springframework.web.servlet.View;
import webmvc.org.springframework.web.servlet.mvc.tobe.AnnotationHandlerMapping;
import webmvc.org.springframework.web.servlet.mvc.tobe.HandlerExecution;
import webmvc.org.springframework.web.servlet.mvc.tobe.HandlerMapping;
import webmvc.org.springframework.web.servlet.view.JspView;

import static webmvc.org.springframework.web.servlet.view.JspView.REDIRECT_PREFIX;

public class DispatcherServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(DispatcherServlet.class);

    private List<HandlerMapping> handlerMappings;

    public DispatcherServlet(ServletContext servletContext) {
        handlerMappings = (List<HandlerMapping>) servletContext.getAttribute("handlerMappings");
    }

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException {
        log.debug("Method : {}, Request URI : {}", request.getMethod(), request.getRequestURI());
        try {
            handle(request, response);
        } catch (Exception e) {
            log.error("Exception : {}", e.getMessage(), e);
            throw new ServletException(e.getMessage());
        }
    }
    private void handle(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final HandlerExecution handlerExecution = getHandlerExecution(request);
        final ModelAndView modelAndView = handlerExecution.handle(request, response);
        renderModelAndView(request, response, modelAndView);
    }

    private HandlerExecution getHandlerExecution(final HttpServletRequest request) throws ServletException {
        return handlerMappings.stream()
                .map(handlerMapping -> handlerMapping.getHandler(request))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ServletException("핸들러를 찾을 수 없습니다."));
    }

    private void renderModelAndView(final HttpServletRequest request, final HttpServletResponse response,
                                    final ModelAndView modelAndView) throws Exception {
        final View view = modelAndView.getView();
        if (view.isRedirectView()) {
            response.sendRedirect(view.getViewName());
            return;
        }
        view.render(modelAndView.getModel(), request, response);
    }

}