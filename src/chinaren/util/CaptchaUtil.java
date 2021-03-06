package chinaren.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * 关于验证码的工具类
 * @ClassName CaptchaUtil
 * @author 李浩然
 * @date 2017年7月20日
 * @version 1.0
 */
@SuppressWarnings("restriction")
public final class CaptchaUtil
{
    private CaptchaUtil(){}

    /*
     * 随机字符字典
     */
    private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

    /*
     * 随机数
     */
    private static Random random = new Random();

    /**
     * 获取6位随机数
     * @author 李浩然
     * @return 六位随机数字符串
     */
    private static String getRandomString()
    {
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < 6; i++)
        {
            buffer.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return buffer.toString();
    }

    /**
     * 获取随机数颜色
     * @author 李浩然
     * @return 随机的一种颜色
     */
    private static Color getRandomColor()
    {
        return new Color(random.nextInt(255),random.nextInt(255),
                random.nextInt(255));
    }

    /**
     * 返回某颜色的反色
     * @author 李浩然
     * @param c 要取反色的颜色
     * @return 颜色的反色
     */
    private static Color getReverseColor(Color c)
    {
        return new Color(255 - c.getRed(), 255 - c.getGreen(),
                255 - c.getBlue());
    }

    /**
     * 输出随机数
     * @author 李浩然
     * @param request HTTP请求实体
     * @param response HTTP响应实体
     * @throws ServletException 发生Servlet错误时，抛出该异常
     * @throws IOException 发生IO错误时，抛出该异常
     */
    public static void outputCaptcha(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        response.setContentType("image/jpeg");

        String randomString = getRandomString();
        request.getSession(true).setAttribute("randomString", randomString);

        int width = 100;
        int height = 30;

        Color color = getRandomColor();
        Color reverse = getReverseColor(color);

        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.setColor(reverse);
        g.drawString(randomString, 18, 20);
        for (int i = 0, n = random.nextInt(100); i < n; i++)
        {
            g.drawRect(random.nextInt(width), random.nextInt(height), 1, 1);
        }

        // 转成JPEG格式
        ServletOutputStream out = response.getOutputStream();
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        encoder.encode(bi);
        out.flush();
    }
}
