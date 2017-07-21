package chinaren.service;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.multipart.MultipartFile;

import chinaren.model.Result;
import chinaren.model.User;

/**
 * 用户相关数据处理，服务层接口
 * @ClassName UserService 
 * @author 李浩然
 * @date 2017年7月21日
 * @version 1.0
 */
public interface UserService {
	
	/**
	 * 新用户注册服务
	 * @author 李浩然
	 * @param user 待注册的新用户实体
	 * @return 注册结果
	 */
	public Result<Boolean> register(User user);
	
	/**
	 * 用户登录服务
	 * @author 李浩然
	 * @param user 待登录的用户实体
	 * @param session 请求登录的session
	 * @return 登录结果
	 */
	public Result<Boolean> login(User user, HttpSession session);
	
	/**
	 * 用户注销服务
	 * @author 李浩然
	 * @param userId 待注销的用户的ID
	 * @param session 请求注销的session
	 */
	public void logout(long userId, HttpSession session);
	
	/**
	 * 获取用户信息服务
	 * @author 李浩然
	 * @param userId 需要获取信息的用户的ID
	 * @return 包含用户实体的服务结果
	 */
	public Result<User> getUserInformation(long userId);
	
	/**
	 * 输出用户头像服务
	 * @author 李浩然
	 * @param userId 需要输出头像的用户的ID
	 * @param response 头像输出流
	 */
	public void outputHeadImage(long userId, HttpServletResponse response);
	
	/**
	 * 修改密码服务
	 * @author 李浩然
	 * @param userId 待修改密码的用户的ID
	 * @param password 新密码
	 * @return 修改密码结果
	 */
	public Result<Boolean> modifyPassword(long userId, String password);
	
	/**
	 * 修改用户信息服务
	 * @author 李浩然
	 * @param user 待修改信息的用户实体（包含修改后的用户信息）
	 * @return 修改用户信息结果
	 */
	public Result<Boolean> modifyUserInformation(User user);
	
	/**
	 * 修改用户头像服务
	 * @author 李浩然
	 * @param userId 待修改头像的用户的ID
	 * @param image 新的用户头像
	 * @return 修改用户头像结果
	 */
	public Result<Boolean> modifyHeadImage(long userId, MultipartFile image);
}




















