package org.tron.core.services.http;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.common.utils.ByteArray;
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
public class SendBlockByIdServlet extends HttpServlet {

  @Autowired
  private Wallet wallet;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      String input = request.getParameter("value");
      Block reply = wallet.getBlockById(ByteString.copyFrom(ByteArray.fromHexString(input)));
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
      BytesMessage.Builder build = BytesMessage.newBuilder();
      boolean visible = Util.getVisiblePost(input);
      JsonFormat.merge(input, build, visible);
      Block reply = wallet.getBlockById(build.getValue());
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