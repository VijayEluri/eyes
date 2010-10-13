import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import models.ProbeEventLog;
import models.Server;
import models.Server.Status;
import models.ServerEventLog;
import models.User;
import models.probe.Probe;
import models.probe.ProbeResult;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.jobs.Every;
import play.jobs.Job;
import play.libs.Mail;

@Every("5mn")
public class Monitor extends Job {

  public void doJob() {

    Logger.info("Checking probes");
    Probe[] probes = Server.allProbes();
    for (Probe probe : probes) {
      try {
        Class probeClass = probe.getClass();

        Field field = probeClass.getField("status");
        field.set(probe, probe.check());

        Method method = probeClass.getMethod("save");
        method.invoke(probe);
      } catch (Exception e) {
        Logger.error(e, "Can't save probe status");
      }
    }

    List<Server> servers = Server.all().fetch();
    for (Server server : servers) {
      server.status = Status.UP;
      StringBuffer buffer = new StringBuffer();
      Probe serverProbes[] = server.probes();
      for (Probe probe : serverProbes) {
        ProbeResult status = probe.status();
        if (!status.success && server.status == Status.UP) {
          server.status = Status.DOWN;
        }

        buffer.append(String.format("%s: %s\n<br />\n", probe.name(),
            status.success ? "OK" : status.message));

        ProbeEventLog.submit(probe.getId(), probe.type(), status.success,
            status.message);
      }

      if (server.status == Status.DOWN) {
        server.message = Messages.get("server.probe.down");
      } else {
        server.message = "";
      }
      server.save();
      ServerEventLog.submit(server, server.status, server.message);

      if (server.status == Status.DOWN && server.probes().length > 0
          && server.responders.size() > 0) {
        ArrayList<String> recipients = new ArrayList<String>();
        Set<User> responders = server.responders;
        for (User responder : responders) {
          recipients.add(responder.email);
        }

        String from = Play.configuration.getProperty("eyes.mail");
        HtmlEmail htmlEmail = new HtmlEmail();
        try {
          htmlEmail.setFrom(from);
          htmlEmail.setTo(recipients);
          htmlEmail.setSubject(String.format("%s have some problems",
              server.name));
          htmlEmail.setHtmlMsg(buffer.toString());
          Mail.send(htmlEmail);
        } catch (EmailException e) {
          Logger.error(e, "Can't send mail");
        }

      }

    }

  }
}
