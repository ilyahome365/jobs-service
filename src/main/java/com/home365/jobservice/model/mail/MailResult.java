package com.home365.jobservice.model.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailResult {
    private boolean completed;
    private String error;
    private String stackTrace;
    private List<MailSummary> mailSummaries;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MailSummary {
        private String id;
        private String to;
        private String status;
        private String error;


        @Override
        public String toString() {
            return "MailSummary{" +
                    "id ='" + id + '\'' +
                    ", to ='" + to + '\'' +
                    ", Status ='" + status + '\'' +
                    ", Reject Reason ='" + error + '\'' +
                    '}';
        }
    }
}
