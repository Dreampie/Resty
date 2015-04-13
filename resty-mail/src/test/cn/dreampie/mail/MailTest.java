package cn.dreampie.mail;

import org.apache.commons.mail.HtmlEmail;
import org.junit.Before;
import org.junit.Test;

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
    //SimpleEmail simpleEmail=MailSender.getSimpleEmail("测试主题","测试内容","xx@qq.com");
    //simpleEmail.send();

    //MailSender.sendText("测试主题","测试内容","xx@qq.com");

    HtmlEmail htmlEmail = MailSender.getHtmlEmail("测试", "xx@qq.com");
    //String cid1 = htmlEmail.embed(new File(图片文件地址1), "1");
    //String cid2 = htmlEmail.embed(new File(图片文件地址2), "2");
    //发送图片在htmlMsg里加上这个 <img src="cid:" + cid1 + "\"'/><img src=\"cid:" + cid2 + ""'/>
    htmlEmail.setHtmlMsg("<a href='www.dreampie.cn'>Dreampie</a>");
    htmlEmail.send();

    //MailSender.sendHtml("测试主题", "<a href='www.dreampie.cn'>Dreampie</a>", "xx@qq.com");

    //MultiPartEmail multiPartEmail=MailSender.getMultiPartEmail("测试主题","测试内容",new EmailAttachment(),"xx@qq.com");
    //multiPartEmail.send();

    //MailSender.sendAttachment("测试主题","测试内容",new EmailAttachment(),"xx@qq.com");
  }
}