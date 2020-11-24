package com.home365.jobservice.model.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
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
        private boolean sent;
        private String id;
        private String to;
        private String status;
        private String error;


        @Override
        public String toString() {
            return "MailSummary{" +
                    "id ='" + id + '\'' +
                    ", to ='" + to + '\'' +
                    ", sent =" + sent +
                    ", Status ='" + status + '\'' +
                    ", Reject Reason ='" + error + '\'' +
                    '}';
        }
    }
}
