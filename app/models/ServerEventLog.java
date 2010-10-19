package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import models.Server.Status;
import play.db.jpa.Model;

@Entity
public class ServerEventLog extends Model {

  @OneToOne
  @JoinColumn(name = "server_id")
  public Server server;
  public Date created;
  public Status status;
  public String message;

  public ServerEventLog(Server server, Status status, String message) {
    this.server = server;
    this.status = status;
    this.message = message;
    this.created = new Date();
  }

  public static void submit(Server server, Status status, String message) {
    ServerEventLog event = new ServerEventLog(server, status, message);
    event.save();
  }

  public static long eventCount(Server server, Status status, Date begin,
      Date end) {
    long count = 0;

    if (status != null) {
      if (begin != null && end != null) {
        count = ServerEventLog.count(
            "server = ? and status = ? and created >= ? and created <= ?",
            server, status, begin, end);
      } else if (begin != null) {
        count = ServerEventLog.count("byServerAndCreatedAndStatus", server,
            begin, status);
      } else {
        count = ServerEventLog.count("byServerAndStatus", server, status);
      }
    } else {
      if (begin != null && end != null) {
        count = ServerEventLog.count(
            "server = ? and created >= ? and created <= ?", server, begin, end);
      } else if (begin != null) {
        count = ServerEventLog.count("byServerAndCreated", server, begin);
      } else {
        count = ServerEventLog.count("byServer", server);
      }
    }

    return count;
  }

}
