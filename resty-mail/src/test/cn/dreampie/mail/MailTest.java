package cn.dreampie.mail;

import org.apache.commons.mail.HtmlEmail;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by wangrenhui on 15/4/13.
 */
public class MailTest {

  @Before
  public void setUp() throws Exception {
    MailPlugin mailPlugin = new MailPlugin();
    mailPlugin.start();
  }

  @Test
  public void testSendMail() throws Exception {
    HtmlEmail htmlEmail = MailSender.getHtmlEmail("测试", "173956022@qq.com");
    //String cid1 = htmlEmail.embed(new File(图片文件地址1), "1");
    //String cid2 = htmlEmail.embed(new File(图片文件地址2), "2");
    //发送图片在htmlMsg里加上这个 <img src="cid:" + cid1 + "\"'/><img src=\"cid:" + cid2 + ""'/>
    htmlEmail.setHtmlMsg("<a href='www.dreampie.cn'>Dreampie</a>");
    htmlEmail.send();

  }
}