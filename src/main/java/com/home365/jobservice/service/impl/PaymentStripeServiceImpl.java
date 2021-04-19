package com.home365.jobservice.service.impl;

import com.home365.jobservice.model.StripeChargeRequest;
import com.home365.jobservice.model.StripePayment;
import com.home365.jobservice.service.PaymentStripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PaymentStripeServiceImpl implements PaymentStripeService {

    @Override
    public Charge createCharge(String stripeCustomerId,
                               int amount,
                               String destinationStripeAccountId,
                               String description,
                               boolean addAchFee) throws StripeException {
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", amount);
        chargeParams.put("currency", "usd");
        chargeParams.put("customer", stripeCustomerId);
        Map<String, Object> transferDataParams = new HashMap<>();
        transferDataParams.put("destination", destinationStripeAccountId);
        chargeParams.put("transfer_data", transferDataParams);
        return Charge.create(chargeParams);
    }

    // create payment and send to payment service
    @Override
    public StripePayment createPaymentTypeAndSendForPayment(String destination,
                                                            String locationKey,
                                                            String stripeCustomerKey,
                                                            double amount,
                                                            String accountId,
                                                            String ownerName,
                                                            String description) throws StripeException {
        log.info("OwnerDrawJob.createPaymentTypeAndSendForPayment");
        int amountConverted = (int) amount;
        log.info("destination = " + destination + ", locationKey = " + locationKey + ", stripeCustomerKey = " + stripeCustomerKey + ", amount = " + amountConverted);

        StripeChargeRequest stripeChargeRequest = new StripeChargeRequest();
        stripeChargeRequest.setDestinationStripeAccountId(destination);
        stripeChargeRequest.setStripeCustomerId(stripeCustomerKey);
        stripeChargeRequest.setStripeLocationKey(locationKey);
        stripeChargeRequest.setAmount(amountConverted);
        stripeChargeRequest.setAddAchFee(true);
        stripeChargeRequest.setDescription(description + " for AccountId  : " + accountId + " Name :  " + ownerName);
        return createCharge(stripeChargeRequest);
    }

    private StripePayment createCharge(StripeChargeRequest stripeChargeRequest) throws StripeException {
        Stripe.apiKey = stripeChargeRequest.getStripeLocationKey();
        Map<String, Object> chargeParams = new HashMap<>();

        chargeParams.put("amount", stripeChargeRequest.getAmount());
        chargeParams.put("currency", "usd");
        chargeParams.put("description", stripeChargeRequest.getDescription());
        chargeParams.put("customer", stripeChargeRequest.getStripeCustomerId());
        Map<String, Object> transferDataParams = new HashMap<>();
        transferDataParams.put("destination", stripeChargeRequest.getDestinationStripeAccountId());
        chargeParams.put("transfer_data", transferDataParams);
        log.info("Stripe Request : " + transferDataParams);
        Charge charge = Charge.create(chargeParams);
        StripePayment stripePayment = new StripePayment();
        stripePayment.setId(charge.getId());
        stripePayment.setTransfer(charge.getTransfer());
        log.info("Stripe Response : " + stripePayment);
        return stripePayment;
    }
}
