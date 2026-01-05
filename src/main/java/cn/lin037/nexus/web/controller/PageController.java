package cn.lin037.nexus.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器 - 返回前端模板
 * 数据通过前端JavaScript调用后端API获取
 */
@Slf4j
@Controller
public class PageController {

    /**
     * 主页面 - 学习空间管理
     */
    @GetMapping({"", "/", "/index", "/spaces"})
    public String index() {
        checkLogin();
        return "index";
    }

    /**
     * 知识图谱页面
     */
    @GetMapping("/knowledge-graph")
    public String knowledgeGraph() {
        checkLogin();
        return "knowledge-graph";
    }

    /**
     * 智能体对话页面
     */
    @GetMapping("/agent-chat")
    public String agentChat() {
        checkLogin();
        return "agent-chat";
    }

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * 注册页面
     */
    @GetMapping("/register")
    public String register() {
        return "register";
    }

    /**
     * 检查用户是否登录
     */
    private void checkLogin() {
        StpUtil.checkLogin();
    }
}
