package com.climbassist.main;

import com.climbassist.metrics.Metrics;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Main controller that handles all GET requests that are not for an API.
 */
@Controller
public class MainController {

    @Metrics(api = "Index")
    // we handle all paths here, as the front-end takes care of displaying a 404 if the page doesn't exist
    @RequestMapping(path = "/**", method = RequestMethod.GET)
    public String index() {
        return "forward:/static/index.html";
    }
}
