package chinaren.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import chinaren.dao.AddressDao;
import chinaren.dao.MessageDao;
import chinaren.model.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import chinaren.common.SessionContext;
import chinaren.dao.UserDao;
import chinaren.util.EmailUtil;
import chinaren.util.HashUtil;
import chinaren.util.HeadImageUtil;

/**
 * 用户相关数据处理，服务层实现类
 * @ClassName UserServiceImpl 
 * @author 李浩然
 * @date 2017年7月21日
 * @version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserDao userDao;

	@Autowired
    private AddressDao addressDao;

	@Autowired
    private MessageDao messageDao;
	
	private Logger logger = Logger.getLogger(UserServiceImpl.class);
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss - ");
	
	private UserContext userContext;
	
	private SessionContext sessionContext;

	private AddressContext addressContext;
	
	private static final String CHECK_EMAIL = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
	
	private static final String CHECK_PHONE = "^[0-9]*$";

	/**
	 * 构造方法
	 */
	public UserServiceImpl() {
		sessionContext = SessionContext.getInstance();
		userContext = new UserContext();
	}

	/**
	 * @see chinaren.service.UserService#register(javax.servlet.ServletContext, chinaren.model.User, java.lang.String)
	 */
	@Override
	public Result<Boolean> register(ServletContext servletContext, User user, String code) {
		logger.info(dateFormat.format(new Date()) + "register: validate email - " + user.getEmail());
		// 验证邮箱是否存在
		if (user.getEmail() == null
				|| user.getEmail().trim().equals("")
				|| !Pattern.compile(CHECK_EMAIL).matcher(user.getEmail()).matches()
				|| userDao.selectUserByEmail(user.getEmail()).isSuccessful()) {
			return new Result<Boolean>(false, "邮箱已存在", false);
		}
		// 验证邮箱及验证码
		if (!userContext.verifyEmailCode(user.getEmail(), code)) {	
			return new Result<Boolean>(false, "邮箱错误或邮箱验证码错误，请核对！", false);
		}
		// 验证密码合法性
		logger.info(dateFormat.format(new Date()) + "register: validate password - " + user.getPassword());
		if (user.getPassword() == null
				|| user.getPassword().trim().equals("")
				|| user.getPassword().length() < 8
				|| user.getPassword().length() > 32) {
			return new Result<Boolean>(false, "密码长度必须在8到32位之间！", false);
		}
		// 验证姓名
		logger.info(dateFormat.format(new Date()) + "register: validate name - " + user.getName());
		if (user.getName() == null 
				|| user.getName().trim().equals("")) {
			
		}
		// 验证手机号合法性
		logger.info(dateFormat.format(new Date()) + "register: validate phone - " + user.getPhone());
		if (user.getPhone() == null
				|| user.getPhone().trim().equals("")
				|| user.getPhone().length() != 11
				|| !Pattern.compile(CHECK_PHONE).matcher(user.getPhone()).matches()) {
			return new Result<Boolean>(false, "手机号不合法，请检查！", false);
		}

		user.setHeadImage(HeadImageUtil.DEFAULT_IMAGE + HeadImageUtil.POSTFIX);
		
		// 创建默认个人简介
		user.setIntroduction("此人很懒，什么也没有留下！");
		logger.info(dateFormat.format(new Date()) + "register: set default introduction - " + user.getIntroduction());
		
		// 密码哈希处理
		user.setPassword(HashUtil.generate(user.getPassword()));
		logger.info(dateFormat.format(new Date()) + "register: transform password - " + user.getPassword());
		
		logger.info(dateFormat.format(new Date()) + "register: insert user into database");
		Result<User> result = userDao.insertUser(user);
		
		logger.info(dateFormat.format(new Date()) + "register: finished - user " + user.getUserId());
		if (!result.isSuccessful()) {
			return new Result<Boolean>(false, "注册时发生未知错误，请稍后重试！", false);
		}

		// 创建默认头像
		logger.info(dateFormat.format(new Date()) + "register: create head image");
		if (!HeadImageUtil.createDefaultHeadImage(servletContext.getRealPath(HeadImageUtil.PATH_NAME), result.getResult().getUserId())) {
			return new Result<Boolean>(false, "创建头像时发生错误，请重试！", false);
		}

		return new Result<Boolean>(true, "注册成功!", true);
	}

	/**
	 * @see chinaren.service.UserService#login(java.lang.String, java.lang.String, javax.servlet.http.HttpSession)
	 */
	@Override
	public Result<Boolean> login(String email, String password, HttpSession session) {
		logger.info(dateFormat.format(new Date()) + "login - " + email + "::" + password);
		
		if (email == null || password == null) {
			return new Result<Boolean>(false, "邮箱账号或密码错误", false);
		}
		
		Result<User> result = userDao.selectUserByEmail(email);
		
		// 验证邮箱和密码
		if (!result.isSuccessful() 
				|| !HashUtil.verify(password, result.getResult().getPassword())) {
			return new Result<Boolean>(false, "邮箱账号或密码错误！", false);
		}
		
		if (!userContext.addUser(result.getResult())) {
			return new Result<Boolean>(false, "登录时发生未知错误，请重试！", false);
		}
		session.setAttribute(SessionContext.ATTR_USER_ID, String.valueOf(result.getResult().getUserId()));
		session.setAttribute(SessionContext.ATTR_USER_NAME, result.getResult().getName());
		sessionContext.sessionHandlerByCacheMap(session);
		
		logger.info(dateFormat.format(new Date()) + "login successful - user" + result.getResult().getUserId());
		return new Result<Boolean>(true, "登录成功", true);
	}

	/**
	 * @see chinaren.service.UserService#logout(long, javax.servlet.http.HttpSession)
	 */
	@Override
	public void logout(long userId, HttpSession session) {
		userContext.removeUser(userId);
		session.invalidate();
		logger.info(dateFormat.format(new Date()) + "logout - user " + userId);
	}

	/**
	 * @see chinaren.service.UserService#getUserInformation(long)
	 */
	@Override
	public Result<User> getUserInformation(long userId) {
		logger.info(dateFormat.format(new Date()) + "get user information - user " + userId);
		User user = userContext.getUser(userId);
		if (user == null) {
			Result<User> result = userDao.selectUserByUserId(userId);
			if (result.isSuccessful()) {
				user = result.getResult();
			} else {
				return new Result<User>(false, "未查询到相应的用户信息", null);
			}
		}
		return new Result<User>(true, "成功获取用户信息", user);
	}

	/**
	 * @see chinaren.service.UserService#outputHeadImage(long, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void outputHeadImage(long userId, HttpServletRequest request, HttpServletResponse response) {
		logger.info(dateFormat.format(new Date()) + "output head image - user " + userId);
		HeadImageUtil.outputHeadImage(
				request.getSession().getServletContext().getRealPath(HeadImageUtil.PATH_NAME), 
				userId, response);
	}

	/**
	 * @see chinaren.service.UserService#modifyPassword(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Result<Boolean> modifyPassword(String email, String code, String password) {
		logger.info(dateFormat.format(new Date()) + "modify password - " + email + "::" + password);
		Result<User> result = null;
		if (email == null
				|| email.trim().equals("")
				|| !Pattern.compile(CHECK_EMAIL).matcher(email).matches()
				|| !(result = userDao.selectUserByEmail(email)).isSuccessful()) {
			return new Result<Boolean>(false, "邮箱不存在", false);
		}
		
		if (!userContext.verifyEmailCode(email, code)) {	
			return new Result<Boolean>(false, "邮箱错误或邮箱验证码错误，请核对！", false);
		}
		
		result = userDao.updatePassword(result.getResult().getUserId(), HashUtil.generate(password));
		if (!result.isSuccessful()) {
			logger.info(dateFormat.format(new Date()) + "modify password: failed - " + email);
			return new Result<Boolean>(false, "修改密码时发生未知错误，请重试！", false);
		}
		userContext.update(result.getResult().getUserId());
		logger.info(dateFormat.format(new Date()) + "modify password: successful - " + email);
		return new Result<Boolean>(true, "修改密码成功!", true);
	}

	/**
	 * @see chinaren.service.UserService#modifyUserInformation(chinaren.model.User)
	 */
	@Override
	public Result<Boolean> modifyUserInformation(User user) {
		logger.info(dateFormat.format(new Date()) + "modify user's information - user " + user.getUserId());
		Result<User> result = userDao.updateUserInfo(user);
		if (!result.isSuccessful()) {
			logger.info(dateFormat.format(new Date()) + "modify user's information: failed - " + user.getUserId());
			return new Result<Boolean>(false, "修改密码时发生未知错误，请重试！", false);
		}
		userContext.update(user.getUserId());
		logger.info(dateFormat.format(new Date()) + "modify user's information: successful - " + user.getUserId());
		return new Result<Boolean>(true, "修改用户信息成功!", true);
	}

	/**
	 * @see chinaren.service.UserService#modifyHeadImage(javax.servlet.ServletContext, long, org.springframework.web.multipart.MultipartFile)
	 */
	@Override
	public Result<Boolean> modifyHeadImage(ServletContext servletContext, long userId, MultipartFile image) {
		logger.info(dateFormat.format(new Date()) + "modify user's head image - user " + userId);
		if (!HeadImageUtil.modifyHeadImage(servletContext.getRealPath(HeadImageUtil.PATH_NAME), userId, image)) {
			logger.info(dateFormat.format(new Date()) + "modify user's head image: failed - " + userId);
			return new Result<Boolean>(false, "修改头像时发生未知错误，请重试！", false);
		}
		userContext.update(userId);
		logger.info(dateFormat.format(new Date()) + "modify user's head image: successful - " + userId);
		return new Result<Boolean>(true, "修改头像成功！", true);
	}

	/**
	 * @see chinaren.service.UserService#sendEmail(java.lang.String)
	 */
	@Override
	public Result<Boolean> sendEmail(String email) {
		logger.info(dateFormat.format(new Date()) + "send email to " + email);
		
		if (email == null
				|| email.trim().equals("")
				|| !Pattern.compile(CHECK_EMAIL).matcher(email).matches()) {
			logger.info(dateFormat.format(new Date()) + "send email: invalid email - " + email);
			return new Result<Boolean>(false, "邮箱不合法", false);
		}
		
		if (!EmailUtil.sendMail(email, userContext.createEmailCode(email))) {
			logger.info(dateFormat.format(new Date()) + "send email: failed - " + email);
			return new Result<Boolean>(false, "发送邮件时发送错误，请检查邮箱是否有效！", false);
		}
		logger.info(dateFormat.format(new Date()) + "send email: successful - " + email);
		return new Result<Boolean>(true, "发送邮件成功！", true);
	}

	/**
	 * @see chinaren.service.UserService#verifyEmail(java.lang.String, java.lang.String)
	 */
	@Override
	public Result<Boolean> verifyEmail(String email, String code) {
		logger.info(dateFormat.format(new Date()) + "verify email - " + email + "::" + code);
		if (!userContext.verifyEmailCode(email, code)) {
			logger.info(dateFormat.format(new Date()) + "verify email: failed - " + email);
			return new Result<Boolean>(false, "验证码错误或验证码已失效，请输入正确的验证码或选择重新发送邮件", false);
		}
		logger.info(dateFormat.format(new Date()) + "verify email: successful - " + email);
		return new Result<Boolean>(true, "发送邮件成功！", true);
	}

	/**
	 * @see chinaren.service.UserService#getStatisticsResult(long)
	 */
	public StatisticsResult getStatisticsResult(long userId) throws ParseException {
	    StatisticsResult statisticsResult = getUserContext().getStatisticsResult(userId);
	    if (statisticsResult == null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
            // 计算日期
            Date now = new Date();
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(now);
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            Date begin = calendar.getTime();
            // 结束日期计算
            // 格式日期
            begin = simpleDateFormat.parse(simpleDateFormat.format(begin));
            now = simpleDateFormat.parse(simpleDateFormat.format(now));
            // 创建列表
            List<String> dateStrings = new ArrayList<>();
            List<Long> classIds = new ArrayList<>();
            List<String> classNames = new ArrayList<>();
            Map<String, Map<Long, MessageCounter>> messageCounters = new HashMap<>();
            Calendar dd = Calendar.getInstance();
            dd.setTime(begin);
            while (dd.getTime().before(now)) {
                String dateStr = simpleDateFormat.format(dd.getTime());
                messageCounters.put(dateStr, new HashMap<>());
                dateStrings.add(dateStr);
                dd.add(Calendar.DAY_OF_MONTH, 1);
            }
            List<Message> messages = messageDao.selectMessageByUserId(userId).getResult();
            logger.info("message count: " + messages.size());
            for (Message message : messages) {
                if (begin.before(message.getMsgTime()) && now.after(message.getMsgTime())) {
                    String dateStr = simpleDateFormat.format(message.getMsgTime());
                    Map<Long, MessageCounter> mcMap = messageCounters.get(dateStr);
                    if (messageCounters.get(dateStr).get(message.getClassId()) == null) {
                        MessageCounter mc = new MessageCounter();
                        mc.setDate(dateStr);
                        mc.setClassId(message.getClassId());
                        mc.setClassName(message.getClassName());
                        mcMap.put(message.getClassId(), mc);
                    }
                    if (!classIds.contains(message.getClassId())) {
                        classIds.add(message.getClassId());
                        classNames.add(message.getClassName());
                    }
                    mcMap.get(message.getClassId()).getMessages().add(message);
                }
            }
            int[][] counts = new int[classIds.size()][dateStrings.size()];
            for (int i = 0; i < dateStrings.size(); i++) {
                for (int j = 0; j < classIds.size(); j++) {
                    try {
                        counts[j][i] = messageCounters.get(dateStrings.get(i)).get(classIds.get(j)).getMessages().size();
                    } catch (NullPointerException e) {
                        counts[j][i] = 0;
                    }
                }
            }
            for (int i = 0; i < dateStrings.size(); i++) {
                dateStrings.set(i, dateStrings.get(i).substring(dateStrings.get(i).indexOf(".") + 1));
            }
            statisticsResult = new StatisticsResult(dateStrings, classNames, counts);
            getUserContext().addStatisticsResult(userId, statisticsResult);
        }
        return statisticsResult;
    }

	/**
	 * @see chinaren.service.UserService#getAddressContext()
	 */
	@Override
    public UserServiceImpl.AddressContext getAddressContext() {
	    if (addressContext == null) {
	        addressContext = new AddressContext();
        }
        return addressContext;
    }

	/**
	 * @see chinaren.service.UserService#getUserContext()
	 */
    @Override
    public UserServiceImpl.UserContext getUserContext() {
		if (userContext == null) {
			userContext = new UserContext();
		}
		return userContext;
	}

    /**
     * 维护地址信息的类
     * @ClassName AddressContext 
     * @author 李浩然
     * @date 2017年7月27日
     * @version 1.0
     */
    public class AddressContext {

        private List<Province> provinces;

        private Map<String, List<City>> cities;

        private Map<String, List<Area>> areas;

        /**
         * 私有构造方法
         */
        private AddressContext() {
            cities = new HashMap<>();
            areas = new HashMap<>();
            load();
        }

        /**
         * 从数据库中加载地址信息
         * @author 李浩然
         */
        private void load() {
            // 装载省份
            System.err.println(addressDao);
            Result<List<Province>> provinceResult = addressDao.selectProvinces();
            provinces = provinceResult.getResult();
            // 装载城市和地区
            for (Province province : provinces) {
                Result<List<City>> cityResult = addressDao.selectCities(province.getProvinceId());
                cities.put(province.getProvinceId(), cityResult.getResult());
                for (City city : cityResult.getResult()) {
                    Result<List<Area>> areaResult = addressDao.selectAreas(city.getCityId());
                    areas.put(city.getCityId(), areaResult.getResult());
                }
            }
        }

        /**
         * 获取省份列表
         * @author 李浩然
         * @return 省份列表
         */
        public List<Province> getProvinces() {
            return provinces;
        }

        /**
         * 获取城市列表
         * @author 李浩然
         * @return 城市列表
         */
        public Map<String, List<City>> getCities() {
            return cities;
        }

        /**
         * 获取地区列表
         * @author 李浩然
         * @return 地区列表
         */
        public Map<String, List<Area>> getAreas() {
            return areas;
        }

    }

	/**
	 * 用户数据动态维护类
	 * @ClassName UserContext
	 * @author 李浩然
	 * @date 2017年7月22日
	 * @version 1.0
	 */
	public class UserContext {

		/**
		 * 已登录的用户集
		 */
		private Map<Long, User> loginUsers;

		/**
		 * 待验证的邮箱验证码
		 */
		private Map<String, String> emailCode;

		/**
		 * 保存已登录用户的活跃数据统计结果
		 */
		private Map<Long, StatisticsResult> statistics;

		/**
		 * 私有构造方法
		 */
		private UserContext() {
			loginUsers = new HashMap<Long, User>();
			emailCode = new HashMap<String, String>();
			statistics = new HashMap<>();
		}

		/**
		 * 添加一个用户至登录用户集中
		 * @author 李浩然
		 * @param user 待添加的用户
		 * @return 操作是否成功
		 */
		public boolean addUser(User user) {
			if (user != null && user.getUserId() > 0) {
				loginUsers.remove(user.getUserId());
				loginUsers.put(user.getUserId(), user);
				return true;
			}
			return false;
		}

		/**
		 * 从登录用户集中删除一个用户
		 * @author 李浩然
		 * @param userId 待删除用户的ID
		 */
		public void removeUser(long userId) {
			loginUsers.remove(userId);
			statistics.remove(userId);
		}

		/**
		 * 从登录用户集中获取一个用户
		 * @author 李浩然
		 * @param userId 要获取的用户的ID
		 * @return 用户实体，若不存在相应用户，返回null
		 */
		public User getUser(long userId) {
			return loginUsers.get(userId);
		}

		/**
		 * 生成一个随机的邮件验证码
		 * @author 李浩然
		 * @param email 待生成验证码的邮箱地址
		 * @return 生成的邮件验证码
		 */
		public String createEmailCode(String email) {
			Random random = new Random();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < 6; i++) {
				int number = random.nextInt(10);
				sb.append(number);
			}
			String code = sb.toString();
			emailCode.remove(email);
			emailCode.put(email, code);
			return code;
		}

		/**
		 * 验证邮件验证码是否正确
		 * @author 李浩然
		 * @param email 邮箱地址
		 * @param code 邮件验证码
		 * @return 若邮件验证码正确，返回true；否则，返回false
		 */
		public boolean verifyEmailCode(String email, String code) {
			if (email != null && code != null && code.equals(emailCode.get(email))) {
				emailCode.remove(email);
				return true;
			}
			return false;
		}

        /**
         * 获取用户的统计数据
         * @param userId 用户的ID
         * @return 用户的统计数据
         */
		public StatisticsResult getStatisticsResult(long userId) {
			return statistics.get(userId);
		}

        /**
         * 设置用户的统计数据
         * @param userId 用户的ID
         * @param statisticsResult 用户的统计数据
         */
		public void addStatisticsResult(long userId, StatisticsResult statisticsResult) {
			statistics.put(userId, statisticsResult);
		}

		/**
		 * 更新登录用户集中的用户信息
		 * @author 李浩然
		 * @param userId 待更新的用户ID集
		 */
		public void update(Long... userId) {
			for(long id : userId) {
				if (loginUsers.containsKey(id)) {
					Result<User> result = userDao.selectUserByUserId(id);
					if (result.isSuccessful()) {
						loginUsers.remove(id);
						loginUsers.put(result.getResult().getUserId(), result.getResult());
					}
				}
			}
		}

		/**
		 * 更新登录用户集中的用户信息
		 * @author 李浩然
		 * @param userId 待更新的用户ID集
		 */
		public void update(List<Long> userId) {
			for(long id : userId) {
				if (loginUsers.containsKey(id)) {
					Result<User> result = userDao.selectUserByUserId(id);
					if (result.isSuccessful()) {
						loginUsers.remove(id);
						loginUsers.put(result.getResult().getUserId(), result.getResult());
					}
				}
			}
		}

	}
}
