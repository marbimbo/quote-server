package org.misio.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ports")
public class TopicController {

    private int orderPort;
    private int exceptionPort;

    @Value("${orderPort}")
    public void setOrderPort(int orderPort) {
        this.orderPort = orderPort;
    }

    @Value("${exceptionPort}")
    public void setExceptionPort(int exceptionPort) {
        this.exceptionPort = exceptionPort;
    }

    @GetMapping("/order")
    public int getOrderPort() {
        return orderPort;
    }

    @GetMapping("/exception")
    public int getExceptionPort() {
        return exceptionPort;
    }

}
