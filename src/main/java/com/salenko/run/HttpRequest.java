package com.salenko.run;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.util.EntityUtils;

public class HttpRequest {

    public static void doPost(final String code, String requestURL, CloseableHttpAsyncClient httpclient,
            final CountDownLatch latch1, final ArrayList<String> reSends) throws InterruptedException {

        final HttpPost request = new HttpPost(requestURL);
        HttpEntity entity = MultipartEntityBuilder.create().addTextBody("code", code).build();
        request.setEntity(entity);
        httpclient.execute(request, new FutureCallback<HttpResponse>() {

            public void completed(final HttpResponse response) {
                latch1.countDown();
                String responseString;
                try {
                    responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
                    if (responseString.contains("503 Service Temporarily Unavailable") || responseString.contains("failed")) {
                        System.out.println("Re-send" + code);
                        reSends.add(code);
                    } else {
                        if (!responseString.contains("WRONG") && !responseString.contains("405 Not Allowed")) {
                            System.out.println("Right code: " + code);
                            System.out.println(responseString);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void failed(final Exception ex) {
                latch1.countDown();
                System.out.println("failed");
            }

            public void cancelled() {
                latch1.countDown();
                System.out.println("cancelled");
            }

        });
        latch1.await();
    }

}
