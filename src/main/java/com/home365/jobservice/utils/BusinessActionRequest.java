package com.home365.jobservice.utils;


import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class BusinessActionRequest {
    public  static final String  CREATION_OF_ONE_RATE = " creation of One-Rate plan ";
    public  static final String  UPDATING_OF_ONE_RATE = " updating to One-Rate plan";
    public  static final String  EDITING_PLAN = " editing plan";
    public  static final String  CANCELING_BILL = " canceling bill ";
    public  static final String  CANCELING_CHECK = " canceling check ";
    public static final String APPROVING_BILL = " approving of bill ";
    public static final String PAYMENT_OF_BILL = " payment of bill ";
    public static final String RESENDING_CHECK = " resending check  ";
    public static final String CANCELING_BILLS = " canceling bills ";
    public static final String MATERIAL_TRANSFER = " Material transfer ";
    public static final String CREATION_OF_TERMINATION_FEE = "  creation of termination fee ";
    public static final String CREATION_OF_ONE_RATE_PLAN = " creation of oneRate plan";
    public static final String STRIPE_PAYMENT = " stripe payment ";
    public static final String  PAYMENT_OF_CHARGE  = "  Payment for charge  ";
    public static final String  CHARGE_CREATION  = "  create charge  ";
    public static final String  DISPOSITION_CHARGE  = "  disposition charge  ";
    public static final String  DISPOSITION_PAYMENT  = "   disposition payment ";
    public static final String  DEPOSIT_RELEASE  = "   deposit release ";
    public static final String  FIRST_CONTRIBUTION  = "   first contribution of new owner ";


    public static String getBusinessAction() {
        String businessAction = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            businessAction = request.getParameter("businessAction");
        }
        if (ObjectUtils.isEmpty(businessAction)) {
            Object businessAction1 = requestAttributes.getAttribute("businessAction", RequestAttributes.SCOPE_REQUEST);
            if (businessAction1 != null && businessAction1 instanceof String) {
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
