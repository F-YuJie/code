package com.bw.fyj.controller;

import com.bw.fyj.service.IMailService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("test")
public class CustomerController {

    @Reference
    private IMailService iMailService;

    /**
     * 分页条件查询
     *
     * @param mv
     * @param emailcontent
     * @param page
     * @return
     */
    @GetMapping("/public/findByPageAndContent")
    public ModelAndView findByPageAndContent(ModelAndView mv,
                                             @RequestParam(value = "emailcontent", defaultValue = "") String emailcontent,
                                             @RequestParam(value = "page", defaultValue = "1") String page,
                                             @RequestParam(value = "size", defaultValue = "3") String size
    ) {
        ModelAndView byPageAndContent = iMailService.findByPageAndContent(emailcontent, Integer.parseInt(page), Integer.parseInt(size), mv);
        return byPageAndContent;
    }

    /**
     * solr条件查询
     *
     * @param emailcontent
     * @param page
     * @param size
     * @param mv
     * @return
     */
    @PostMapping("/public/findByContent")
    public ModelAndView findByContent(@RequestParam(value = "emailcontent", defaultValue = "") String emailcontent,
                                      @RequestParam(value = "page", defaultValue = "0") String page,
                                      @RequestParam(value = "size", defaultValue = "3") String size, ModelAndView mv) {
        ModelAndView byContent = iMailService.findByContent(emailcontent, Integer.parseInt(page), Integer.parseInt(size), mv);
        return byContent;
    }

}