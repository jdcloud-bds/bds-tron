package org.tron.core.services.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.core.Wallet;
import org.tron.core.config.args.Args;
import org.tron.protos.Protocol.Block;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SendBlockByNumServlet extends HttpServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      long num = Long.parseLong(request.getParameter("num"));
      Block reply = wallet.getBlockByNum(num);
      if (reply != null && !Args.getInstance().getKafkaEndpoint().equals("")) {
        HttpUtil.postJsonContent(Args.getInstance().getKafkaEndpoint(), Util.printBlockKafka(reply));
        response.getWriter().println(Util.printBlockKafka(reply));
      } else {
        response.getWriter().println("{}");
      }
    } catch (Exception e) {
      logger.debug("Exception: {}", e.getMessage());
      try {
        response.getWriter().println(Util.printErrorMsg(e));
      } catch (IOException ioe) {
        logger.debug("IOException: {}", ioe.getMessage());
      }
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String input = request.getReader().lines()
          .collect(Collectors.joining(System.lineSeparator()));
      NumberMessage.Builder build = NumberMessage.newBuilder();
      boolean visible = Util.getVisiblePost(input);
      JsonFormat.merge(input, build, visible);
      Block reply = wallet.getBlockByNum(build.getNum());
      if (reply != null && !Args.getInstance().getKafkaEndpoint().equals("")) {
        HttpUtil.postJsonContent(Args.getInstance().getKafkaEndpoint(), Util.printBlockKafka(reply));
        response.getWriter().println(Util.printBlockKafka(reply));
      } else {
        response.getWriter().println("{}");
      }
    } catch (Exception e) {
      logger.debug("Exception: {}", e.getMessage());
      try {
        response.getWriter().println(Util.printErrorMsg(e));
      } catch (IOException ioe) {
        logger.debug("IOException: {}", ioe.getMessage());
      }
    }
  }
}