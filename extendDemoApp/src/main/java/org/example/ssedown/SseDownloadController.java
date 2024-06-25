package org.example.ssedown;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RestController
public class SseDownloadController {
    @Autowired
    ThreadPoolTaskExecutor threadPoolTaskExecutor;
    public static Map<String, SseEmitter> subscribeMap = new ConcurrentHashMap<>();

    @CrossOrigin
    @GetMapping(value = "/download")
    public SseEmitter download(String param) throws IOException, InterruptedException {

        SseEmitter sseEmitter = new SseEmitter(0L); // 永久有效;
            sseEmitter.onCompletion(() -> {
                System.out.println("结束了");
            });
            sseEmitter.onError(throwable -> {
                System.err.println(throwable);
            });
            sseEmitter.onTimeout(()->{
                System.out.println("timeout");
            });
        SseEmitter finalSseEmitter1 = sseEmitter;
        threadPoolTaskExecutor.execute(()->{
            //begin
            SseEmitter.SseEventBuilder beginEvent = SseEmitter.event()
                    .data("begin")
                    .id("-1")
                    .name("sse event - mvc");
            try {
                finalSseEmitter1.send(beginEvent);
            } catch (IOException e) {
                finalSseEmitter1.completeWithError(e);
            }
            //for
            for(int i=0; i < 10; i++){
                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .data("SSE MVC - " + LocalTime.now().toString())
                        .id(String.valueOf(i))
                        .name("sse event - mvc");
                try {
                    Thread.sleep(1000);
                    finalSseEmitter1.send(event);
                } catch (IOException e) {
                    finalSseEmitter1.completeWithError(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            //finish
            SseEmitter.SseEventBuilder finishevent = SseEmitter.event()
                    .data("finish")
                    .id("finish")
                    .name("sse event - mvc");
            try {
                finalSseEmitter1.send(finishevent);
            } catch (IOException e) {
                finalSseEmitter1.completeWithError(e);
            }
            finalSseEmitter1.complete();
        });

//        sseMvcExecutor.shutdown();

        return sseEmitter;

    }
}
