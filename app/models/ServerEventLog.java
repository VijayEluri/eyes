package models;

import java.util.Date;

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
    return ServerEventLog.count(
        "server = ? and status = ? and created >= ? and created <= ?", server,
        status, begin, end);
  }

  public static long eventCount(Server server, Date begin, Date end) {
    return ServerEventLog.count("server = ? and created >= ? and created <= ?",
        server, begin, end);
  }

  public static long eventCount(Server server, Status status, Date date) {
    return ServerEventLog.count("byServerAndCreatedAndStatus", server, date,
        status);
  }

  public static long eventCount(Server server, Date date) {
    return ServerEventLog.count("byServerAndCreated", server, date);
  }

  public static long eventCount(Server server, Status status) {
    return ServerEventLog.count("byServerAndStatus", server, status);
  }

  public static long eventCount(Server server) {
    return ServerEventLog.count("byServer", server);
  }

}
