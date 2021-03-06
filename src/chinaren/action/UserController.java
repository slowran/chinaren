package chinaren.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import chinaren.model.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import chinaren.common.SessionContext;
import chinaren.service.UserService;
import chinaren.util.CaptchaUtil;

/**
 * 用户相关请求控制器
 * @ClassName UserController 
 * @author 李浩然
 * @date 2017年7月24日
 * @version 1.0
 */
@Controller
public class UserController {

	@Autowired
	private UserService userService;
	
	private Logger logger = Logger.getLogger(UserController.class);

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss - ");
	
    /**
     * 处理未登录界面
     * @author 李浩然
     * @return 跳转视图名称
     */
	@RequestMapping(value = "/no_login", method = { RequestMethod.GET, RequestMethod.POST })
	public String noLogin() {
		return "no_login";
	}
	
	/**
	 * 处理验证码获取请求
	 * @author 李浩然
	 * @param request HTTP请求实体
	 * @param response HTTP响应实体
	 * @throws ServletException Servlet处理异常
	 * @throws IOException IO处理异常
	 */
	@RequestMapping(value = "/captcha", method = RequestMethod.GET)
    @ResponseBody
    public void captcha(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CaptchaUtil.outputCaptcha(request, response);
    }

	/**
	 * 处理头像获取请求
	 * @author 李浩然
	 * @param request HTTP请求实体
	 * @param response HTTP响应实体
	 */
    @RequestMapping(value = "/headImage", method = RequestMethod.GET)
    @ResponseBody
    public void displayHeadImage(HttpServletRequest request, HttpServletResponse response) {
        userService.outputHeadImage(Long.parseLong(
                request.getSession().getAttribute(SessionContext.ATTR_USER_ID).toString()), request, response);
    }

    /**
     * 处理头像获取请求
     * @author 李浩然
     * @param userId 待获取头像的用户ID
     * @param request HTTP请求实体
     * @param response HTTP响应实体
     */
    @RequestMapping(value = "/othersHeadImage", method = RequestMethod.GET)
    @ResponseBody
    public void displayHeadImage(@RequestParam("userId") long userId, HttpServletRequest request, HttpServletResponse response) {
        userService.outputHeadImage(userId, request, response);
    }

