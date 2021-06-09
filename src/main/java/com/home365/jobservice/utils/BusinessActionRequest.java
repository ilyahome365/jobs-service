package com.home365.jobservice.utils;


import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class BusinessActionRequest {

    public static final String CHANGE_BILL_STATUS_JOB = " job of change bill status";
    public static final String CREATION_LATE_FEE = " job of creation late fee";
    public static final String LEASE_UPDATE = " job of lease update";
    public static final String PAYING_BILLS = " job of paying bills ";


    public static String getBusinessAction() {
        String businessAction = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            businessAction = request.getParameter("businessAction");
        }
        if (ObjectUtils.isEmpty(businessAction) && requestAttributes != null) {
            Object businessAction1 = requestAttributes.getAttribute("businessAction", RequestAttributes.SCOPE_REQUEST);
            if (businessAction1 instanceof String) {
                businessAction = (String) businessAction1;
            }
        }
        return businessAction;
    }


    public static  void setBusinessActionOnRequest(String businessAction) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                requestAttributes.setAttribute("businessAction", businessAction, RequestAttributes.SCOPE_REQUEST);
            }
    }

    public static String getUserId() {
        String userId = "HOME365_BOT";
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            String userIdFromRequest = request.getParameter("userId");
            if (!StringUtils.isEmpty(userIdFromRequest)) {
                userId = userIdFromRequest;
            }
        }
        return userId;
    }

}
