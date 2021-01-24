package com.home365.jobservice.ascpect;


import com.home365.jobservice.entities.IAuditableEntity;
import com.home365.jobservice.service.AuditEventService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Aspect
@Component
@Slf4j
public class AuditAspect {


    final
    AuditEventService auditEventService;


    public AuditAspect(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    /**
     * Pointcut that catch all persistInDb
     */
    @Pointcut("execution(* com.home365.jobservice.repository.*.save*(..))")
    public void commonSave() {
    }



    /**
     * Pointcut that catch all persistInDb
     */
    @Pointcut("execution(* com.home365.jobservice.repository.*+.saveAll*(..))")
    public void commonSaveAll() {

    }


    @Before("commonSaveAll()")
    public void addAuditingInfoBulk(JoinPoint joinPoint){
        String userId = getUserId();
        Object[] args = joinPoint.getArgs();
        log.debug("Before auditing from method : {} ",joinPoint.getSignature().getName());
        if(args[0] instanceof List){
            List<Object> list = (List<Object>) args[0];
            for (Object obj : list) {
                if (obj instanceof IAuditableEntity) {
                    auditEventService.audit(userId, (IAuditableEntity) obj, true);
                }
            }
        }
    }


    @Before("commonSave()")
    public void addAuditingInformation(JoinPoint joinPoint) throws Throwable {
        String userId = getUserId();
        Object[] args = joinPoint.getArgs();
        log.debug("Before auditing from method : {} ",joinPoint.getSignature().getName());
        if(args[0] instanceof IAuditableEntity){
            auditEventService.audit(userId,(IAuditableEntity)args[0],true );
        }
    }


    private String getUserId() {
        String userId = "HOME365_BOT";
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
            String userIdFromRequest = request.getParameter("userId");
            if(!StringUtils.isEmpty(userIdFromRequest)){
                userId = userIdFromRequest;
            }
        }
        return userId;
    }



}