    /**
     * 处理发送邮件验证码请求
     * @author 李浩然
     * @param email 邮箱地址
     * @param response  HTTP响应实体
     * @throws IOException IO处理异常
     */
    @RequestMapping(value = "/sendEmail", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public void sendEmail(@RequestParam("email") String email, HttpServletResponse response) throws IOException {
        logger.info(dateFormat.format(new Date()) + "send email to " + email);
        PrintWriter out = response.getWriter();
	    if (userService.sendEmail(email).isSuccessful()) {
            out.append("successful");
            out.flush();
        } else {
            out.append("failed");
            out.flush();
        }
    }

    /**
     * 处理首页界面获取请求
     * @author 李浩然
     * @param session Session实体
     * @param displayName 用户名称
     * @return 跳转视图及相关模型
     * @throws ParseException 解析处理异常
     */
	@RequestMapping(value = "/main", method = RequestMethod.GET)
	public ModelAndView mainPage(HttpSession session,
                                       @SessionAttribute(SessionContext.ATTR_USER_NAME) String displayName)
            throws ParseException {
		long userId = Long.parseLong(session.getAttribute(SessionContext.ATTR_USER_ID).toString());
        User user = userService.getUserInformation(userId).getResult();
        StatisticsResult statisticsResult = userService.getStatisticsResult(userId);
		return new ModelAndView("main")
                .addObject("display_name", displayName)
                .addObject("dateStrings", statisticsResult.getDataString())
                .addObject("classNames", statisticsResult.getClassNames())
                .addObject("counts", statisticsResult.getCounts())
				.addObject("user", user);
	}

	/**
	 * 处理注销请求
	 * @author 李浩然
	 * @param session Session实体
	 * @return 跳转视图名称
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpSession session) {
        userService.logout(Long.parseLong(session.getAttribute(SessionContext.ATTR_USER_ID).toString()), session);
        return "redirect:/login";
    }

	/**
	 * 处理登录界面获取请求
	 * @author 李浩然
	 * @param session Session实体
	 * @return  跳转视图及相关模型
	 */
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView loginPage(HttpSession session) {
		ModelAndView modelAndView = new ModelAndView("login");
		if (session.getAttribute(SessionContext.ATTR_USER_ID) != null) {
			userService.logout(Long.parseLong(session.getAttribute(SessionContext.ATTR_USER_ID).toString()), session);
		}
		modelAndView.addObject("user", new User());
		modelAndView.addObject("has_error", false);
		modelAndView.addObject("error_message", "");
		return modelAndView;
	}

	/**
	 * 处理登录请求
	 * @author 李浩然
	 * @param user 用户实体
	 * @param session Session实体
	 * @param captcha 验证码
	 * @return 跳转视图及相关模型
	 */
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ModelAndView login(@ModelAttribute("user") User user, HttpSession session,
							  @RequestParam("captcha") String captcha) {
		ModelAndView modelAndView = new ModelAndView();
		if (session.getAttribute("randomString") == null
				|| !session.getAttribute("randomString").toString().toLowerCase().equals(captcha.toLowerCase())) {
			modelAndView.setViewName("login");
			modelAndView.addObject("has_error", true);
			modelAndView.addObject("error_message", "验证码错误！");
			return modelAndView;
		}
		Result<Boolean> result = userService.login(user.getEmail(), user.getPassword(), session);
		if (!result.isSuccessful()) {
			modelAndView.setViewName("login");
			modelAndView.addObject("has_error", true);
			modelAndView.addObject("error_message", result.getMessage());
			return modelAndView;
		} else {
			modelAndView.setViewName("redirect:/main");
			return modelAndView;
		}
	}

	/**
	 * 处理注册界面获取请求
	 * @author 李浩然
	 * @param user 用户实体
	 * @return 跳转视图及相关模型
	 */
	@RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView registerPage(@ModelAttribute("user") User user) {
        logger.info(dateFormat.format(new Date()) + "register - get");
	    return new ModelAndView("register")
                .addObject("user", (user == null ? new User() : user))
                .addObject("confirmPassword", "")
                .addObject("code", "")
                .addObject("provinces", userService.getAddressContext().getProvinces())
                .addObject("cities", userService.getAddressContext().getCities())
                .addObject("areas", userService.getAddressContext().getAreas())
                .addObject("has_error", false)
                .addObject("error_message", "");
    }

	/**
	 * 处理注册请求
	 * @author 李浩然
	 * @param user 用户实体
	 * @param session Session实体
	 * @param captcha 验证码
	 * @param code 邮件验证码
	 * @return 跳转视图及相关模型
	 */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView register(@ModelAttribute("user") User user, HttpSession session,
                                 @RequestParam("captcha") String captcha, @RequestParam("code") String code) {
        logger.info(dateFormat.format(new Date()) + "register - post");
	    ModelAndView modelAndView = new ModelAndView();
        if (session.getAttribute("randomString") == null
                || !session.getAttribute("randomString").toString().toLowerCase().equals(captcha.toLowerCase())) {
            modelAndView.setViewName("register");
            user.setProvince("0");
            user.setCity("0");
            user.setArea("0");
            modelAndView.addObject("has_error", true)
                    .addObject("error_message", "验证码错误！")
                    .addObject("user", user)
                    .addObject("confirmPassword", user.getPassword())
                    .addObject("code", code)
                    .addObject("provinces", userService.getAddressContext().getProvinces())
                    .addObject("cities", userService.getAddressContext().getCities())
                    .addObject("areas", userService.getAddressContext().getAreas());
            logger.info(dateFormat.format(new Date()) + "register: captcha failed");
            return modelAndView;
        }
        Result<Boolean> result = userService.register(session.getServletContext(), user, code);
        if (!result.isSuccessful()) {
            modelAndView.setViewName("register");
            user.setProvince("0");
            user.setCity("0");
            user.setArea("0");
            modelAndView.addObject("has_error", true)
                    .addObject("error_message", result.getMessage())
                    .addObject("user", user)
                    .addObject("confirmPassword", user.getPassword())
                    .addObject("code", code)
                    .addObject("provinces", userService.getAddressContext().getProvinces())
                    .addObject("cities", userService.getAddressContext().getCities())
                    .addObject("areas", userService.getAddressContext().getAreas());
            logger.info(dateFormat.format(new Date()) + "register: information failed");
            return modelAndView;
        }
        logger.info(dateFormat.format(new Date()) + "register: successful");
        modelAndView.setViewName("redirect:/login");
        return modelAndView;
    }

    /**
     * 处理找回密码界面获取请求
     * @author 李浩然
     * @param session Session实体
     * @return 跳转视图及相关模型
     */
    @RequestMapping(value = "/resetPWD", method = RequestMethod.GET)
    public ModelAndView resetPasswordPage(HttpSession session) {
        logger.info(dateFormat.format(new Date()) + "reset password - get");
        if (session.getAttribute(SessionContext.ATTR_USER_ID) != null) {
            userService.logout(Long.parseLong(session.getAttribute(SessionContext.ATTR_USER_ID).toString()), session);
        }
        return new ModelAndView("reset_password")
                .addObject("has_error", false)
                .addObject("error_message", "")
                .addObject("email", "")
                .addObject("code", "")
                .addObject("password", "");
    }

    /**
     * 处理找回密码请求
     * @author 李浩然
     * @param email 邮箱地址
     * @param code 邮箱验证码
     * @param password 新密码
     * @param captcha 验证码
     * @param randomString Session中的验证码字符串
     * @return 跳转视图及相关模型
     */
    @RequestMapping(value = "/resetPWD", method = RequestMethod.POST)
    public ModelAndView resetPassword(@RequestParam("email") String email,
                                      @RequestParam("code") String code,
                                      @RequestParam("password") String password,
                                      @RequestParam("captcha") String captcha,
                                      @SessionAttribute("randomString") String randomString) {
        logger.info(dateFormat.format(new Date()) + "reset password - get");
        ModelAndView modelAndView = new ModelAndView();
        if (randomString == null || !randomString.toLowerCase().equals(captcha.toLowerCase())) {
            modelAndView.setViewName("reset_password");
            modelAndView.addObject("has_error", true)
                    .addObject("error_message", "验证码错误！")
                    .addObject("email", email)
                    .addObject("code", code)
                    .addObject("password", password);
            logger.info(dateFormat.format(new Date()) + "register: captcha failed");
            return modelAndView;
        }
        Result<Boolean> result = userService.modifyPassword(email, code, password);
        if (!result.isSuccessful()) {
            modelAndView.setViewName("reset_password");
            modelAndView.addObject("has_error", true)
                    .addObject("error_message", result.getMessage())
                    .addObject("email", email)
                    .addObject("code", code)
                    .addObject("password", password);
            logger.info(dateFormat.format(new Date()) + "register: captcha failed");
            return modelAndView;
        } else {
            modelAndView.setViewName("redirect:/login");
            return modelAndView;
        }
    }

    /**
     * 处理修改个人信息界面获取请求
     * @author 李浩然
     * @param session Session实体
     * @param displayName 用户名称
     * @return 跳转视图及相关模型
     */
    @RequestMapping(value = "/modifyInformation", method = RequestMethod.GET)
    public ModelAndView modifyInformationPage(HttpSession session,
                                          @SessionAttribute(SessionContext.ATTR_USER_NAME) String displayName) {
	    ModelAndView modelAndView = new ModelAndView();
	    long userId = Long.parseLong(session.getAttribute(SessionContext.ATTR_USER_ID).toString());
	    Result<User> result = userService.getUserInformation(userId);
	    if (!result.isSuccessful()) {
	        modelAndView.setViewName("error_page");
	        modelAndView.addObject("has_error", true)
                    .addObject("error_message", "发生未知错误，请重试或重新登录！");
	        return modelAndView;
        }
        modelAndView.setViewName("modify_information");
	    User user = result.getResult();
	    user.setProvince("0");
	    user.setCity("0");
	    user.setArea("0");
	    modelAndView.addObject("has_error", false)
                .addObject("error_message", "")
                .addObject("display_name", displayName)
                .addObject("provinces", userService.getAddressContext().getProvinces())
                .addObject("cities", userService.getAddressContext().getCities())
                .addObject("areas", userService.getAddressContext().getAreas())
                .addObject("user", user);
	    return modelAndView;
    }

    /**
     * 处理修改个人信息请求
     * @author 李浩然
     * @param user 用户实体
     * @return 跳转视图及相关模型
     */
    @RequestMapping(value = "/modifyInformation", method = RequestMethod.POST)
    public ModelAndView modifyInformation(@ModelAttribute("user") User user) {
        ModelAndView modelAndView = new ModelAndView();
        Result<Boolean> result = userService.modifyUserInformation(user);
        if (!result.isSuccessful()) {
            modelAndView.setViewName("error_page");
            modelAndView.addObject("has_error", true)
                    .addObject("error_message", result.getMessage());
            return modelAndView;
        }
        modelAndView.setViewName("redirect:/main");
        return modelAndView;
    }

    /**
     * 处理修改头像请求
     * @author 李浩然
     * @param session Session实体
     * @param file 头像图片文件
     * @return 跳转视图及相关模型
     */
    @RequestMapping(value = "/modifyHeadImage", method = RequestMethod.POST)
    public ModelAndView modifyHeadImage(HttpSession session,
            @RequestParam("image") MultipartFile file) {
        ModelAndView modelAndView = new ModelAndView();
        long userId = Long.parseLong(session.getAttribute(SessionContext.ATTR_USER_ID).toString());
        Result<Boolean> result = userService.modifyHeadImage(session.getServletContext(), userId, file);
        if (!result.isSuccessful()) {
            modelAndView.setViewName("error_page");
            modelAndView.addObject("has_error", true)
                    .addObject("error_message", result.getMessage());
            return modelAndView;
        }
        modelAndView.setViewName("redirect:/main");
        return modelAndView;
    }



}
























