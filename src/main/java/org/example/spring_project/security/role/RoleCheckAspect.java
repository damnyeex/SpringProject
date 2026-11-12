package org.example.spring_project.security.role;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class RoleCheckAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        HttpServletRequest request = attributes.getRequest();
        String userRole = (String) request.getAttribute("role");

        // Проверяем, входит ли роль в список разрешенных
        if (userRole == null || !Arrays.asList(requireRole.value()).contains(userRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: Insufficient rights");
        }
    }
}